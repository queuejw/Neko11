package ru.dimon6018.neko11.workers.skins

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import ru.dimon6018.neko11.R
import ru.dimon6018.neko11.workers.PrefState

object CatSkins {
    fun getCatHat(context: Context?, seed: Long?): Drawable? {
        val result: Drawable?
        val mPrefs = PrefState(context!!)
        result = when (mPrefs.getCatHatCode(seed!!)) {
            0 -> AppCompatResources.getDrawable(context, R.drawable.nothing)
            1 -> AppCompatResources.getDrawable(context, R.mipmap.hat_1)
            2 -> AppCompatResources.getDrawable(context, R.mipmap.hat_2)
            3 -> AppCompatResources.getDrawable(context, R.mipmap.hat_3)
            4 -> AppCompatResources.getDrawable(context, R.mipmap.hat_4)
            5 -> AppCompatResources.getDrawable(context, R.mipmap.hat_5)
            6 -> AppCompatResources.getDrawable(context, R.mipmap.hat_6)
            7 -> AppCompatResources.getDrawable(context, R.mipmap.hat_7)
            8 -> AppCompatResources.getDrawable(context, R.mipmap.hat_8)
            9 -> AppCompatResources.getDrawable(context, R.mipmap.hat_9)
            10 -> AppCompatResources.getDrawable(context, R.mipmap.hat_10)
            11 -> AppCompatResources.getDrawable(context, R.mipmap.hat_11)
            else -> AppCompatResources.getDrawable(context, R.drawable.nothing)
        }
        return result
    }

    fun getCatSuit(context: Context?, seed: Long?): Drawable? {
        val result: Drawable?
        val mPrefs = PrefState(context!!)
        result = when (mPrefs.getCatSuitCode(seed!!)) {
            0 -> AppCompatResources.getDrawable(context, R.drawable.nothing)
            1 -> AppCompatResources.getDrawable(context, R.mipmap.suit_1)
            2 -> AppCompatResources.getDrawable(context, R.mipmap.suit_2)
            3 -> AppCompatResources.getDrawable(context, R.mipmap.suit_3)
            4 -> AppCompatResources.getDrawable(context, R.mipmap.suit_4)
            5 -> AppCompatResources.getDrawable(context, R.mipmap.suit_5)
            6 -> AppCompatResources.getDrawable(context, R.mipmap.suit_6)
            7 -> AppCompatResources.getDrawable(context, R.mipmap.suit_7)
            8 -> AppCompatResources.getDrawable(context, R.mipmap.suit_8)
            9 -> AppCompatResources.getDrawable(context, R.mipmap.suit_9)
            10 -> AppCompatResources.getDrawable(context, R.mipmap.suit_10)
            11 -> AppCompatResources.getDrawable(context, R.mipmap.suit_11)
            else -> AppCompatResources.getDrawable(context, R.drawable.nothing)
        }
        return result
    }

    fun getCatDirty(context: Context?, seed: Long?): Drawable? {
        val result: Drawable?
        val mPrefs = PrefState(context!!)
        result = if (mPrefs.getCatDirty(seed!!)) {
            AppCompatResources.getDrawable(context, R.drawable.cat_dirt)
        } else {
            AppCompatResources.getDrawable(context, R.drawable.nothing)
        }
        return result
    }
}
