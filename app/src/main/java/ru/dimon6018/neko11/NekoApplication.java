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

package ru.dimon6018.neko11;

import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class NekoApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public void onCreate() {
        super.onCreate();
        NekoApplication.context = getApplicationContext();
    }
    public static Context getNekoContext() {
        return NekoApplication.context;
    }
	public static int getNekoTheme(Context context) {
	SharedPreferences nekoprefs = context.getSharedPreferences(SETTINGS, MODE_PRIVATE);
	   int THEME = nekoprefs.getInt("theme", 0);
	   int currentTheme = switch (THEME) {
           case 1 -> R.style.Theme_Neko11_Pink;
           case 2 -> R.style.Theme_Neko11_Red;
           case 3 -> R.style.Theme_Neko11_Yellow;
           case 4 -> R.style.Theme_Neko11_Green;
           case 5 -> R.style.Theme_Neko11_Lime;
           case 6 -> R.style.Theme_Neko11_Aqua;
           case 7 -> R.style.Theme_Neko11_Blue;
           case 8 -> R.style.Theme_Neko11_Dynamic;
           default -> R.style.Theme_Neko11_Standart;
       };
        return currentTheme;
    }	
}