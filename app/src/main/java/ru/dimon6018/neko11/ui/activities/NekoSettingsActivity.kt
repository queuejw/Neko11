package ru.dimon6018.neko11.ui.activities

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import org.xmlpull.v1.XmlPullParserException
import ru.dimon6018.neko11.BuildConfig
import ru.dimon6018.neko11.NekoApplication.Companion.getNekoTheme
import ru.dimon6018.neko11.NekoGeneralActivity.Companion.showSnackBar
import ru.dimon6018.neko11.R
import ru.dimon6018.neko11.workers.BackupParser
import ru.dimon6018.neko11.workers.PrefState
import ru.dimon6018.neko11.workers.PrefState.PrefsListener
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

class NekoSettingsActivity : AppCompatActivity(), PrefsListener {
    private var nekoprefs: SharedPreferences? = null
    private var mPrefs: PrefState? = null
    private var isSave = false
    private var isRestore = false
    private var isDelete = false
    private var opensettingsbtn: MaterialButton? = null
    private var accentchoose: MaterialButton? = null
    private var chooseBackg: MaterialButton? = null

    private var standartsort: MaterialButton? = null
    private var namesort: MaterialButton? = null
    private var offsort: MaterialButton? = null

    private var backgoundNone: MaterialButton? = null
    private var backgoundCircle: MaterialButton? = null
    private var backgoundSquare: MaterialButton? = null

    private var details: MaterialButton? = null
    private var recovery: MaterialButton? = null
    private var whiteswitch: MaterialSwitch? = null
    private var linearcontrol: MaterialSwitch? = null
    private var dyncolor: MaterialSwitch? = null
    private var autowhiteswitch: MaterialSwitch? = null
    private var controlsFirst: MaterialSwitch? = null
    private var allowCatRun: MaterialSwitch? = null
    private var musicSwitch: MaterialSwitch? = null
    private var textMatch: MaterialSwitch? = null
    private var legacyControls: MaterialSwitch? = null
    private var legacyFood: MaterialSwitch? = null
    private var sortGroup: MaterialButtonToggleGroup? = null
    private var backgroundGroup: MaterialButtonToggleGroup? = null
    private var removeBack: MaterialTextView? = null
    private var limitSlider: Slider? = null
    private var catResizer: Slider? = null
    private var cord: LinearLayout? = null
    //remove in future
    private var ad: MaterialButton? = null

