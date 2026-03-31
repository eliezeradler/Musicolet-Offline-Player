package com.example.offlineplayer.service
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.offlineplayer.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private var songsRemaining: Int = -1

    companion object { var currentAudioSessionId: Int = C.AUDIO_SESSION_ID_UNSET }

    override fun onCreate() {
        super.onCreate()
        val audioAttributes = AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build()
        player = ExoPlayer.Builder(this).setAudioAttributes(audioAttributes, true).setHandleAudioBecomingNoisy(true).build()

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) { currentAudioSessionId = audioSessionId }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    updateSongStats(mediaItem?.mediaId)
                    if (songsRemaining > 0) {
                        songsRemaining--
                        if (songsRemaining == 0) { player.pause(); songsRemaining = -1 }
                    }
                }
            }
        })
        mediaSession = MediaSession.Builder(this, player).build()
    }

    private fun updateSongStats(songId: String?) {
        val id = songId?.toLongOrNull() ?: return
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch { db.songDao().incrementPlayCount(id, System.currentTimeMillis()) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_SLEEP_TIMER_FINISH" -> player.pause()
            "ACTION_SET_SONGS_TIMER" -> songsRemaining = intent.getIntExtra("SONGS_COUNT", -1)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession
    override fun onDestroy() { mediaSession?.run { player.release(); release(); mediaSession = null }; currentAudioSessionId = C.AUDIO_SESSION_ID_UNSET; super.onDestroy() }
    override fun onTaskRemoved(rootIntent: Intent?) { if (mediaSession?.player?.playWhenReady == false) stopSelf() }
}


