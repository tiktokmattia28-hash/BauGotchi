package com.mio.baugotchi

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var dialogueText: TextView
    private lateinit var dogEmoji: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // Attiva il cane sentinella ad ogni sblocco schermo
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        applicationContext.registerReceiver(BootAndScreenReceiver(), filter)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 80, 50, 50)
            setBackgroundColor(Color.parseColor("#1A1A2E"))
            gravity = Gravity.CENTER
        }

        dogEmoji = TextView(this).apply {
            text = "🐶"
            textSize = 85f
            gravity = Gravity.CENTER
        }

        dialogueText = TextView(this).apply {
            text = "Bau! Controllo cosa fai sul tuo Redmi..."
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(20, 40, 20, 40)
            gravity = Gravity.CENTER
        }

        val btnPet = Button(this).apply {
            text = "✋ Accarezza il cane"
            setOnClickListener {
                playBark(1)
                dogEmoji.text = "🥰"
                dialogueText.text = "Bau! Bravo umano, continua così!"
            }
        }

        val btnFeed = Button(this).apply {
            text = "🍖 Dai croccantini"
            setOnClickListener {
                playBark(1)
                dogEmoji.text = "😋"
                dialogueText.text = "Gnam! Ottimi, ora però non stare troppo al telefono!"
            }
        }

        val btnReport = Button(this).apply {
            text = "📊 Analizza Fine Giornata"
            setOnClickListener { showDailyReport() }
        }

        layout.addView(dogEmoji)
        layout.addView(dialogueText)
        layout.addView(btnPet)
        layout.addView(btnFeed)
        layout.addView(btnReport)
        setContentView(layout)

        if (!hasUsagePermission()) {
            dialogueText.text = "Dammi il permesso 'Accesso ai dati di utilizzo' così ti controllo!"
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else {
            showDailyReport()
        }
    }

    private fun playBark(times: Int) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 90)
            toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 180)
            if (times == 2) {
                dogEmoji.postDelayed({ toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 220) }, 250)
            }
        } catch (e: Exception) {}
    }

    private fun hasUsagePermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun showDailyReport() {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            cal.timeInMillis,
            System.currentTimeMillis()
        )

        val topApp = stats
            ?.filter { !it.packageName.contains("launcher") && it.packageName != packageName }
            ?.maxByOrNull { it.totalTimeInForeground }

        if (topApp != null && topApp.totalTimeInForeground > 60_000) {
            val mins = topApp.totalTimeInForeground / (1000 * 60)
            val appName = topApp.packageName.substringAfterLast(".")
            playBark(2)
            dogEmoji.text = "🤬"
            dialogueText.text = "BAU BAU! Oggi hai buttato $mins minuti su '$appName'! Ma sei coglione?! Spengi sto cazzo di telefono e riposati!"
        } else {
            dogEmoji.text = "🐶"
            dialogueText.text = "Bau! Oggi sei stato bravo, hai usato poco il telefono!"
        }
    }
}
