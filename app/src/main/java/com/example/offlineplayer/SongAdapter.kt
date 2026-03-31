package com.example.offlineplayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.offlineplayer.data.SongEntity

class SongAdapter(
    private var songsList: List<SongEntity>,
    private val onItemClick: (SongEntity) -> Unit,
    private val onItemLongClick: (SongEntity) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private var filteredList = songsList

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvSongTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvSongArtist)
        val ivArt: ImageView = view.findViewById(R.id.ivSongArt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SongViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false))

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = filteredList[position]
        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist
        Glide.with(holder.itemView.context).load(song.dataPath).placeholder(android.R.drawable.ic_media_play).into(holder.ivArt)
        
        holder.itemView.setOnClickListener { onItemClick(song) }
        holder.itemView.setOnLongClickListener { onItemLongClick(song); true }
        holder.itemView.setOnFocusChangeListener { _, hasFocus -> holder.tvTitle.isSelected = hasFocus }
    }
    
    override fun getItemCount() = filteredList.size
    fun updateData(newSongs: List<SongEntity>) { songsList = newSongs; filteredList = newSongs; notifyDataSetChanged() }
    
    fun filterT9(query: String) {
        filteredList = if (query.isEmpty()) songsList else songsList.filter { com.example.offlineplayer.search.T9Search.match(query, it.title) || com.example.offlineplayer.search.T9Search.match(query, it.artist) }
        notifyDataSetChanged()
    }
}


