package ru.dimon6018.neko11.ui.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.dimon6018.neko11.BuildConfig
import ru.dimon6018.neko11.R
import ru.dimon6018.neko11.workers.PrefState
import kotlin.system.exitProcess

class NekoCrash: AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Neko11_Red)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.neko_crash)
        val model = "Model: " + Build.MODEL + "\n"
        val name = "Neko11 Ver: " + BuildConfig.VERSION_NAME + "\n"
        val brand = "Brand: " + Build.BRAND + "\n"
        val info: MaterialButton = findViewById(R.id.buttonRecoveryInfo)
        val restart: MaterialButton = findViewById(R.id.buttonRecoveryRestart)
        val clear: MaterialButton = findViewById(R.id.buttonRecoveryClear)
        val settings: MaterialButton = findViewById(R.id.buttonRecoverySettings)
        val error = "Detected critical error.\n " + model + brand + name + intent.extras?.getString("stacktrace")
        info.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.ic_warning)
                    .setMessage(error)
                    .setNegativeButton(R.string.copy
                    ) { _: DialogInterface?, _: Int -> copyError(error) }
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
        }
        restart.setOnClickListener {
            exitProcess(0)
        }
        clear.setOnClickListener {
            PrefState(this).clearPrefsWithoutCats()
            MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.ic_success)
                    .setMessage(R.string.success)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
        }
        settings.setOnClickListener {
            startActivity(Intent(this, NekoSettingsActivity::class.java))
        }
    }
    private fun copyError(code: String) {
        val clipbrd = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("promo", code)
        clipbrd.setPrimaryClip(clip)
    }
}