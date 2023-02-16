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

package ru.dimon6018.neko11;

import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.STATEPREF;
import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.view.ContextThemeWrapper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.elevation.SurfaceColors;

import android.app.UiModeManager;
import java.util.Random;

import ru.dimon6018.neko11.ui.fragments.NekoLand;
import ru.dimon6018.neko11.ui.fragments.CatControlsFragment;
import ru.dimon6018.neko11.ui.activities.NekoSettingsActivity;
import ru.dimon6018.neko11.ui.activities.NekoAboutActivity;
import ru.dimon6018.neko11.ui.oobe.NekoOOBE;
import ru.dimon6018.neko11.workers.NekoWorker;
import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.activation.NekoActivationActivity;
import ru.dimon6018.neko11.ui.activities.NekoAchievementsActivity;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import dev.chrisbanes.insetter.Insetter;

public class NekoGeneralActivity extends AppCompatActivity {

    public SharedPreferences appstate;
	private BottomNavigationView navbar;
	private CoordinatorLayout cord;
	private Toolbar toolbar;
	public String promo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences nekoprefs = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        appstate = getSharedPreferences(STATEPREF, Context.MODE_PRIVATE);
        int STATE = appstate.getInt("state", 2);
		int THEME = nekoprefs.getInt("theme", 0);
        switch(STATE) {
          case 1:
            break;
         case 2:
            Intent activation = new Intent(this, NekoActivationActivity.class);
            startActivity(activation);
            finish();
            break;
         case 3:
            Intent oobe = new Intent(this, NekoOOBE.class);
            startActivity(oobe);
            finish();
            break;
         default:
            break;
       }
	switch(THEME) {
		case 0:
            setTheme(R.style.Theme_Neko11_Standart);
            break;
        case 1:
            setTheme(R.style.Theme_Neko11_Pink);
            break;
        case 2:
            setTheme(R.style.Theme_Neko11_Red);
            break;
        case 3:
            setTheme(R.style.Theme_Neko11_Orange);
            break;
		case 4:
            setTheme(R.style.Theme_Neko11_Green);
            break;
		case 5:
            setTheme(R.style.Theme_Neko11_Lime);
            break;
        case 6:
            setTheme(R.style.Theme_Neko11_Aqua);
            break;
        case 7:
            setTheme(R.style.Theme_Neko11_Blue);
            break;
		case 8:
            setTheme(R.style.Theme_Neko11_Dynamic);
			DynamicColors.applyToActivityIfAvailable(this);
            break;
        default:
            setTheme(R.style.Theme_Neko11_Standart);
            break;
       }
        int DARK_ENABLE = nekoprefs.getInt("darktheme", 0);
		switch(DARK_ENABLE) {
			case 0:
			  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				  UiModeManager UImanager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);	
					UImanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
				} else {
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				}
			 break;
			case 1:
			    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					UiModeManager UImanager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);		
					UImanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
				} else {
			    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				}
			 break;
			case 2:
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				    UiModeManager UImanager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);	
					UImanager.setApplicationNightMode(UImanager.MODE_NIGHT_AUTO);
				} else {
			 AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
				}
			 break;
			default:
		    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					UiModeManager UImanager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);		
					UImanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
				} else {
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				}
             break;		
		}		
        super.onCreate(savedInstanceState);
		setContentView(R.layout.neko_activity);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
		navbar = findViewById(R.id.navigation);
		cord = findViewById(R.id.coordinator);
		
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

		Insetter.builder()
       .padding(WindowInsetsCompat.Type.statusBars())
       .applyToView(toolbar);
	   
	   	Insetter.builder()
       .padding(WindowInsetsCompat.Type.navigationBars())
       .applyToView(cord);
	   
	   getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
	   
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, NekoLand.class, null)
                .commit();

        navbar.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.collection:
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.container, NekoLand.class, null)
                            .commit();
                    return true;
                case R.id.controls:
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.container, CatControlsFragment.class, null)
                            .commit();
                    return true;
            }
            return false;
        });
	}
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ((MenuBuilder) menu).setOptionalIconsVisible(true);
        getMenuInflater().inflate(R.menu.neko_general_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
             case R.id.aboutMenuId:
                  startActivity(new Intent(NekoGeneralActivity.this, NekoAboutActivity.class));    
                 return true;
			case R.id.achievementsMenuId:
                  startActivity(new Intent(NekoGeneralActivity.this, NekoAchievementsActivity.class));    
                 return true;	 
            case R.id.promoMenuId:
                showPromoDialog();
                return true;
            case R.id.settingsMenuId:
                startActivity(new Intent(NekoGeneralActivity.this, NekoSettingsActivity.class));    
                return true;
            case R.id.checkUpdateId:
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.check_updates)
                        .setIcon(R.drawable.ic_updates)
                        .setMessage(R.string.update_message)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.openurl, (dialog, id) -> openurlvoid())
                        .show();
                return true;
            case R.id.helpMenuId:
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.container, NekoHelpFragment.class, null)
                        .commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openurlvoid() {
        Toast.makeText(this, R.string.update_toast, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/nekoapp_news")));
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
	private void showPromoDialog() {
		 final Context context = new ContextThemeWrapper(this, getTheme());	
        View view = LayoutInflater.from(context).inflate(R.layout.edit_text_promo, null);
        final EditText text = view.findViewById(R.id.editpromo);
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.promo)
                .setIcon(R.drawable.key)
                .setView(view)
				.setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    promo = text.getText().toString();	
                }).show();
	}
}
