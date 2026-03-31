package com.example.offlineplayer
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bumptech.glide.Glide
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.example.offlineplayer.service.PlaybackService

class NowPlayingActivity : AppCompatActivity() {
    private lateinit var tvNowTitle: TextView
    private lateinit var tvNowArtist: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnPrev: Button
    private lateinit var btnPlayPause: Button
    private lateinit var btnNext: Button
    private lateinit var btnLyrics: Button
    private lateinit var btnEditTags: Button
    private lateinit var ivAlbumArt: ImageView

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isUserSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)

        tvNowTitle = findViewById(R.id.tvNowTitle)
        tvNowArtist = findViewById(R.id.tvNowArtist)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        seekBar = findViewById(R.id.seekBar)
        btnPrev = findViewById(R.id.btnPrev)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnLyrics = findViewById(R.id.btnLyrics)
        btnEditTags = findViewById(R.id.btnEditTags)
        ivAlbumArt = findViewById(R.id.ivAlbumArt)

        tvNowTitle.isSelected = true
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        mediaControllerFuture = MediaController.Builder(this, SessionToken(this, ComponentName(this, PlaybackService::class.java))).buildAsync()
        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
            mediaController?.let { c ->
                c.addListener(playerListener)
                updateUI(c)
                updateProgress()
                btnPlayPause.requestFocus()
            }
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        super.onStop()
        mediaController?.removeListener(playerListener)
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        handler.removeCallbacksAndMessages(null)
    }

    private fun setupListeners() {
        btnPlayPause.setOnClickListener { mediaController?.let { if (it.isPlaying) it.pause() else it.play() } }
        btnNext.setOnClickListener { mediaController?.seekToNextMediaItem() }
        btnPrev.setOnClickListener { mediaController?.seekToPreviousMediaItem() }
        btnLyrics.setOnClickListener { startActivity(Intent(this, LyricsActivity::class.java)) }
        btnEditTags.setOnClickListener {
            mediaController?.currentMediaItem?.mediaId?.let { id ->
                startActivity(Intent(this, TagEditorActivity::class.java).putExtra("SONG_ID", id.toLong()))
            } ?: Toast.makeText(this, "אין שיר מתנגן", Toast.LENGTH_SHORT).show()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { if (fromUser) tvCurrentTime.text = formatTime(progress.toLong()) }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { isUserSeeking = true }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { isUserSeeking = false; seekBar?.let { mediaController?.seekTo(it.progress.toLong()) } }
        })
    }

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) { mediaController?.let { updateUI(it) } }
        override fun onIsPlayingChanged(isPlaying: Boolean) { btnPlayPause.text = if (isPlaying) "השהה" else "נגן" }
    }

    private fun updateUI(controller: MediaController) {
        tvNowTitle.text = controller.mediaMetadata.title ?: "ללא שם"
        tvNowArtist.text = controller.mediaMetadata.artist ?: "אמן לא ידוע"
        Glide.with(this).load(controller.currentMediaItem?.localConfiguration?.uri).placeholder(android.R.drawable.ic_media_play).into(ivAlbumArt)
        val duration = controller.duration
        if (duration > 0) { seekBar.max = duration.toInt(); tvTotalTime.text = formatTime(duration) }
        btnPlayPause.text = if (controller.isPlaying) "השהה" else "נגן"
    }

    private fun updateProgress() {
        mediaController?.let { c ->
            if (!isUserSeeking && c.duration > 0) {
                val pos = c.currentPosition
                seekBar.progress = pos.toInt()
                tvCurrentTime.text = formatTime(pos)
            }
        }
        handler.postDelayed({ updateProgress() }, 1000)
    }

    private fun formatTime(ms: Long): String {
        val totalSecs = ms / 1000
        return String.format("%02d:%02d", totalSecs / 60, totalSecs % 60)
    }
}


