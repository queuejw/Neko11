/*
 * Copyright (C) 2023 Dmitry Frolkov
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
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.media.AudioAttributes
import android.media.MediaPlayer
import ru.dimon6018.neko11.ui.activities.NekoSettingsActivity
import ru.dimon6018.neko11.workers.Cat
import ru.dimon6018.neko11.workers.ExceptionHandler
import ru.dimon6018.neko11.workers.PrefState


class NekoApplication : Application() {
    override fun onCreate() {
        ExceptionHandler.setContext(applicationContext)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
        super.onCreate()
        nekoContext = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var nekoContext: Context? = null
            private set
        private var accentColors = intArrayOf(
                R.color.pink_theme_primary, R.color.red_theme_primary, R.color.yellow_theme_primary, R.color.green_theme_primary,
                R.color.lime_theme_primary, R.color.aqua_theme_primary, R.color.blue_theme_primary
        )

        @JvmStatic
        fun getNekoTheme(context: Context): Int {
            val nekoPrefs = context.getSharedPreferences(NekoSettingsActivity.SETTINGS, MODE_PRIVATE)
            return when (nekoPrefs.getInt("theme", 0)) {
                1 -> R.style.Theme_Neko11_Pink
                2 -> R.style.Theme_Neko11_Red
                3 -> R.style.Theme_Neko11_Yellow
                4 -> R.style.Theme_Neko11_Green
                5 -> R.style.Theme_Neko11_Lime
                6 -> R.style.Theme_Neko11_Aqua
                7 -> R.style.Theme_Neko11_Blue
                8 -> R.style.Theme_Neko11_Dynamic
                else -> R.style.Theme_Neko11_Standart
            }
        }

        @JvmStatic
        fun getCatMood(context: Context, cat: Cat): String {
            val prefs = PrefState(context)
            val result = when (prefs.getMoodPref(cat)) {
                1 -> context.getString(R.string.mood1)
                2 -> context.getString(R.string.mood2)
                3 -> context.getString(R.string.mood3)
                4 -> context.getString(R.string.mood4)
                5 -> context.getString(R.string.mood5)
                else -> context.getString(R.string.mood1)
            }
            return result
        }

        @JvmStatic
        fun getTextColor(context: Context): Int {
            val nekoPrefs = context.getSharedPreferences(NekoSettingsActivity.SETTINGS, MODE_PRIVATE)
            val color = nekoPrefs.getInt("theme", 0)
            return if (color >= 0 && color < accentColors.size) {
                accentColors[color]
            } else {
                // Default to "unknown" if the selected color is out of bounds
                android.R.color.white
            }
        }
        fun playMusic(mPrefs: PrefState, resources: Resources) {
            if(NekoGeneralActivity.mediaPlayer == null) {
                NekoGeneralActivity.mediaPlayer = MediaPlayer()
            }
            val afd = resources.assets.openFd("music/music1.mp3")
            NekoGeneralActivity.mediaPlayer!!.isLooping = true
            NekoGeneralActivity.mediaPlayer!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.getLength())
            NekoGeneralActivity.mediaPlayer!!.setAudioAttributes(
                    AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
            if(mPrefs.isMusicEnabled()) {
                NekoGeneralActivity.mediaPlayer!!.prepare()
                NekoGeneralActivity.mediaPlayer!!.start()
                NekoGeneralActivity.isMusicPlaying = true
            }
        }
    }
}