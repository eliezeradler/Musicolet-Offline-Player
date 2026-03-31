package com.example.offlineplayer
import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.example.offlineplayer.lyrics.LrcParser
import com.example.offlineplayer.lyrics.LyricsAdapter
import com.example.offlineplayer.service.PlaybackService

class LyricsActivity : AppCompatActivity() {
    private lateinit var tvLyricsStatus: TextView
    private lateinit var recyclerViewLyrics: RecyclerView
    private var lyricsAdapter: LyricsAdapter? = null
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isUserScrolling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lyrics)
        tvLyricsStatus = findViewById(R.id.tvLyricsStatus)
        recyclerViewLyrics = findViewById(R.id.recyclerViewLyrics)
        recyclerViewLyrics.layoutManager = LinearLayoutManager(this)
        recyclerViewLyrics.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) { super.onScrollStateChanged(recyclerView, newState); isUserScrolling = newState != RecyclerView.SCROLL_STATE_IDLE }
        })
    }

    override fun onStart() {
        super.onStart()
        mediaControllerFuture = MediaController.Builder(this, SessionToken(this, ComponentName(this, PlaybackService::class.java))).buildAsync()
        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
            loadLyrics()
            startSync()
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() { super.onStop(); mediaControllerFuture?.let { MediaController.releaseFuture(it) }; mediaController = null; handler.removeCallbacksAndMessages(null) }

    private fun loadLyrics() {
        val filePath = mediaController?.currentMediaItem?.localConfiguration?.uri?.path
        if (filePath != null) {
            val lyrics = LrcParser.parseLrcFile(filePath)
            if (lyrics != null && lyrics.isNotEmpty()) {
                tvLyricsStatus.visibility = TextView.GONE
                lyricsAdapter = LyricsAdapter(lyrics) { timeMs -> mediaController?.seekTo(timeMs) }
                recyclerViewLyrics.adapter = lyricsAdapter
            } else { tvLyricsStatus.text = "לא נמצא קובץ מילים (.lrc)" }
        } else { tvLyricsStatus.text = "אין שיר מתנגן" }
    }

    private fun startSync() {
        mediaController?.let { c ->
            if (c.isPlaying && lyricsAdapter != null) {
                val activeIndex = lyricsAdapter?.updateTime(c.currentPosition) ?: -1
                if (activeIndex != -1 && !isUserScrolling) recyclerViewLyrics.smoothScrollToPosition(activeIndex)
            }
        }
        handler.postDelayed({ startSync() }, 300)
    }
}


