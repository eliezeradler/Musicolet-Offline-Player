package com.example.offlineplayer.lyrics
import java.io.File

data class LyricLine(val timeMs: Long, val text: String)

object LrcParser {
    fun parseLrcFile(audioFilePath: String): List<LyricLine>? {
        val audioFile = File(audioFilePath)
        val lrcFile = File(audioFile.parent, audioFile.nameWithoutExtension + ".lrc")
        if (!lrcFile.exists()) return null

        val lines = mutableListOf<LyricLine>()
        val regex = Regex("""\[(\d{1,3}):(\d{1,2})\.(\d{1,3})\](.*)""")
        try {
            lrcFile.forEachLine { line ->
                val match = regex.find(line)
                if (match != null) {
                    val min = match.groupValues[1].toLong()
                    val sec = match.groupValues[2].toLong()
                    val msStr = match.groupValues[3]
                    val ms = if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                    val text = match.groupValues[4].trim()
                    lines.add(LyricLine((min * 60 * 1000) + (sec * 1000) + ms, text))
                }
            }
        } catch (e: Exception) { return null }
        return lines.sortedBy { it.timeMs }
    }
}


