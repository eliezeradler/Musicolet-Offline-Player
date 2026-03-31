package com.example.offlineplayer
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.offlineplayer.data.AppDatabase
import com.example.offlineplayer.data.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        songAdapter = SongAdapter(emptyList(), 
            onItemClick = { (activity as? MainActivity)?.playSongFromList(it) },
            onItemLongClick = { showSongOptions(it) }
        )
        recyclerView.adapter = songAdapter
        loadSongs()
        return view
    }

    private fun loadSongs() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            val songs = db.songDao().getAllSongs()
            withContext(Dispatchers.Main) { songAdapter.updateData(songs) }
        }
    }

    private fun showSongOptions(song: SongEntity) {
        val options = arrayOf("נגן הבא", "הוסף לתור", "ערוך תגיות")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
        builder.setTitle(song.title).setItems(options) { _, w ->
            when (w) {
                0 -> (activity as? MainActivity)?.addToQueue(song, true)
                1 -> (activity as? MainActivity)?.addToQueue(song, false)
                2 -> startActivity(Intent(requireContext(), TagEditorActivity::class.java).putExtra("SONG_ID", song.id))
            }
        }.create().apply { show(); listView.requestFocus() }
    }
}


