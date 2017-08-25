package local.john.netplay.listener

import java.io.BufferedReader
import java.io.File
import java.io.PrintWriter
import java.net.*

internal val PORT                       = 5051                                                                          // Port number to listen on
internal val ROOT                       = "C:\\Media"                                                                   // Root folder of NetLib library
private val  PATH_TO_VLC                = "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe"                             // Full path to VLC executable

private val  ALLOWED_FILE_EXTENSIONS    = listOf("avi", "mkv", "mp4", "mp3")                                            // File extensions that are able to be played by VLC

fun main(args: Array<String>) {
    Listener()
}

private class Listener(port: Int = PORT, var root: String = ROOT) {
    init {
        try {
            ServerSocket(port).use {
                println("Listening on port $port")

                while (true) {
                    it.accept().use {
                        val writer = PrintWriter(it.getOutputStream(), true)
                        val reader = BufferedReader(it.getInputStream().bufferedReader())
                        val raw = try { reader.readLine().toString() } catch (e: NullPointerException) { "" }
                        val resp = try { raw.split(" ") } catch (e: IllegalArgumentException) { listOf(raw) }

                        var file = ""
                        var list = false
                        var play = false

                        resp.forEach {
                            val cmd: String
                            var args: String? = null

                            if (it.contains("=")) {
                                val temp = it.split("=")
                                cmd = temp[0]
                                args = temp[1]
                            } else cmd = it

                            when (cmd) {
                                "root" -> { root = args?.trim('\"', '\'') ?: root }
                                "file" -> { file = args?.replace("`", " ") ?: file }
                                "list" -> { list = true }
                                "play" -> { play = true }
                            }
                        }

                        // Returns a list of Media types as Pair<String, List<String>>
                        if (list) {
                            try {
                                File(root).listFiles().map { cat -> cat.name to getListOfFiles(cat) }
                                        .forEach { (cat, lists) -> writer.println("$cat@${try {
                                            lists.reduce { a, b -> "$a|$b" } } catch (e: Exception) { "" }}") }
                            } catch (e: NullPointerException) {println("Unable to access root folder: '$root'")}
                        }

                        // Plays a given file, or ignores if is not allowed extension
                        else if (play)
                            if(ALLOWED_FILE_EXTENSIONS.contains(file.substring(file.lastIndexOf(".") + 1, file.length)))
                                Runtime.getRuntime().exec("$PATH_TO_VLC \"$file\" -f")
                            else
                                println("Unrecognized file extension: $file")
                    }
                }
            }
        } catch (e: BindException) { println("Address already in use!") }
    }
}

private fun getListOfFiles(file: File): List<String> {
    val tempList = mutableListOf<String>()

    file.listFiles().forEach {
        if(it.extension != "srt" && it.extension != "db") {                                                             // Ignore .srt and .db files @TODO: come up with a better solution.
            if (it.isDirectory) tempList.addAll(getListOfFiles(it))
            else if (it.isFile) tempList.add(it.absolutePath)
        }
    }

    return tempList
}
