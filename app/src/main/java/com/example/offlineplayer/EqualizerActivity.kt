package com.example.offlineplayer
import android.content.Context
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.offlineplayer.service.PlaybackService

class EqualizerActivity : AppCompatActivity() {
    private lateinit var switchEqualizer: Switch
    private lateinit var layoutBands: LinearLayout
    private var equalizer: Equalizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)
        switchEqualizer = findViewById(R.id.switchEqualizer)
        layoutBands = findViewById(R.id.layoutBands)
        setupEqualizer()
    }

    private fun setupEqualizer() {
        val sessionId = PlaybackService.currentAudioSessionId
        if (sessionId == androidx.media3.common.C.AUDIO_SESSION_ID_UNSET || sessionId == 0) {
            Toast.makeText(this, "יש לנגן שיר לפחות פעם אחת כדי להגדיר אקולייזר", Toast.LENGTH_LONG).show()
            switchEqualizer.isEnabled = false
            return
        }

        try { equalizer = Equalizer(0, sessionId) } catch (e: Exception) { Toast.makeText(this, "המכשיר אינו תומך באקולייזר", Toast.LENGTH_SHORT).show(); return }

        val prefs = getSharedPreferences("EQ_PREFS", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("EQ_ENABLED", false)
        equalizer?.enabled = isEnabled
        switchEqualizer.isChecked = isEnabled

        switchEqualizer.setOnCheckedChangeListener { _, isChecked ->
            equalizer?.enabled = isChecked
            prefs.edit().putBoolean("EQ_ENABLED", isChecked).apply()
        }

        val numberOfBands = equalizer?.numberOfBands ?: 0
        val minEQLevel = equalizer?.bandLevelRange?.get(0) ?: 0
        val maxEQLevel = equalizer?.bandLevelRange?.get(1) ?: 0

        for (i in 0 until numberOfBands) {
            val bandIndex = i.toShort()
            val freq = (equalizer?.getCenterFreq(bandIndex) ?: 0) / 1000
            val freqText = if (freq >= 1000) "${freq / 1000}kHz" else "${freq}Hz"

            val tvFreq = TextView(this).apply { text = freqText; setTextColor(android.graphics.Color.WHITE); gravity = Gravity.CENTER; setPadding(0, 16, 0, 8) }
            val seekBar = SeekBar(this).apply {
                max = maxEQLevel - minEQLevel
                val savedLevel = prefs.getInt("BAND_$bandIndex", equalizer?.getBandLevel(bandIndex)?.toInt() ?: 0)
                progress = savedLevel - minEQLevel
                equalizer?.setBandLevel(bandIndex, savedLevel.toShort())
                isFocusable = true
                setBackgroundResource(R.drawable.dpad_focus_selector)
                setPadding(16, 16, 16, 16)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            val newLevel = (progress + minEQLevel).toShort()
                            equalizer?.setBandLevel(bandIndex, newLevel)
                            prefs.edit().putInt("BAND_$bandIndex", newLevel.toInt()).apply()
                        }
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }
            layoutBands.addView(tvFreq)
            layoutBands.addView(seekBar)
        }
        switchEqualizer.requestFocus()
    }
}


