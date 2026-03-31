package com.example.offlineplayer
import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.offlineplayer.data.AppDatabase
import com.example.offlineplayer.scanner.MediaScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<Button>(R.id.btnScan).setOnClickListener { checkPermissionsAndScan() }
        findViewById<Button>(R.id.btnOpenEqualizer).setOnClickListener { startActivity(Intent(this, EqualizerActivity::class.java)) }
        findViewById<Button>(R.id.btnTimer15).setOnClickListener { setSleepTimer(15) }
        findViewById<Button>(R.id.btnTimer30).setOnClickListener { setSleepTimer(30) }
        findViewById<Button>(R.id.btnTimer1Song).setOnClickListener { setSongsTimer(1) }
        findViewById<Button>(R.id.btnTimer3Songs).setOnClickListener { setSongsTimer(3) }
        findViewById<Button>(R.id.btnTimerCancel).setOnClickListener { cancelSleepTimer() }
        
        findViewById<Button>(R.id.btnScan).requestFocus()
    }

    private fun setSleepTimer(minutes: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(this, 0, Intent(this, SleepTimerReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (minutes * 60 * 1000), pendingIntent)
        Toast.makeText(this, "טיימר הוגדר ל-$minutes דקות", Toast.LENGTH_SHORT).show()
    }

    private fun cancelSleepTimer() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(this, 0, Intent(this, SleepTimerReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
        Toast.makeText(this, "טיימר בוטל", Toast.LENGTH_SHORT).show()
    }

    private fun setSongsTimer(count: Int) {
        startService(Intent(this, com.example.offlineplayer.service.PlaybackService::class.java).apply { action = "ACTION_SET_SONGS_TIMER"; putExtra("SONGS_COUNT", count) })
        Toast.makeText(this, "יעצור אחרי $count שירים", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissionsAndScan() {
        val p = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED) startMediaScan() else ActivityCompat.requestPermissions(this, arrayOf(p), 123)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startMediaScan() else Toast.makeText(this, "נדרשת הרשאה", Toast.LENGTH_SHORT).show()
    }

    private fun startMediaScan() {
        Toast.makeText(this, "סורק...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.IO) {
            val songs = MediaScanner(this@SettingsActivity).scanLocalMedia()
            val db = AppDatabase.getDatabase(this@SettingsActivity)
            db.songDao().deleteAll()
            db.songDao().insertSongs(songs)
            withContext(Dispatchers.Main) { Toast.makeText(this@SettingsActivity, "סריקה הושלמה: ${songs.size} שירים", Toast.LENGTH_LONG).show() }
        }
    }
}