package com.example.offlineplayer
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.offlineplayer.data.AppDatabase
import com.example.offlineplayer.data.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoldersFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var songAdapter: SongAdapter
    private var isViewingSongs = false

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() { showFoldersList() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        folderAdapter = FolderAdapter(emptyList()) { openFolder(it) }
        songAdapter = SongAdapter(emptyList(), 
            onItemClick = { (activity as? MainActivity)?.playSongFromList(it) },
            onItemLongClick = { showSongOptions(it) }
        )

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
        showFoldersList()
        return view
    }

    private fun showFoldersList() {
        isViewingSongs = false
        backPressedCallback.isEnabled = false
        recyclerView.adapter = folderAdapter
        lifecycleScope.launch(Dispatchers.IO) {
            val folders = AppDatabase.getDatabase(requireContext()).songDao().getAllFolders()
            withContext(Dispatchers.Main) { folderAdapter.updateData(folders); recyclerView.requestFocus() }
        }
    }

    private fun openFolder(folderPath: String) {
        isViewingSongs = true
        backPressedCallback.isEnabled = true
        recyclerView.adapter = songAdapter
        lifecycleScope.launch(Dispatchers.IO) {
            val songs = AppDatabase.getDatabase(requireContext()).songDao().getSongsByFolder(folderPath)
            withContext(Dispatchers.Main) { songAdapter.updateData(songs); recyclerView.requestFocus() }
        }
    }

    private fun showSongOptions(song: SongEntity) {
        val options = arrayOf("נגן הבא", "הוסף לתור", "ערוך תגיות")
        androidx.appcompat.app.AlertDialog.Builder(requireContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(song.title).setItems(options) { _, w ->
                when (w) {
                    0 -> (activity as? MainActivity)?.addToQueue(song, true)
                    1 -> (activity as? MainActivity)?.addToQueue(song, false)
                    2 -> startActivity(Intent(requireContext(), TagEditorActivity::class.java).putExtra("SONG_ID", song.id))
                }
            }.create().apply { show(); listView.requestFocus() }
    }
}


