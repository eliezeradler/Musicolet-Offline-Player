package com.example.offlineplayer
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.example.offlineplayer.data.SongEntity
import com.example.offlineplayer.service.PlaybackService
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var layoutMiniPlayer: LinearLayout
    private lateinit var tvMiniTitle: TextView
    private lateinit var btnMiniPlayPause: Button
    private lateinit var ivMiniArt: ImageView

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        layoutMiniPlayer = findViewById(R.id.layoutMiniPlayer)
        tvMiniTitle = findViewById(R.id.tvMiniTitle)
        btnMiniPlayPause = findViewById(R.id.btnMiniPlayPause)
        ivMiniArt = findViewById(R.id.ivMiniArt)

        setupTabs()
        setupMiniPlayer()
    }

    private fun setupTabs() {
        val adapter = object : FragmentStateAdapter(this) {
            val titles = arrayOf("תיקיות", "הכי מושמעים", "שירים")
            override fun getItemCount() = titles.size
            override fun createFragment(pos: Int): Fragment = when(pos) {
                0 -> FoldersFragment()
                1 -> StatsFragment()
                2 -> SongsFragment()
                else -> SongsFragment()
            }
        }
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, pos -> tab.text = adapter.titles[pos] }.attach()
        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.view?.isFocusable = true
            tabLayout.getTabAt(i)?.view?.setBackgroundResource(R.drawable.dpad_focus_selector)
        }
        viewPager.setCurrentItem(2, false)
        tabLayout.post { tabLayout.getTabAt(2)?.view?.requestFocus() }
    }

    private fun setupMiniPlayer() {
        layoutMiniPlayer.setOnClickListener { startActivity(Intent(this, NowPlayingActivity::class.java)) }
        btnMiniPlayPause.setOnClickListener { mediaController?.let { if (it.isPlaying) it.pause() else it.play() } }
    }

    override fun onStart() {
        super.onStart()
        mediaControllerFuture = MediaController.Builder(this, SessionToken(this, ComponentName(this, PlaybackService::class.java))).buildAsync()
        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
            mediaController?.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) { updateMiniPlayer() }
                override fun onIsPlayingChanged(isPlaying: Boolean) { btnMiniPlayPause.text = if (isPlaying) "השהה" else "נגן" }
            })
            updateMiniPlayer()
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() { super.onStop(); mediaControllerFuture?.let { MediaController.releaseFuture(it) }; mediaController = null }

    private fun updateMiniPlayer() {
        val c = mediaController ?: return
        tvMiniTitle.text = c.mediaMetadata.title ?: "לא מתנגן"
        tvMiniTitle.isSelected = true
        btnMiniPlayPause.text = if (c.isPlaying) "השהה" else "נגן"
        Glide.with(this).load(c.currentMediaItem?.localConfiguration?.uri).placeholder(android.R.drawable.ic_media_play).into(ivMiniArt)
    }

    fun playSongFromList(song: SongEntity) {
        val c = mediaController ?: return
        val mi = MediaItem.Builder().setMediaId(song.id.toString()).setUri(Uri.fromFile(File(song.dataPath)))
            .setMediaMetadata(MediaMetadata.Builder().setTitle(song.title).setArtist(song.artist).build()).build()
        c.setMediaItem(mi)
        c.prepare(); c.play()
    }

    fun addToQueue(song: SongEntity, playNext: Boolean) {
        val c = mediaController ?: return
        val mi = MediaItem.Builder().setMediaId(song.id.toString()).setUri(Uri.fromFile(File(song.dataPath)))
            .setMediaMetadata(MediaMetadata.Builder().setTitle(song.title).setArtist(song.artist).build()).build()
        if (playNext) {
            c.addMediaItem(if (c.mediaItemCount > 0) c.currentMediaItemIndex + 1 else 0, mi)
        } else {
            c.addMediaItem(mi)
        }
        Toast.makeText(this, "התור עודכן", Toast.LENGTH_SHORT).show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_0) { startActivity(Intent(this, SettingsActivity::class.java)); return true }
        if (keyCode == KeyEvent.KEYCODE_POUND) { startActivity(Intent(this, EqualizerActivity::class.java)); return true }
        return super.onKeyDown(keyCode, event)
    }
}


