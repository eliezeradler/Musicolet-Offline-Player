package com.example.offlineplayer
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.offlineplayer.data.AppDatabase
import com.example.offlineplayer.data.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

class TagEditorActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etArtist: EditText
    private lateinit var etAlbum: EditText
    private lateinit var btnSaveTags: Button
    private var currentSong: SongEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_editor)
        etTitle = findViewById(R.id.etTitle)
        etArtist = findViewById(R.id.etArtist)
        etAlbum = findViewById(R.id.etAlbum)
        btnSaveTags = findViewById(R.id.btnSaveTags)

        val songId = intent.getLongExtra("SONG_ID", -1L)
        if (songId != -1L) loadSongData(songId) else { Toast.makeText(this, "שגיאה", Toast.LENGTH_SHORT).show(); finish() }
        btnSaveTags.setOnClickListener { saveTagsToFile() }
    }

    private fun loadSongData(songId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            currentSong = AppDatabase.getDatabase(this@TagEditorActivity).songDao().getSongById(songId)
            withContext(Dispatchers.Main) {
                currentSong?.let { s -> etTitle.setText(s.title); etArtist.setText(s.artist); etAlbum.setText(s.album); etTitle.requestFocus() }
            }
        }
    }

    private fun saveTagsToFile() {
        val song = currentSong ?: return
        btnSaveTags.isEnabled = false; btnSaveTags.text = "שומר..."
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val audioFile = AudioFileIO.read(File(song.dataPath))
                val tag = audioFile.tagOrCreateAndSetDefault
                tag.setField(FieldKey.TITLE, etTitle.text.toString().trim())
                tag.setField(FieldKey.ARTIST, etArtist.text.toString().trim())
                tag.setField(FieldKey.ALBUM, etAlbum.text.toString().trim())
                audioFile.commit()

                AppDatabase.getDatabase(this@TagEditorActivity).songDao().updateSong(song.copy(
                    title = etTitle.text.toString().trim(), artist = etArtist.text.toString().trim(), album = etAlbum.text.toString().trim()
                ))
                withContext(Dispatchers.Main) { Toast.makeText(this@TagEditorActivity, "נשמר בהצלחה!", Toast.LENGTH_SHORT).show(); finish() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@TagEditorActivity, "שגיאה בשמירה", Toast.LENGTH_LONG).show(); btnSaveTags.isEnabled = true; btnSaveTags.text = "שמור שינויים בקובץ" }
            }
        }
    }
}


