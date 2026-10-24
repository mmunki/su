
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import java.io.File
import java.util.Properties

private val props = Properties()
private var commitHash = ""
private val supportAbis = setOf("armeabi-v7a", "x86", "arm64-v8a", "x86_64", "riscv64")
private val defaultAbis = setOf("armeabi-v7a", "x86", "arm64-v8a", "x86_64")

object Config {
    operator fun get(key: String): String? {
        val v = props[key] as? String ?: return null
        return v.ifBlank { null }
    }

    fun contains(key: String) = get(key) != null

    val version: String get() = get("version") ?: commitHash
    val versionCode: Int get() = get("supersu.versionCode")!!.toInt()
    val stubVersion: String get() = get("supersu.stubVersion")!!
    val abiList: Set<String> get() {
        val abiList = get("abiList") ?: return defaultAbis
        return abiList.split(Regex("\\s*,\\s*")).toSet() intersect supportAbis
    }
}

fun Project.rootFile(path: String): File {
    val file = File(path)
    return if (file.isAbsolute) file
    else File(rootProject.file(".."), path)
}

// Resolve a path relative to the app/ Gradle root (rootProject)
fun Project.appFile(path: String): File {
    val file = File(path)
    return if (file.isAbsolute) file
    else File(rootProject.projectDir, path)
}

class SuperSuPlugin : Plugin<Project> {
    override fun apply(project: Project) = project.applyPlugin()

    private fun Project.applyPlugin() {
        props.clear()

        // Get gradle properties relevant to the root solution
        props.putAll(properties.filter { (key, _) -> key.startsWith("supersu.") })

        // Load config.prop
        val configPath: String? by this
        val configFile = rootFile(configPath ?: "config.prop")
        if (configFile.exists()) {
            configFile.inputStream().use {
                val config = Properties()
                config.load(it)
                // Remove properties that should be passed by commandline
                config.remove("abiList")
                props.putAll(config)
            }
        }

        // Commandline override
        findProperty("abiList")?.let { props.put("abiList", it) }

        commitHash = resolveCommitHash()
    }
}

// Read the commit hash directly from .git so configuration time never starts
// an external process. JGit used to invoke the system git binary for its
// system configuration, which made the Gradle configuration cache fail.
private fun Project.resolveCommitHash(): String {
    val gitEntry = rootFile(".git")
    if (!gitEntry.exists()) return ""

    val gitDir = if (gitEntry.isFile) {
        val line = gitEntry.readText().lineSequence()
            .firstOrNull { it.startsWith("gitdir:") }
            ?.substringAfter("gitdir:")?.trim()
            ?: return ""
        val dir = File(line)
        if (dir.isAbsolute) dir else File(gitEntry.parentFile, line)
    } else {
        gitEntry
    }

    val headFile = File(gitDir, "HEAD")
    if (!headFile.exists()) return ""
    val head = headFile.readText().trim()
    if (!head.startsWith("ref: ")) return head.take(8)

    val ref = head.substringAfter("ref: ").trim()
    val refFile = File(gitDir, ref)
    if (refFile.exists()) return refFile.readText().trim().take(8)

    val packedRefs = File(gitDir, "packed-refs")
    if (!packedRefs.exists()) return ""
    return packedRefs.readLines()
        .firstOrNull { it.endsWith(" $ref") }
        ?.substringBefore(' ')
        ?.take(8)
        ?: ""
}
