/*
 * Copyright (C) 2020 The Android Open Source Project
 * Copyright (C) 2017, 2018, 2019 Christopher Blay <chris.b.blay@gmail.com>
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
package ru.dimon6018.neko11.workers

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.appcompat.app.AppCompatActivity
import ru.dimon6018.neko11.ui.activities.NekoSettingsActivity
import ru.dimon6018.neko11.ui.fragments.NekoLandFragment

class PrefState(private val mContext: Context) : OnSharedPreferenceChangeListener {
    private val mPrefs: SharedPreferences = mContext.getSharedPreferences(FILE_NAME, 0)
    private var mListener: PrefsListener? = null

    // Can also be used for renaming.
    fun addCat(cat: Cat) {
        mPrefs.edit()
                .putString(CAT_KEY_PREFIX + cat.seed, cat.name)
                .apply()
    }
    fun removeCat(cat: Cat) {
        mPrefs.edit()
                .remove(CAT_KEY_PREFIX + cat.seed)
                .apply()
    }

    fun setMood(cat: Cat, mood: Int) {
        mPrefs.edit()
                .putInt(CAT_KEY_PREFIX_MOOD + cat.seed, mood)
                .apply()
    }
    fun setAge(cat: Cat, age: Int) {
        mPrefs.edit()
                .putInt(CAT_AGE + cat.seed, age)
                .apply()
    }
    val cats: List<Cat>
        get() {
            val cats = ArrayList<Cat>()
            val map = mPrefs.all
            for (key in map.keys) {
                if (key.startsWith(CAT_KEY_PREFIX)) {
                    val seed = key.substring(CAT_KEY_PREFIX.length).toLong()
                    val cat = Cat(mContext, seed, map[key].toString())
                    cat.name = map[key].toString()
                    cats.add(cat)
                }
            }
            return cats
        }
    fun catBySeed(catSeed: Long): Cat? {
        var cat: Cat? = null
        val map = mPrefs.all
        for (key in map.keys) {
            if (key.startsWith(CAT_KEY_PREFIX)) {
                val seed = key.substring(CAT_KEY_PREFIX.length).toLong()
                if (seed == catSeed) {
                    cat = Cat(mContext, seed, map[key].toString())
                }
            }
        }
        return cat
    }
    fun clearPrefsWithoutCats() {
        val map = mPrefs.all
        val nekoprefs = mContext.getSharedPreferences(NekoSettingsActivity.SETTINGS, AppCompatActivity.MODE_PRIVATE)
        nekoprefs.edit().clear().apply()
        setCustomBackgroundPath("")
        for (key in map.keys) {
            if (key.startsWith(CAT_KEY_PREFIX)) {
                continue
            }
            else if(key.startsWith(CAT_KEY_PREFIX_MOOD)) {
                continue
            }
            else if(key.startsWith(CAT_DIRTY_PREFIX)) {
                continue
            }
            else {
                mPrefs.edit().remove(key).apply()
            }
        }
        val editor = nekoprefs.edit()
        editor.putInt("state", 0)
        editor.apply()
    }
    fun getEditor(): SharedPreferences.Editor {
        return mPrefs.edit()
    }
    fun getPrefsAccess(): SharedPreferences {
        return mPrefs
    }
    fun setupHats() {
        var count = 0
        while (count != NekoLandFragment.HATS) {
            if(count == 0) {
                mPrefs.edit().putBoolean("is_hat_purchased_0", true).apply()
                count += 1
                continue
            }
            mPrefs.edit().putBoolean("is_hat_purchased_$count", false).apply()
            count += 1
        }
    }
    fun setupSuits() {
        var count = 0
        while (count != NekoLandFragment.SUITS) {
            if(count == 0) {
                mPrefs.edit().putBoolean("is_suit_purchased_0", true).apply()
                count += 1
                continue
            }
            mPrefs.edit().putBoolean("is_suit_purchased_$count", false).apply()
            count += 1
        }
    }
    fun clearActionsBlock() {
        val map = mPrefs.all
        for (key in map.keys) {
            if (key.startsWith(CAT_INTERACT_KEY_PREFIX)) {
                val seed = key.substring(CAT_INTERACT_KEY_PREFIX.length).toLong()
                val cat = Cat(mContext, seed, map[key].toString())
                setCanInteract(cat, 6)
            }
        }
    }

    fun getMoodPref(cat: Cat): Int {
        return mPrefs.getInt(CAT_KEY_PREFIX_MOOD + cat.seed, 3)
    }
    fun getCatAge(seed: Long): Int {
        return mPrefs.getInt(CAT_AGE + seed, 2)
    }
    var toyState: Int
        get() = mPrefs.getInt(TOY_STATE, 0)
        set(toystate) {
            mPrefs.edit().putInt(TOY_STATE, toystate).apply()
        }
    var foodState: Int
        get() = mPrefs.getInt(FOOD_STATE, 0)
        set(foodState) {
            mPrefs.edit().putInt(FOOD_STATE, foodState).apply()
        }
    var waterState: Float
        get() = mPrefs.getFloat(WATER_STATE, 0f)
        set(waterState) {
            mPrefs.edit().putFloat(WATER_STATE, waterState).apply()
        }
    fun setListener(listener: PrefsListener?) {
        mListener = listener
        if (mListener != null) {
            mPrefs.registerOnSharedPreferenceChangeListener(this)
        } else {
            mPrefs.unregisterOnSharedPreferenceChangeListener(this)
        }
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        mListener!!.onPrefsChanged()
    }
    interface PrefsListener {
        fun onPrefsChanged()
    }
    var sortState: Int
        get() = mPrefs.getInt(SORT_STATE, 1)
        set(sortState) {
            mPrefs.edit().putInt(SORT_STATE, sortState).apply()
        }
    var catsInLineLimit: Int
        get() = mPrefs.getInt(CATS_LIMIT, 3)
        set(limit) {
            mPrefs.edit().putInt(CATS_LIMIT, limit).apply()
        }
    val nCoins: Int
        get() = mPrefs.getInt(NCOINS, 0)
    val moodBoosters: Int
        get() = mPrefs.getInt(MOOD_BOOSTER, 0)
    val luckyBoosters: Int
        get() = mPrefs.getInt(LUCKY_BOOSTER, 0)

    fun addNCoins(coins: Int) {
        val currentCoins = nCoins
        val newCoins = currentCoins + coins
        mPrefs.edit().putInt(NCOINS, newCoins).apply()
    }

    fun removeNCoins(coins: Int) {
        val currentCoins = nCoins
        var newCoins = currentCoins - coins
        if(newCoins <= 0) {
            newCoins = 0
        }
        mPrefs.edit().putInt(NCOINS, newCoins).apply()
    }

    fun addMoodBooster(boosters: Int) {
        val currentBoosters = moodBoosters
        val newBoosters = currentBoosters + boosters
        mPrefs.edit().putInt(MOOD_BOOSTER, newBoosters).apply()
    }

    fun removeMoodBooster(boosters: Int) {
        val currentBoosters = moodBoosters
        var newBoosters = currentBoosters - boosters
        if(newBoosters <= 0) {
            newBoosters = 0
        }
        mPrefs.edit().putInt(MOOD_BOOSTER, newBoosters).apply()
    }

    fun addLuckyBooster(boosters: Int) {
        val currentBoosters = luckyBoosters
        val newBoosters = currentBoosters + boosters
        mPrefs.edit().putInt(LUCKY_BOOSTER, newBoosters).apply()
    }

    fun removeLuckyBooster(boosters: Int) {
        val currentBoosters = luckyBoosters
        var newBoosters = currentBoosters - boosters
        if(newBoosters <= 0) {
            newBoosters = 0
        }
        mPrefs.edit().putInt(LUCKY_BOOSTER, newBoosters).apply()
    }

    fun isCanInteract(cat: Cat): Int {
        return mPrefs.getInt(CAT_INTERACT_KEY_PREFIX + cat.seed, 6)
    }

    fun setCanInteract(cat: Cat, i: Int) {
        mPrefs.edit().putInt(CAT_INTERACT_KEY_PREFIX + cat.seed, i).apply()
    }

    var catIconSize: Int
        get() = mPrefs.getInt(ICON_SIZE, 150)
        set(size) {
            mPrefs.edit().putInt(ICON_SIZE, size).apply()
        }

    fun wipeData() {
        mPrefs.edit().clear().apply()
    }

    fun setCustomBackgroundPath(path: String?) {
        mPrefs.edit().putString(LAUNCHER_CUSTOM_BACKGRD, path).apply()
    }

    val backgroundPath: String?
        get() = mPrefs.getString(LAUNCHER_CUSTOM_BACKGRD, "")

    fun setCatHat(hat: Int, seed: Long) {
        mPrefs.edit().putInt(CAT_HAT_PREFIX + seed, hat).apply()
    }
    fun getCatHatCode(seed: Long): Int {
        return mPrefs.getInt(CAT_HAT_PREFIX + seed, 0)
    }
    fun setCatSuit(suit: Int, seed: Long) {
        mPrefs.edit().putInt(CAT_SUIT_PREFIX + seed, suit).apply()
    }
    fun getCatSuitCode(seed: Long): Int {
        return mPrefs.getInt(CAT_SUIT_PREFIX + seed, 0)
    }
    fun setCatDirty(bool: Boolean, seed: Long) {
        mPrefs.edit().putBoolean(CAT_DIRTY_PREFIX + seed, bool).apply()
    }
    fun getCatDirty(seed: Long): Boolean {
        return mPrefs.getBoolean(CAT_DIRTY_PREFIX + seed, false)
    }
    fun setIconBackground(type: Int) {
        mPrefs.edit().putInt(ICON_BACKGROUND, type).apply()
    }
    fun getIconBackground(): Int {
        return mPrefs.getInt(ICON_BACKGROUND, 1)
    }
    fun setToiletState(level: Int) {
        mPrefs.edit().putInt(TOILET_STATE, level).apply()
    }
    fun getToiletState(): Int {
        return mPrefs.getInt(TOILET_STATE, 0)
    }
    fun setRunDialog(bool: Boolean) {
        mPrefs.edit().putBoolean(CAT_DIALOG, bool).apply()
    }
    fun isDialogEnabled(): Boolean {
        return mPrefs.getBoolean(CAT_DIALOG, false)
    }
    fun setMusic(bool: Boolean) {
        mPrefs.edit().putBoolean(MUSIC, bool).apply()
    }
    fun isMusicEnabled(): Boolean {
        return mPrefs.getBoolean(MUSIC, true)
    }

    companion object {
        const val FILE_NAME = "mPrefs"
        private const val FOOD_STATE = "food"
        private const val SORT_STATE = "sort"
        private const val TOILET_STATE = "toilet"
        private const val CATS_LIMIT = "limit"
        const val ICON_SIZE = "size"
        const val ICON_BACKGROUND = "background"
        const val CAT_DIALOG = "catDialogEnabled"
        const val MUSIC = "musicEnabled"
        private const val NCOINS = "nCoins"
        const val TOY_STATE = "toy"
        const val WATER_STATE = "water"
        const val CAT_KEY_PREFIX = "cat:"
        const val CAT_HAT_PREFIX = "catHat:"
        const val CAT_DIRTY_PREFIX = "catDirty:"
        const val CAT_AGE = "catAge:"
        const val CAT_SUIT_PREFIX = "catSuit:"
        const val CAT_INTERACT_KEY_PREFIX = "catInteract:"
        const val CAT_KEY_PREFIX_MOOD = "mood:"
        const val MOOD_BOOSTER = "mood_booster"
        const val LUCKY_BOOSTER = "lucky_booster"
        const val LAUNCHER_CUSTOM_BACKGRD = "LAUNCHER_CUSTOM_BACKGROUND"
        @JvmField
        var isLuckyBoosterActive = false
    }
}
