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
import ru.dimon6018.neko11.ui.activities.NekoSettingsActivity

class NekoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        nekoContext = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var nekoContext: Context? = null
            private set

        @JvmStatic
        fun getNekoTheme(context: Context): Int {
            val nekoprefs = context.getSharedPreferences(NekoSettingsActivity.SETTINGS, MODE_PRIVATE)
            return when (nekoprefs.getInt("theme", 0)) {
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
    }
}