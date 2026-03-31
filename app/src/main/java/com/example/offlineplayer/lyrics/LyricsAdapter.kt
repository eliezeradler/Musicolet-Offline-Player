package com.example.offlineplayer.lyrics
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.offlineplayer.R

class LyricsAdapter(private val lyrics: List<LyricLine>, private val onLineClicked: (Long) -> Unit) : RecyclerView.Adapter<LyricsAdapter.LyricViewHolder>() {
    private var currentLineIndex = -1

    class LyricViewHolder(view: View) : RecyclerView.ViewHolder(view) { val tvLyricLine: TextView = view.findViewById(R.id.tvLyricLine) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LyricViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_lyric, parent, false))

    override fun onBindViewHolder(holder: LyricViewHolder, position: Int) {
        val line = lyrics[position]
        holder.tvLyricLine.text = line.text
        if (position == currentLineIndex) {
            holder.tvLyricLine.setTextColor(Color.WHITE)
            holder.tvLyricLine.textSize = 22f
        } else {
            holder.tvLyricLine.setTextColor(Color.parseColor("#888888"))
            holder.tvLyricLine.textSize = 18f
        }
        holder.itemView.setOnClickListener { onLineClicked(line.timeMs) }
    }

    override fun getItemCount() = lyrics.size

    fun updateTime(timeMs: Long): Int {
        var newIndex = lyrics.indexOfLast { it.timeMs <= timeMs }
        if (newIndex == -1) newIndex = 0
        if (newIndex != currentLineIndex) {
            val oldIndex = currentLineIndex
            currentLineIndex = newIndex
            notifyItemChanged(oldIndex)
            notifyItemChanged(currentLineIndex)
            return currentLineIndex
        }
        return -1
    }
}


