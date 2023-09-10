/*
 * Copyright (C) 2023 Dmitry Frolkov <dimon6018t@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.dimon6018.neko11

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import ru.dimon6018.neko11.activation.NekoActivationActivity
import ru.dimon6018.neko11.controls.CatControlsFragment
import ru.dimon6018.neko11.ui.activities.NekoAboutActivity
import ru.dimon6018.neko11.ui.activities.NekoAchievementsActivity
import ru.dimon6018.neko11.ui.activities.NekoSettingsActivity
import ru.dimon6018.neko11.ui.fragments.NekoLandFragment
import ru.dimon6018.neko11.workers.Cat
import ru.dimon6018.neko11.workers.NekoWorker
import ru.dimon6018.neko11.workers.PrefState
import ru.dimon6018.neko11.workers.PrefState.PrefsListener


class NekoGeneralActivity : AppCompatActivity(), PrefsListener {

    private var nekoprefs: SharedPreferences? = null
    private var mPrefs: PrefState? = null
    var navbar: BottomNavigationView? = null
    private var promo: String? = null
    private var state = 0
    private var viewPager: ViewPager2? = null
    private var pagerAdapter: FragmentStateAdapter? = null
    private var needWelcomeDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val nekoprefs = getSharedPreferences(NekoSettingsActivity.SETTINGS, MODE_PRIVATE)
            setupState()
            Thread {
                setupDarkMode()
                setTheme(NekoApplication.getNekoTheme(this))
            runOnUiThread {
                setupNavbarListener()
                if(needWelcomeDialog) welcomeDialog()
                if(getAndroidV()) androidVDialog()
            }
            }.start()
            super.onCreate(savedInstanceState)
            setContentView(R.layout.neko_activity)
            viewPager = findViewById(R.id.pager)
            pagerAdapter = NekoAdapter(this)
            viewPager?.adapter = pagerAdapter
            mPrefs = PrefState(this)
            mPrefs!!.setListener(this)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            if (supportActionBar != null) {
                supportActionBar!!.setDisplayUseLogoEnabled(true)
            }
            navbar = findViewById(R.id.navigation)
            window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
            if (!nekoprefs!!.getBoolean("controlsFirst", false)) {
                navbar?.selectedItemId = R.id.collection
            } else {
                navbar?.selectedItemId = R.id.controls
            }
        viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) navbar?.selectedItemId = R.id.collection else navbar?.selectedItemId = R.id.controls
            }
        })
    }
    private fun getAndroidV(): Boolean {
        return (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1)
    }
    private fun androidVDialog() {
            MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.ic_warning)
                    .setMessage(R.string.unsupported_android)
                    .setNegativeButton(android.R.string.ok, null)
                    .show()
    }

    override fun onPrefsChanged() {}

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        (menu as MenuBuilder).setOptionalIconsVisible(true)
        menuInflater.inflate(R.menu.neko_general_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("MissingInflatedId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.aboutMenuId -> {
                if (!debug) {
                    startActivity(Intent(this@NekoGeneralActivity, NekoAboutActivity::class.java))
                } else {
                    var cat: Cat?
                    for (i in 0..10) {
                        cat = NekoWorker.newRandomCat(this, mPrefs)
                        mPrefs!!.addCat(cat)
                        mPrefs!!.addNCoins(666)
                    }
                }
                return true
            }
            R.id.achievementsMenuId -> {
                startActivity(Intent(this@NekoGeneralActivity, NekoAchievementsActivity::class.java))
                return true
            }

            R.id.promoMenuId -> {
                showPromoDialog()
                return true
            }

            R.id.settingsMenuId -> {
                startActivity(Intent(this@NekoGeneralActivity, NekoSettingsActivity::class.java))
                return true
            }
            R.id.myInfoMenuId -> {
                val context: Context = ContextThemeWrapper(this, theme)
                val view = LayoutInflater.from(context).inflate(R.layout.neko_stats_dialog, null)
                val stat1: MaterialTextView = view.findViewById(R.id.cat_count_stat)
                stat1.text = getString(R.string.stat_1, mPrefs?.CatsAllTime())
                val stat2: MaterialTextView = view.findViewById(R.id.booster_count_stat)
                stat2.text = getString(R.string.stat_2, mPrefs?.boostersUseAllTime())
                val stat3: MaterialTextView = view.findViewById(R.id.water_stat)
                stat3.text = getString(R.string.stat_3, mPrefs?.WaterMl())
                val stat4: MaterialTextView = view.findViewById(R.id.cat_actions_use_count)
                stat4.text = getString(R.string.stat_4, mPrefs?.catActionsUseAllTime())
                MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.my_stats)
                        .setIcon(R.drawable.ic_help)
                        .setView(view)
                        .setNegativeButton(android.R.string.ok, null)
                        .show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    public override fun onDestroy() {
        mPrefs!!.setListener(null)
        super.onDestroy()
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
    }

    private fun setupDarkMode() {
        when (nekoprefs!!.getInt("darktheme", 0)) {
            1 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val uimanager = getSystemService(UI_MODE_SERVICE) as UiModeManager
                    uimanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }

            2 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val uimanager = getSystemService(UI_MODE_SERVICE) as UiModeManager
                    uimanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val uimanager = getSystemService(UI_MODE_SERVICE) as UiModeManager
                    uimanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }

    private fun showPromoDialog() {
        val context: Context
        context = ContextThemeWrapper(this, theme)
        val view = LayoutInflater.from(context).inflate(R.layout.edit_text_promo, null)
        val text = view.findViewById<EditText>(R.id.editpromo)
        MaterialAlertDialogBuilder(context)
                .setTitle(R.string.promo)
                .setIcon(R.drawable.key)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { dialog: DialogInterface?, which: Int ->
                    promo = text.text.toString()
                    checkPromo(promo!!)
                }.show()
    }

    private fun checkPromo(promo: String) {
        val code1availability = nekoprefs!!.getBoolean("code1availability", true)
        val code2availability = nekoprefs!!.getBoolean("code2availability", true)
        val code3availability = nekoprefs!!.getBoolean("code3availability", true)
        val code4availability = nekoprefs!!.getBoolean("code4availability", true)
        val code5availability = nekoprefs!!.getBoolean("code5availability", true)
        val editor = nekoprefs!!.edit()
        if (promo == nekoprefs!!.getString("code1", "")) {
            if (code1availability) {
                showSnackBar(getString(R.string.code_is_true), Snackbar.LENGTH_LONG, navbar)
                mPrefs!!.addNCoins(202)
                mPrefs!!.addLuckyBooster(1)
                mPrefs!!.addMoodBooster(1)
                editor.putBoolean("code1availability", false)
            } else {
                showSnackBar(getString(R.string.code_is_false), Snackbar.LENGTH_LONG, navbar)
            }
        } else if (promo == nekoprefs!!.getString("code2", "")) {
            if (code2availability) {
                showSnackBar(getString(R.string.code_is_true), Snackbar.LENGTH_LONG, navbar)
                mPrefs!!.addNCoins(400)
                mPrefs!!.addMoodBooster(3)
                mPrefs!!.addLuckyBooster(5)
                editor.putBoolean("code2availability", false)
            } else {
                showSnackBar(getString(R.string.code_is_false), Snackbar.LENGTH_LONG, navbar)
            }
        } else if (promo == nekoprefs!!.getString("code3", "")) {
            if (code3availability) {
                showSnackBar(getString(R.string.code_is_true), Snackbar.LENGTH_LONG, navbar)
                mPrefs!!.addNCoins(1000)
                mPrefs!!.addMoodBooster(6)
                mPrefs!!.addLuckyBooster(7)
                editor.putBoolean("code3availability", false)
            } else {
                showSnackBar(getString(R.string.code_is_false), Snackbar.LENGTH_LONG, navbar)
            }
        } else if (promo == nekoprefs!!.getString("code4", "")) {
            if (code4availability) {
                showSnackBar(getString(R.string.code_is_true), Snackbar.LENGTH_LONG, navbar)
                mPrefs!!.addNCoins(10000)
                mPrefs!!.addMoodBooster(15)
                mPrefs!!.addLuckyBooster(25)
                editor.putBoolean("code4availability", false)
            } else {
                showSnackBar(getString(R.string.code_is_false), Snackbar.LENGTH_LONG, navbar)
            }
        } else if (promo == nekoprefs!!.getString("code5", "")) {
            if (code5availability) {
                showSnackBar(getString(R.string.code_is_true), Snackbar.LENGTH_LONG, navbar)
                mPrefs!!.addNCoins(700)
                editor.putBoolean("code5availability", false)
            } else {
                showSnackBar(getString(R.string.code_is_false), Snackbar.LENGTH_LONG, navbar)
            }
        } else if (promo == "hello") {
            showSnackBar("Hi!", Snackbar.LENGTH_SHORT, navbar)
        } else if (promo == "null" || promo == "none" || promo == "") {
            showSnackBar("no.", Snackbar.LENGTH_SHORT, navbar)
        } else if (promo == "how are you") {
            showSnackBar("please help me!!!!", Snackbar.LENGTH_SHORT, navbar)
        } else {
            showSnackBar(getString(R.string.wrong_code), Snackbar.LENGTH_LONG, navbar)
        }
        editor.apply()
    }

    private fun setupNavbarListener() {
        navbar!!.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.collection) {
                viewPager!!.currentItem = 0
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.controls) {
                if (checkState() != -1) {
                    viewPager!!.currentItem = 1
                    navbar?.animate()?.translationY(0f)?.setDuration(200);
                } else {
                    viewPager!!.currentItem = 1
                    val editor = nekoprefs!!.edit()
                    editor.putInt("state", 0)
                    editor.apply()
                    navbar?.animate()?.translationY(0f)?.setDuration(200);
                    MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.app_name_neko)
                            .setIcon(R.drawable.ic_fullcat_icon)
                            .setMessage(R.string.welcome_dialog_part3)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok) { dialog: DialogInterface?, id: Int ->
                                showSnackBar(getString(R.string.welcome_dialog_final), Snackbar.LENGTH_LONG, navbar)
                                setCurrentTheme(0)
                            }.show()
                }
                val editor = nekoprefs!!.edit()
                editor.putInt("state", 0)
                editor.apply()
                return@setOnItemSelectedListener true
            }
            false
        }
        ViewCompat.setOnApplyWindowInsetsListener(navbar!!) { v: View, insets: WindowInsetsCompat ->
            val pB = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).top
            v.setPadding(0, 0, 0, pB)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupState() {
        when (checkState()) {
            0 -> {}
            1 -> startActivity(Intent(this, NekoActivationActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_CLEAR_TASK))

            2 -> needWelcomeDialog = true
        }
    }

    private fun checkState(): Int {
        nekoprefs = getSharedPreferences(NekoSettingsActivity.SETTINGS, MODE_PRIVATE)
        state = nekoprefs!!.getInt("state", 1)
        return state
    }

    private fun welcomeDialog() {
        MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_name_neko)
                .setIcon(R.drawable.ic_bowl)
                .setCancelable(false)
                .setMessage(R.string.welcome_dialog)
                .setPositiveButton(R.string.get_prize
                ) { dialog: DialogInterface?, id: Int -> gift }.show()
    }

    private val gift: Unit
        get() {
            var cat: Cat?
            for (i in 0..6) {
                cat = NekoWorker.newRandomCat(this, mPrefs)
                mPrefs?.addCat(cat)
                mPrefs?.addCatsAllTime(1)
            }
            setCurrentTheme(-1)
            MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.app_name_neko)
                    .setIcon(R.drawable.ic_fullcat_icon)
                    .setMessage(R.string.welcome_dialog_part2)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok
                    ) { dialog: DialogInterface?, id: Int -> showSnackBar(getString(R.string.open_controls_tip), Snackbar.LENGTH_LONG, navbar) }.show()
        }

    private fun setCurrentTheme(state: Int) {
        val editor = nekoprefs!!.edit()
        editor.putInt("state", state)
        editor.apply()
    }

    companion object {
        private const val debug = false

        @JvmStatic
        fun showSnackBar(text: String?, time: Int, view: View?) {
            val snackbar = Snackbar.make(view!!, text!!, time)
            snackbar.setAnchorView(view)
            snackbar.show()
        }
    }

    class NekoAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            val fragment = if (position == 1) {
                CatControlsFragment()
            } else {
                NekoLandFragment()
            }
            return fragment
        }
    }
}