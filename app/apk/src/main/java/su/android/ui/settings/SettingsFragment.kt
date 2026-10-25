package su.android.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import su.android.R
import su.android.arch.BaseFragment
import su.android.arch.viewModel
import su.android.databinding.FragmentSettingsBinding
import su.android.databinding.ItemSettingsBinding
import su.android.R as CoreR

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    override val layoutRes = R.layout.fragment_settings
    override val viewModel by viewModel<SettingsViewModel>()
    override val snackbarView: View get() = binding.snackbarContainer

    private val adapter = SettingsAdapter()

    override fun onStart() {
        super.onStart()
        activity?.title = resources.getString(CoreR.string.settings)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsList.adapter = adapter
        populateCards()
    }

    private fun populateCards() {
        val items = viewModel.items
            .filterIsInstance<SettingsGroupItem.CardGroup>()
            .flatMap { it.children }
        adapter.submit(items)
    }

    override fun onResume() {
        super.onResume()
        viewModel.items.forEach { group ->
            if (group is SettingsGroupItem.CardGroup) {
                group.children.forEach { it.refresh() }
            } else {
                group.refresh()
            }
        }
    }

    override fun onPreBind(binding: FragmentSettingsBinding) = Unit

    private inner class SettingsAdapter : RecyclerView.Adapter<SettingsAdapter.Holder>() {

        private val items = mutableListOf<BaseSettingsItem>()
        private val margin by lazy { resources.getDimensionPixelSize(R.dimen.l1) / 2 }

        fun submit(newItems: List<BaseSettingsItem>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val binding = DataBindingUtil.inflate<ItemSettingsBinding>(
                layoutInflater,
                R.layout.item_settings,
                parent,
                false
            )
            return Holder(binding)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = items[position]
            holder.binding.item = item
            holder.binding.handler = viewModel as? BaseSettingsItem.Handler
            holder.binding.lifecycleOwner = viewLifecycleOwner
            holder.binding.executePendingBindings()

            (holder.itemView.layoutParams as? RecyclerView.LayoutParams)?.apply {
                setMargins(0, margin, 0, margin)
            }
        }

        inner class Holder(val binding: ItemSettingsBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