    private var pickMedia = registerForActivityResult<PickVisualMediaRequest, Uri>(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            Log.i("PhotoPicker", "Selected URI: $uri")
            mPrefs!!.setCustomBackgroundPath(getRealPathFromURI(uri, this))
            Toast.makeText(this, getString(R.string.background_setup_tip), Toast.LENGTH_LONG).show()
        } else {
            Log.i("PhotoPicker", "No media selected")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getNekoTheme(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.neko_settings_activity)
        mPrefs = PrefState(this)
        nekoprefs = getSharedPreferences(SETTINGS, MODE_PRIVATE)
        mPrefs!!.setListener(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbarset)
        cord = findViewById(R.id.cord)
        chooseBackg = findViewById(R.id.chooseBackground)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
        setupScreen()
        setupClickListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onPrefsChanged() {}

    override fun onPause() {
        super.onPause()
        mPrefs?.setListener(null)
    }
    override fun onResume() {
        super.onResume()
        mPrefs?.setListener(this)
    }

    private fun setupClickListeners() {
        sortGroup!!.addOnButtonCheckedListener { _: MaterialButtonToggleGroup?, checkedId: Int, isChecked: Boolean ->
            if (checkedId == R.id.sortname) {
                if (isChecked) {
                    mPrefs!!.sortState = 2
                }
            }
            if (checkedId == R.id.sortstandart) {
                if (isChecked) {
                    mPrefs!!.sortState = 1
                }
            }
            if (checkedId == R.id.sortoff) {
                if (isChecked) {
                    mPrefs!!.sortState = 0
                }
            }
        }
        backgroundGroup!!.addOnButtonCheckedListener { _: MaterialButtonToggleGroup?, checkedId: Int, isChecked: Boolean ->
            if (checkedId == R.id.cat_background_none) {
                if (isChecked) {
                    mPrefs!!.setIconBackground(0)
                }
            }
            if (checkedId == R.id.cat_background_circle) {
                if (isChecked) {
                    mPrefs!!.setIconBackground(1)
                }
            }
            if (checkedId == R.id.cat_background_square) {
                if (isChecked) {
                    mPrefs!!.setIconBackground(2)
                }
            }
        }
        accentchoose!!.setOnClickListener { v: View -> showMenu(v, R.menu.neko_colors) }
        recovery!!.setOnClickListener { startRecovery(this) }
        opensettingsbtn!!.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", this.packageName, null)
            intent.setData(uri)
            startActivity(intent)
        }
        whiteswitch!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val editor = nekoprefs!!.edit()
            if (isChecked) {
                editor.putInt("darktheme", 1)
                editor.apply()
                if (VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val uimanager = getSystemService(UI_MODE_SERVICE) as UiModeManager
                    uimanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            } else {
                editor.putInt("darktheme", 0)
                editor.apply()
                if (VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val uimanager = getSystemService(UI_MODE_SERVICE) as UiModeManager
                    uimanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
        autowhiteswitch!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val editor = nekoprefs!!.edit()
            if (isChecked) {
                whiteswitch!!.setEnabled(false)
                whiteswitch!!.setChecked(false)
                editor.putInt("darktheme", 2)
                editor.apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            } else {
                whiteswitch!!.setEnabled(true)
                editor.putInt("darktheme", 0)
                editor.apply()
            }
        }
        dyncolor!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val editor = nekoprefs!!.edit()
            if (isChecked) {
                editor.putInt("theme", 8)
                editor.apply()
            } else {
                editor.putInt("theme", 0)
                editor.apply()
            }
        }
        linearcontrol!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val editor = nekoprefs!!.edit()
            editor.putBoolean("linear_control", isChecked)
            editor.apply()
        }
        legacyControls!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val editor = nekoprefs!!.edit()
            editor.putBoolean("legacyGameplay", isChecked)
            editor.apply()
        }
        textMatch!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val editor = nekoprefs!!.edit()
            editor.putBoolean("coloredText", isChecked)
            editor.apply()
        }
        allowCatRun!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val editor = nekoprefs!!.edit()
            editor.putBoolean("gameplayFeature1", isChecked)
            editor.apply()
        }
        controlsFirst!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val editor = nekoprefs!!.edit()
            editor.putBoolean("controlsFirst", isChecked)
            editor.apply()
        }
        musicSwitch!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
           mPrefs!!.setMusic(isChecked)
        }
        legacyFood!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            mPrefs!!.setLegacyFood(isChecked)
        }
        limitSlider!!.addOnChangeListener(Slider.OnChangeListener { _: Slider?, value: Float, _: Boolean ->
            val valueNew = Math.round(value)
            mPrefs!!.catsInLineLimit = valueNew
        })
        catResizer!!.addOnChangeListener(Slider.OnChangeListener { _: Slider?, value: Float, _: Boolean ->
            val valueNew = Math.round(value)
            mPrefs!!.catIconSize = valueNew
        })
        details!!.setOnClickListener {
            val context: Context = if(VERSION.SDK_INT >= Build.VERSION_CODES.M) ContextThemeWrapper(this, getTheme()) else this
            val view = LayoutInflater.from(context).inflate(R.layout.neko_info_dialog, null)
            val androidtxt = view.findViewById<MaterialTextView>(R.id.neko_info_android_ver)
            val codetxt = view.findViewById<MaterialTextView>(R.id.neko_info_ver_code)
            val nametxt = view.findViewById<MaterialTextView>(R.id.neko_info_ver_name)
            nametxt.text = getString(R.string.neko_info_ver_name, BuildConfig.VERSION_NAME)
            codetxt.text = getString(R.string.neko_info_ver_code, BuildConfig.VERSION_CODE)
            androidtxt.text = getString(R.string.neko_info_android_ver, VERSION.SDK_INT)
            MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.details)
                    .setIcon(R.drawable.ic_info)
                    .setView(view)
                    .setNegativeButton(android.R.string.ok, null)
                    .show()
        }
        ad!!.setOnClickListener { NekoAboutActivity.openWeb(this, "https://github.com/queuejw/MetroPhoneLauncher") }
    }

    private fun startRecovery(context: Context) {
        isSave = false
        isRestore = false
        isDelete = false
        val bottomsheet = BottomSheetDialog(context)
        bottomsheet.setContentView(R.layout.neko_settings_bottomsheet)
        bottomsheet.dismissWithAnimation = true
        val bottomSheetInternal = bottomsheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        BottomSheetBehavior.from(bottomSheetInternal!!).peekHeight = context.resources.getDimensionPixelSize(R.dimen.bottomsheet)
        val savebtn = bottomSheetInternal.findViewById<MaterialButton>(R.id.save)
        val restorebtn = bottomSheetInternal.findViewById<MaterialButton>(R.id.restore)
        val deletebtn = bottomSheetInternal.findViewById<MaterialButton>(R.id.delete)
        val nextbtn = bottomSheetInternal.findViewById<MaterialButton>(R.id.next)
        val getperms = bottomSheetInternal.findViewById<MaterialTextView>(R.id.force_get_perms)
        savebtn.setOnClickListener {
            if (!isSave) {
                isSave = true
                isRestore = false
                isDelete = false
            } else {
                isSave = false
            }
            updateRecoveryScreen(nextbtn, savebtn, restorebtn, deletebtn)
        }
        restorebtn.setOnClickListener {
            if (!isRestore) {
                isRestore = true
                isSave = false
                isDelete = false
            } else {
                isRestore = false
            }
            updateRecoveryScreen(nextbtn, savebtn, restorebtn, deletebtn)
        }
        deletebtn.setOnClickListener {
            if (!isDelete) {
                isDelete = true
                isRestore = false
                isSave = false
            } else {
                isDelete = false
            }
            updateRecoveryScreen(nextbtn, savebtn, restorebtn, deletebtn)
        }
        nextbtn.setOnClickListener {
            if (isSave) {
                bottomsheet.setContentView(R.layout.neko_settings_bottomsheet_save_file)
                savePrefs()
                object : Thread() {
                    override fun run() {
                        try {
                            sleep(2000)
                            bottomsheet.dismiss()
                        } catch (e: InterruptedException) {
                            throw RuntimeException(e)
                        }
                    }
                }.start()
            }
            if (isRestore) {
                bottomsheet.setContentView(R.layout.neko_settings_bottomsheet_select_file)
                val select = bottomSheetInternal.findViewById<MaterialButton>(R.id.choose)
                select.setOnClickListener {
                    openFile()
                    bottomsheet.dismiss()
                }
            }
            if (isDelete) {
                bottomsheet.setContentView(R.layout.neko_settings_bottomsheet_process)
                object : Thread() {
                    override fun run() {
                        try {
                            mPrefs!!.wipeData()
                            nekoprefs!!.all.clear()
                            sleep(1200)
                            bottomsheet.dismiss()
                        } catch (e: InterruptedException) {
                            throw RuntimeException(e)
                        }
                        bottomSheetInternal.post {
                            MaterialAlertDialogBuilder(context)
                                    .setTitle(R.string.backup_title)
                                    .setIcon(R.drawable.ic_backup_done)
                                    .setMessage(R.string.task_success)
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show()
                        }
                    }
                }.start()
            }
        }
        getperms.setOnClickListener { checkPerms() }
        bottomsheet.show()
    }

    private fun updateRecoveryScreen(view: View, view2: View, view3: View, view4: View) {
        view.setEnabled(isSave || isDelete || isRestore)
        val color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondaryContainer, Color.GREEN)
        val transparent = ContextCompat.getColor(this, android.R.color.transparent)
        if (isSave) {
            view2.setBackgroundColor(color)
        } else {
            view2.setBackgroundColor(transparent)
        }
        if (isRestore) {
            view3.setBackgroundColor(color)
        } else {
            view3.setBackgroundColor(transparent)
        }
        if (isDelete) {
            view4.setBackgroundColor(color)
        } else {
            view4.setBackgroundColor(transparent)
        }
    }

    @SuppressLint("SdCardPath")
    fun savePrefs() {
        val ff: File = if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File(this.dataDir
                    .toString() + "/shared_prefs/mPrefs.xml")
        } else {
            File("/data/data/"
                    + this.packageName
                    + "/shared_prefs/mPrefs.xml")
        }
        Log.e("backup", ff.path)
        try {
            val f1 = File(ff.path)
            val f2 = File(Environment.getExternalStorageDirectory().toString() + "/Neko11mBackup.xml")
            val `in`: InputStream = FileInputStream(f1)
            val out: OutputStream = FileOutputStream(f2)
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            `in`.close()
            out.close()
            runOnUiThread {
                showSaveDialog(true, "")
            }
        } catch (ex: IOException) {
            showSaveDialog(false, ex.toString())
        }
    }
    private fun showSaveDialog(success: Boolean, ex: String) {
        if (success) {
            MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.backup_title)
                    .setIcon(R.drawable.ic_backup_done)
                    .setMessage(R.string.backup_done)
                    .setNegativeButton(android.R.string.ok, null)
                    .show()
        } else {
            MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.backup_title)
                    .setIcon(R.drawable.ic_backup_error)
                    .setMessage(R.string.backup_failed)
                    .setNegativeButton(android.R.string.ok, null)
                    .setPositiveButton(R.string.details) { _: DialogInterface?, _: Int ->
                        MaterialAlertDialogBuilder(this)
                                .setMessage("See: $ex")
                                .setNegativeButton(android.R.string.ok, null)
                                .show()
                    }
                    .show()
        }
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("text/xml")
        startActivityForResult(intent, 200)
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int,
                                         resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 200 && resultCode == RESULT_OK && resultData != null) {
            val context: Context = this
            val bottomsheet = BottomSheetDialog(context)
            bottomsheet.setContentView(R.layout.neko_settings_bottomsheet_process)
            bottomsheet.dismissWithAnimation = true
            val bottomSheetInternal = bottomsheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheetInternal!!).peekHeight = context.resources.getDimensionPixelSize(R.dimen.bottomsheet)
            bottomsheet.show()
            try {
                mPrefs!!.wipeData()
                val uri = resultData.data
                val file = File(getRealPathFromURI(uri, this))
                val `is`: InputStream = BufferedInputStream(FileInputStream(file))
                val backuper = BackupParser()
                backuper.parse(`is`, this)
                `is`.close()
                object : Thread() {
                    override fun run() {
                        try {
                            sleep(1100)
                            bottomsheet.dismiss()
                        } catch (e: InterruptedException) {
                            throw RuntimeException(e)
                        }
                        bottomSheetInternal.post {
                            MaterialAlertDialogBuilder(context)
                                    .setTitle(R.string.backup_title)
                                    .setIcon(R.drawable.ic_backup_done)
                                    .setMessage(R.string.restore_done)
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show()
                        }
                    }
                }.start()
            } catch (e: IOException) {
                bottomsheet.dismiss()
                MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.backup_title)
                        .setIcon(R.drawable.ic_backup_error)
                        .setMessage(R.string.restore_failed)
                        .setNegativeButton(android.R.string.ok, null)
                        .setPositiveButton(R.string.details) { _: DialogInterface?, _: Int ->
                            MaterialAlertDialogBuilder(this)
                                    .setMessage("See: $e")
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show()
                        }
                        .show()
                throw RuntimeException(e)
            } catch (e: XmlPullParserException) {
                bottomsheet.dismiss()
                MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.backup_title)
                        .setIcon(R.drawable.ic_backup_error)
                        .setMessage(R.string.restore_failed)
                        .setNegativeButton(android.R.string.ok, null)
                        .setPositiveButton(R.string.details) { _: DialogInterface?, _: Int ->
                            MaterialAlertDialogBuilder(this)
                                    .setMessage("See: $e")
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show()
                        }
                        .show()
                throw RuntimeException(e)
            }
        }
        showSnackBar(getString(R.string.task_success), 5, cord)
    }

    private fun checkPerms() {
        val i = Intent()
        val uri = Uri.fromParts("package", packageName, null)
        if (VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                i.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            }
        } else {
            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.setData(uri)
            startActivity(i)
        }
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        val editor = nekoprefs!!.edit()
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.pink_theme -> {
                    item = 1
                    editor.putInt("theme", item)
                }
                R.id.red_theme -> {
                    item = 2
                    editor.putInt("theme", item)
                }
                R.id.orange_theme -> {
                    item = 3
                    editor.putInt("theme", item)
                }
                R.id.green_theme -> {
                    item = 4
                    editor.putInt("theme", item)
                }
                R.id.lime_theme -> {
                    item = 5
                    editor.putInt("theme", item)
                }
                R.id.aqua_theme -> {
                    item = 6
                    editor.putInt("theme", item)
                }
                R.id.blue_theme -> {
                    item = 7
                    editor.putInt("theme", item)
                }
                R.id.purple_theme -> {
                    item = 0
                    editor.putInt("theme", item)
                }
            }
            editor.apply()
            showSnackBar(getString(R.string.themeChanged), Snackbar.LENGTH_LONG, v)
            true
        }
        popup.show()
    }
    private fun setupScreen() {
        opensettingsbtn = findViewById(R.id.opensettingsbtn)
        whiteswitch = findViewById(R.id.white_switch)
        linearcontrol = findViewById(R.id.linear_controls_enable)
        dyncolor = findViewById(R.id.dynamic_color)
        autowhiteswitch = findViewById(R.id.white_switch_auto)
        accentchoose = findViewById(R.id.choosetheme)
        sortGroup = findViewById(R.id.sortgroup)
        standartsort = findViewById(R.id.sortstandart)
        backgroundGroup = findViewById(R.id.cat_background_group)
        backgoundNone = findViewById(R.id.cat_background_none)
        backgoundCircle = findViewById(R.id.cat_background_circle)
        backgoundSquare = findViewById(R.id.cat_background_square)
        namesort = findViewById(R.id.sortname)
        controlsFirst = findViewById(R.id.is_first_controls)
        allowCatRun = findViewById(R.id.allow_cat_delete)
        offsort = findViewById(R.id.sortoff)
        limitSlider = findViewById(R.id.max_cat_line_slider)
        catResizer = findViewById(R.id.cat_size_slider)
        details = findViewById(R.id.get_neko_info)
        recovery = findViewById(R.id.recovery_btn)
        ad = findViewById(R.id.ad_btn)
        removeBack = findViewById(R.id.remove_background)
        textMatch = findViewById(R.id.text_matches_theme)
        legacyControls = findViewById(R.id.legacy_controls)
        musicSwitch = findViewById(R.id.musicController)
        legacyFood = findViewById(R.id.legacy_food)
        val theme = nekoprefs!!.getInt("theme", 0)
        val darkenabled = nekoprefs!!.getInt("darktheme", 0)
        val isControlsLinear = nekoprefs!!.getBoolean("linear_control", false)
        val isControlsFirst = nekoprefs!!.getBoolean("controlsFirst", false)
        val isCatCanRun = nekoprefs!!.getBoolean("gameplayFeature1", true)
        val legacyGameplay = nekoprefs!!.getBoolean("legacyGameplay", false)
        val coloredText = nekoprefs!!.getBoolean("coloredText", false)
        removeBack!!.setOnClickListener {
            mPrefs!!.setCustomBackgroundPath("")
            showSnackBar(getString(R.string.themeChanged), Snackbar.LENGTH_LONG, removeBack)
        }
        dyncolor!!.setEnabled(VERSION.SDK_INT >= Build.VERSION_CODES.S)
        linearcontrol!!.setChecked(isControlsLinear)
        dyncolor!!.isChecked = theme == 8
        legacyControls!!.isChecked = legacyGameplay
        controlsFirst!!.isChecked = isControlsFirst
        musicSwitch!!.isChecked = mPrefs!!.isMusicEnabled()
        legacyFood!!.isChecked = mPrefs!!.isLegacyFoodEnabled()
        allowCatRun!!.isChecked = isCatCanRun
        textMatch!!.isChecked = coloredText
        limitSlider!!.value = mPrefs!!.catsInLineLimit.toFloat()
        catResizer!!.value = mPrefs!!.catIconSize.toFloat()
        when (darkenabled) {
            0 -> {
                whiteswitch?.isChecked = false
                autowhiteswitch?.isChecked = true
            }
            1 -> whiteswitch?.isChecked = true
            2 -> {
                autowhiteswitch?.isChecked = true
                whiteswitch?.isChecked = false
            }
            else -> whiteswitch?.isChecked = false
        }
        when (mPrefs!!.sortState) {
            2 -> namesort!!.isChecked = true
            1 -> standartsort!!.isChecked = true
            0 -> offsort!!.isChecked = true
        }
        when (mPrefs!!.getIconBackground()) {
            2 -> backgoundSquare!!.isChecked = true
            1 -> backgoundCircle!!.isChecked = true
            0 -> backgoundNone!!.isChecked = true
        }
        chooseBackg!!.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    .build())
        }
    }
    companion object {
        const val SETTINGS = "SettingsPrefs"
        var item = 0

        //Thanks https://stackoverflow.com/a/72444629
        private fun getRealPathFromURI(uri: Uri?, context: Context): String {
            val returnCursor = context.contentResolver.query(uri!!, null, null, null, null)
            val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            val file = File(context.filesDir, name)
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                var read: Int
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable = inputStream!!.available()

                val bufferSize = min(bytesAvailable, maxBufferSize)
                val buffers = ByteArray(bufferSize)
                while (inputStream.read(buffers).also { read = it } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                inputStream.close()
                outputStream.close()
                returnCursor.close()
            } catch (e: Exception) {
                Log.e("ParseURI", "Exception. See: $e")
            }
            return file.path
        }
        fun showRestoreFailedDialog(context: Context, exception: String) {
            MaterialAlertDialogBuilder(context)
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(R.string.error)
                    .setMessage(context.getString(R.string.error_message, exception))
                    .setNegativeButton(android.R.string.ok, null)
                    .show()
        }
    }
}