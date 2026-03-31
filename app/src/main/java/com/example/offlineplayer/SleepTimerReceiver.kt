package com.example.offlineplayer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.offlineplayer.service.PlaybackService

class SleepTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "טיימר השינה הסתיים. לילה טוב!", Toast.LENGTH_LONG).show()
        val stopIntent = Intent(context, PlaybackService::class.java).apply { action = "ACTION_SLEEP_TIMER_FINISH" }
        context.startService(stopIntent)
    }
}


