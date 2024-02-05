package ru.dimon6018.neko11.ui.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import ru.dimon6018.neko11.NekoApplication.Companion.getNekoTheme
import ru.dimon6018.neko11.R
import ru.dimon6018.neko11.ui.fragments.NekoLandFragment
import ru.dimon6018.neko11.workers.Cat
import ru.dimon6018.neko11.workers.Cat.Companion.create
import ru.dimon6018.neko11.workers.NekoWorker
import ru.dimon6018.neko11.workers.PrefState
import ru.dimon6018.neko11.workers.PrefState.PrefsListener
import java.util.Random

class NekoAchievementsActivity : AppCompatActivity(), PrefsListener {
    private var nekoprefs: SharedPreferences? = null
    private var mPrefs: PrefState? = null
    private var coins: MaterialTextView? = null
    private var progress1: LinearProgressIndicator? = null
    private var progress2: LinearProgressIndicator? = null
    private var progress3: LinearProgressIndicator? = null
    private var progress4: LinearProgressIndicator? = null
    private var progress5: LinearProgressIndicator? = null
    private var progress1dstatus = 0
    private var progress2dstatus = 0
    private var progress3dstatus = 0
    private var progress4dstatus = 0
    private var progress5dstatus = 0
    private var gift1: MaterialButton? = null
    private var gift2: MaterialButton? = null
    private var gift3: MaterialButton? = null
    private var gift4: MaterialButton? = null
    private var gift5: MaterialButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getNekoTheme(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.neko_achievements_activity)
        mPrefs = PrefState(this)
        mPrefs!!.setListener(this)
        nekoprefs = getSharedPreferences(NekoSettingsActivity.SETTINGS, MODE_PRIVATE)
        progressSetup()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbarachiev)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayUseLogoEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setupShop()
    }

    override fun onPrefsChanged() {
        progressSetup()
    }

    private fun setupShop() {
        val imageViewCat: ImageView = findViewById(R.id.random_cat_icon)
        val cat: Cat = create(this)
        imageViewCat.setImageBitmap(cat.createIconBitmap(NekoLandFragment.EXPORT_BITMAP_SIZE, NekoLandFragment.EXPORT_BITMAP_SIZE, 0))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun progressSetup() {
        val numCats = nekoprefs!!.getInt("num", 0)
        var a5level = 0
        coins = findViewById(R.id.coins)
        coins!!.text = getString(R.string.coins, mPrefs!!.nCoins)
        val mPrefsManual = getSharedPreferences(PrefState.FILE_NAME, MODE_PRIVATE)
        val map = mPrefsManual.all
        for (key in map.keys) {
            if (key.startsWith(PrefState.CAT_KEY_PREFIX_MOOD)) {
                if (map[key] == 5) {
                    a5level += 1
                }
            }
        }
        progress1dstatus = numCats * 10
        progress2dstatus = numCats * 2
        progress3dstatus = numCats
        progress4dstatus = numCats / 10
        progress5dstatus = a5level * 4
        progress1 = findViewById(R.id.achiev_1_progress)
        progress2 = findViewById(R.id.achiev_2_progress)
        progress3 = findViewById(R.id.achiev_3_progress)
        progress4 = findViewById(R.id.achiev_4_progress)
        progress5 = findViewById(R.id.achiev_5_progress)
        progress1!!.progress = progress1dstatus
        progress2!!.progress = progress2dstatus
        progress3!!.progress = progress3dstatus
        progress4!!.progress = progress4dstatus
        progress5!!.progress = progress5dstatus
        gift1 = findViewById(R.id.get_prize_1)
        gift2 = findViewById(R.id.get_prize_2)
        gift3 = findViewById(R.id.get_prize_3)
        gift4 = findViewById(R.id.get_prize_4)
        gift5 = findViewById(R.id.get_prize_5)
        val mystery = findViewById<MaterialCardView>(R.id.mystery_box_card)
        val cat = findViewById<MaterialCardView>(R.id.random_cat_card)
        val gift1Enabled = nekoprefs!!.getBoolean("gift1_enabled", true)
        val gift2Enabled = nekoprefs!!.getBoolean("gift2_enabled", true)
        val gift3Enabled = nekoprefs!!.getBoolean("gift3_enabled", true)
        val gift4Enabled = nekoprefs!!.getBoolean("gift4_enabled", true)
        val gift5Enabled = nekoprefs!!.getBoolean("gift5_enabled", true)
        if (progress1dstatus >= 100) {
            gift1!!.setEnabled(true)
            if (!gift1Enabled) {
                gift1!!.setText(R.string.gift_not_enabled)
            }
        }
        if (progress2dstatus >= 100) {
            gift2!!.setEnabled(true)
            if (!gift2Enabled) {
                gift2!!.setText(R.string.gift_not_enabled)
            }
        }
        if (progress3dstatus >= 100) {
            gift3!!.setEnabled(true)
            if (!gift3Enabled) {
                gift3!!.setText(R.string.gift_not_enabled)
            }
        }
        if (progress4dstatus >= 100) {
            gift4!!.setEnabled(true)
            if (!gift4Enabled) {
                gift4!!.setText(R.string.gift_not_enabled)
            }
        }
        if (progress5dstatus >= 100 || !nekoprefs!!.getBoolean("code5availability", true)) {
            progress5dstatus = 100
            gift5!!.setEnabled(true)
            if (!gift5Enabled) {
                gift5!!.setText(R.string.gift_not_enabled)
            }
        }
        checkGift()
        mystery.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.achievements)
                    .setMessage(getString(R.string.box_tip))
                    .setIcon(R.drawable.key)
                    .setNegativeButton(android.R.string.cancel, null
                    ).setPositiveButton(R.string.open
                    ) { _: DialogInterface?, _: Int -> openMysteryBox() }.show()
        }
        cat.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.achievements)
                    .setMessage(getString(R.string.cat_tip))
                    .setIcon(R.drawable.key)
                    .setNegativeButton(android.R.string.cancel, null
                    ).setPositiveButton(R.string.yes
                    ) { _: DialogInterface?, _: Int -> getNewCat() }.show()
        }
    }

    private fun getNewCat() {
        val mes: String
        val ico: Drawable?
        if (mPrefs!!.nCoins < 150) {
            mes = getString(R.string.ncoins_err)
            ico = AppCompatResources.getDrawable(this, R.drawable.key)
        } else {
            val cat = create(this)
            mPrefs!!.addCat(cat)
            mPrefs!!.removeNCoins(150)
            ico = BitmapDrawable(getResources(), cat.createIconBitmap(24, 24, 0))
            mes = getString(R.string.new_cat_shop)
        }
        MaterialAlertDialogBuilder(this)
                .setTitle(R.string.achievements)
                .setMessage(mes)
                .setIcon(ico)
                .setNegativeButton(android.R.string.ok, null
                ).show()
    }

    private fun openMysteryBox() {
        if (mPrefs!!.nCoins < 300) {
            MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.achievements)
                    .setMessage(R.string.ncoins_err)
                    .setIcon(R.drawable.key)
                    .setNegativeButton(android.R.string.ok, null
                    ).show()
        } else {
            mPrefs!!.removeNCoins(300)
            val r = Random()
            val result = r.nextInt(8 - 1)
            val s: String
            when (result) {
                1 -> {
                    val coins = r.nextInt(400 + 100)
                    val lb = r.nextInt(3) + 1
                    mPrefs!!.addNCoins(coins)
                    mPrefs!!.addLuckyBooster(lb)
                    s = getString(R.string.box_win1, coins, lb)
                }

                2 -> {
                    val coins = r.nextInt(200 + 100)
                    val mb = r.nextInt(2) + 1
                    mPrefs!!.addNCoins(coins)
                    mPrefs!!.addMoodBooster(mb)
                    s = getString(R.string.box_win2, coins, mb)
                }

                3 -> {
                    val cycles = r.nextInt(3)
                    val mb = r.nextInt(3) + 1
                    for (i in 0..cycles) {
                        val cat = NekoWorker.newRandomCat(this, mPrefs!!, true)
                        mPrefs!!.addCat(cat)
                    }
                    mPrefs!!.addMoodBooster(mb)
                    s = getString(R.string.box_win3, cycles, mb)
                }

                4 -> {
                    val cat = NekoWorker.newRandomCat(this, mPrefs!!, true)
                    mPrefs!!.addCat(cat)
                    val lb = r.nextInt(5) + 1
                    mPrefs!!.addLuckyBooster(lb)
                    s = getString(R.string.box_win4, lb)
                }

                5 -> {
                    val cat = NekoWorker.newRandomCat(this, mPrefs!!, true)
                    mPrefs!!.addCat(cat)
                    val coins = r.nextInt(777 + 100)
                    val lb = r.nextInt(5) + 1
                    val mb = r.nextInt(4) + 1
                    mPrefs!!.addNCoins(coins)
                    mPrefs!!.addLuckyBooster(lb)
                    mPrefs!!.addMoodBooster(mb)
                    s = getString(R.string.box_win5, coins, lb, mb)
                }

                6 -> {
                    val cat = NekoWorker.newRandomCat(this, mPrefs!!, true)
                    mPrefs!!.addCat(cat)
                    s = getString(R.string.box_win6)
                }

                else -> {
                    s = getString(R.string.box_error)
                    mPrefs!!.addNCoins(300)
                }
            }
            MaterialAlertDialogBuilder(this).setTitle(R.string.achievements).setMessage(s).setIcon(R.drawable.key).setNegativeButton(android.R.string.ok, null).show()
        }
    }

    private fun checkGift() {
        val gift1Enabled = nekoprefs!!.getBoolean("gift1_enabled", true)
        val gift2Enabled = nekoprefs!!.getBoolean("gift2_enabled", true)
        val gift3Enabled = nekoprefs!!.getBoolean("gift3_enabled", true)
        val gift4Enabled = nekoprefs!!.getBoolean("gift4_enabled", true)
        val gift5Enabled = nekoprefs!!.getBoolean("gift5_enabled", true)
        gift1!!.setOnClickListener {
            if (gift1Enabled) {
                genCode(1)
            } else {
                getCode(1)
            }
        }
        gift2!!.setOnClickListener {
            if (gift2Enabled) {
                genCode(2)
            } else {
                getCode(2)
            }
        }
        gift3!!.setOnClickListener {
            if (gift3Enabled) {
                genCode(3)
            } else {
                getCode(3)
            }
        }
        gift4!!.setOnClickListener {
            if (gift4Enabled) {
                genCode(4)
            } else {
                getCode(4)
            }
        }
        gift5!!.setOnClickListener {
            if (gift5Enabled) {
                genCode(5)
            } else {
                getCode(5)
            }
        }
    }

    private fun genCode(num: Int) {
        val random = Random()
        val symbols = arrayOf("1", "a", "2", "b", "3", "c", "4", "d", "5", "e", "6", "v", "m", "7", "x")
        var code = ""
        val editor = nekoprefs!!.edit()
        for (i in 0..11) {
            code += symbols[random.nextInt(symbols.size)]
        }
        val message = getString(R.string.new_code_generated, code)
        val copycode = code
        MaterialAlertDialogBuilder(this)
                .setTitle(R.string.achievements)
                .setMessage(message)
                .setIcon(R.drawable.key)
                .setPositiveButton(android.R.string.ok, null
                ).setNegativeButton(R.string.copy) { _: DialogInterface?, _: Int -> copyCode(copycode) }.show()
        when (num) {
            1 -> {
                editor.putString("code1", code)
                editor.putBoolean("gift1_enabled", false)
            }

            2 -> {
                editor.putString("code2", code)
                editor.putBoolean("gift2_enabled", false)
            }

            3 -> {
                editor.putString("code3", code)
                editor.putBoolean("gift3_enabled", false)
            }

            4 -> {
                editor.putString("code4", code)
                editor.putBoolean("gift4_enabled", false)
            }

            5 -> {
                editor.putString("code5", code)
                editor.putBoolean("gift5_enabled", false)
            }
        }
        editor.apply()
        progressSetup()
    }

    private fun getCode(num: Int) {
        val currentCode = when (num) {
            1 -> nekoprefs!!.getString("code1", "")!!
            2 -> nekoprefs!!.getString("code2", "")!!
            3 -> nekoprefs!!.getString("code3", "")!!
            4 -> nekoprefs!!.getString("code4", "")!!
            5 -> nekoprefs!!.getString("code5", "")!!
            else -> ""
        }
        val message = getString(R.string.get_old_code, currentCode)
        MaterialAlertDialogBuilder(this)
                .setTitle(R.string.achievements)
                .setMessage(message)
                .setIcon(R.drawable.key)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(R.string.copy
                ) { _: DialogInterface?, _: Int -> copyCode(currentCode) }.show()
        progressSetup()
    }

    private fun copyCode(code: String) {
        val clipbrd = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("promo", code)
        clipbrd.setPrimaryClip(clip)
    }

    public override fun onPause() {
        super.onPause()
        mPrefs!!.setListener(null)
    }

    public override fun onResume() {
        super.onResume()
        mPrefs!!.setListener(this)
    }
}
