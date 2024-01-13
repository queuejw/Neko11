package ru.dimon6018.neko11.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import ru.dimon6018.neko11.NekoApplication.Companion.getNekoTheme
import ru.dimon6018.neko11.R
import ru.dimon6018.neko11.ui.fragments.NekoLandFragment
import ru.dimon6018.neko11.ui.fragments.NekoLandFragment.Companion.shareCat
import ru.dimon6018.neko11.workers.Cat
import ru.dimon6018.neko11.workers.Cat.Companion.create
import ru.dimon6018.neko11.workers.PrefState

class NekoAboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getNekoTheme(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.neko_about_activity)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val toolbar = findViewById<Toolbar>(R.id.toolbarabout)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayUseLogoEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
        val github = findViewById<MaterialButton>(R.id.github_button)
        val tg = findViewById<MaterialButton>(R.id.telegram_button)
        github.setOnClickListener { openWeb(this, "https://github.com/queuejw/Neko11") }
        tg.setOnClickListener { openWeb(this, "https://t.me/nekoapp_news") }
        setupCatImage()
        val cord = findViewById<CoordinatorLayout>(R.id.coordinatorabout)
        ViewCompat.setOnApplyWindowInsetsListener(cord) { v: View, insets: WindowInsetsCompat ->
            val pB = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val tB = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, tB, 0, pB)
            WindowInsetsCompat.CONSUMED
        }
        val saveCats = findViewById<MaterialButton>(R.id.save_all_cats)
        saveCats.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(R.string.save_title_all)
                    .setMessage(R.string.save_all_cats_q)
                    .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                        saveAllCatsToGallery(this)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NekoLandFragment.STORAGE_PERM_REQUEST) {
            saveAllCatsToGalleryContinue(this)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupCatImage() {
        Thread {
            val cat: Cat = create(this)
            val bitmap = cat.createIconBitmap(NekoLandFragment.EXPORT_BITMAP_SIZE, NekoLandFragment.EXPORT_BITMAP_SIZE, 0)
            runOnUiThread {
                val imageViewCat = findViewById<ImageView>(R.id.imageViewCat)
                imageViewCat.setImageBitmap(bitmap)
            }
        }.start()
    }
    private fun saveAllCatsToGallery(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
                        NekoLandFragment.STORAGE_PERM_REQUEST)
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        NekoLandFragment.STORAGE_PERM_REQUEST)
                return
            }
            saveAllCatsToGalleryContinue(context)
        }
    }
    private fun saveAllCatsToGalleryContinue(context: Context) {
        val mPrefs = PrefState(context)
        Thread {
            var pos = 0
            val max = PrefState(context).cats.size
            val list =  mPrefs.cats
            while (pos != max) {
                val cat = list[pos]
                shareCat(this, cat, false)
                pos += 1
            }
            runOnUiThread {
                MaterialAlertDialogBuilder(this)
                        .setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_success))
                        .setTitle(R.string.save_title)
                        .setMessage(R.string.save_all_cats_done)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
            }
        }.start()
    }
    companion object {
        fun openWeb(activity: Activity, link: String?) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            activity.startActivity(intent)
        }
    }
}
