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

import android.app.Application;import android.content.Context;
import android.content.SharedPreferences;

public class NekoApplication extends Application {
	
	public static int getNekoTheme(Context context) {
	SharedPreferences nekoprefs = context.getSharedPreferences(SETTINGS, MODE_PRIVATE);
	   int THEME = nekoprefs.getInt("theme", 0);
	   int currentTheme;
	switch(THEME) {
		case 0:
             currentTheme = R.style.Theme_Neko11_Standart;
            break;
        case 1:
             currentTheme = R.style.Theme_Neko11_Pink;
            break;
        case 2:
             currentTheme = R.style.Theme_Neko11_Red;
            break;
        case 3:
             currentTheme = R.style.Theme_Neko11_Yellow;
            break;
		case 4:
             currentTheme = R.style.Theme_Neko11_Green;
            break;
		case 5:
             currentTheme = R.style.Theme_Neko11_Lime;
            break;
        case 6:
             currentTheme = R.style.Theme_Neko11_Aqua;
            break;
        case 7:
             currentTheme = R.style.Theme_Neko11_Blue;
            break;
		case 8:
             currentTheme = R.style.Theme_Neko11_Dynamic;
            break;	
        default:
             currentTheme = R.style.Theme_Neko11_Standart;
             break;
	    }
		return currentTheme;
    }	
}