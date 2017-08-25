package local.john.netplay.listener

import java.io.PrintWriter
import java.net.Socket

fun main(args: Array<String>) {
    Socket("localhost", PORT).use {
        PrintWriter(it.getOutputStream(), true).println("root=\"C:\\Media\" list")
        val reader = it.getInputStream().bufferedReader()
        var line = reader.readLine()

        val temp = mutableListOf<Pair<CATEGORY, List<NetLibCommon>>>()

        while (line != null) {
            val (cat, list) = line.split("@")

            temp.add(parseCategory(cat, list.split("|")))

            line = reader.readLine()
        }
    }

}


enum class CATEGORY(val id: Int) {
    NONE(0),
    MOVIE(1),
    TV(2)
}

abstract class NetLibCommon {
    abstract val type: CATEGORY
}

data class mMovie(val title: String, val file: String, override val type: CATEGORY = CATEGORY.MOVIE) : NetLibCommon()
data class mShow(val episode: String, val season: String, val series: String, val file: String, override val type: CATEGORY = CATEGORY.TV) : NetLibCommon()

fun parseCategory(category: String, list: List<String>): Pair<CATEGORY, List<NetLibCommon>> =
        when(category) {
            "Movies" -> {
                CATEGORY.MOVIE to list.map { mMovie(it.replace(ROOT, "").split("\\")[1], it, CATEGORY.MOVIE) }
            }
            "TV" -> {
                CATEGORY.TV to list.map {
                    val (series, season, episode) = it.replace(ROOT + "TV\\", "").split("\\")

                    mShow(episode.substring(0, episode.lastIndexOf(".")), season, series, it)
                }
            }
            else -> { CATEGORY.NONE to emptyList() }
        }
