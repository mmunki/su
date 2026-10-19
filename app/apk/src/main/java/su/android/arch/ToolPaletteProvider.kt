package su.android.arch

/**
 * Implemented by screens that want extra tools in the dense tool palette of the
 * main action bar (the second, scrollable row).
 */
interface ToolPaletteProvider {

    /** Menu resource describing this screen's palette tools, or 0 for none. */
    val paletteMenuRes: Int get() = 0

    /** Called when one of [paletteMenuRes] items is tapped. */
    fun onPaletteItemSelected(id: Int): Boolean = false
}
