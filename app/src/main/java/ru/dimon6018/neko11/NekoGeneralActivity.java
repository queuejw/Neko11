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
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;

import android.app.UiModeManager;

import ru.dimon6018.neko11.ui.fragments.NekoLand;
import ru.dimon6018.neko11.controls.CatControlsFragment;
import ru.dimon6018.neko11.ui.activities.NekoSettingsActivity;
import ru.dimon6018.neko11.ui.activities.NekoAboutActivity;
import ru.dimon6018.neko11.workers.NekoWorker;
import ru.dimon6018.neko11.workers.PrefState;
import ru.dimon6018.neko11.workers.Cat;
import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.NekoApplication;
import ru.dimon6018.neko11.activation.NekoActivationActivity;
import ru.dimon6018.neko11.ui.activities.NekoAchievementsActivity;

public class NekoGeneralActivity extends AppCompatActivity implements PrefState.PrefsListener {


	private SharedPreferences nekoprefs;
	private PrefState mPrefs;
	private BottomNavigationView navbar;
	private CoordinatorLayout cord;
	private Toolbar toolbar;
	private CollapsingToolbarLayout cToolbar;
	public String promo;
	public int state;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences nekoprefs = getSharedPreferences(SETTINGS, MODE_PRIVATE);
		setupState();
        setupDarkMode();
        setTheme(NekoApplication.getNekoTheme(this));
        super.onCreate(savedInstanceState);
		mPrefs = new PrefState(this);
        mPrefs.setListener(this);
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		setContentView(R.layout.neko_activity);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
		navbar = findViewById(R.id.navigation);
		cord = findViewById(R.id.coordinator);
		cToolbar = findViewById(R.id.collapsingtoolbarlayout);

		getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
		setupNavbarListener();
	    boolean CONTROLS_FIRST = nekoprefs.getBoolean("controlsFirst", false);
		if(CONTROLS_FIRST) {
			getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, CatControlsFragment.class, null)
                .commitNow();
				navbar.setSelectedItemId(R.id.controls);
		} else {
			getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, NekoLand.class, null)
                .commitNow();
				navbar.setSelectedItemId(R.id.collection);
		}
	}
	@Override
    public void onPrefsChanged() {
    }
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ((MenuBuilder) menu).setOptionalIconsVisible(true);
        getMenuInflater().inflate(R.menu.neko_general_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
    public boolean onOptionsItemSelected(MenuItem item) {
             if(item.getItemId() == R.id.aboutMenuId) {
                 startActivity(new Intent(NekoGeneralActivity.this, NekoAboutActivity.class));    
                 return true;
			 }
			else if(item.getItemId() == R.id.achievementsMenuId) {
                  startActivity(new Intent(NekoGeneralActivity.this, NekoAchievementsActivity.class));    
                 return true;	 
			}
            else if(item.getItemId() == R.id.promoMenuId) {
                showPromoDialog();
                return true;
			}
            else if(item.getItemId() == R.id.settingsMenuId) {
                startActivity(new Intent(NekoGeneralActivity.this, NekoSettingsActivity.class));    
                return true;
			}
            else if(item.getItemId() == R.id.checkUpdateId) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.check_updates)
                        .setIcon(R.drawable.ic_updates)
                        .setMessage(R.string.update_message)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.openurl, (dialog, id) -> openurlvoid())
                        .show();
                return true;
			}
			else if(item.getItemId() == R.id.helpMenuId) {
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.container, NekoHelpFragment.class, null)
                        .commit();
						navbar.setSelectedItemId(0);
                return true;
			}
			return super.onOptionsItemSelected(item);
    }

    private void openurlvoid() {
        Toast.makeText(this, R.string.update_toast, Toast.LENGTH_LONG).show();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/queuejw/neko11/releases")));
    }
    @Override
    public void onDestroy() {
		mPrefs.setListener(null);
        super.onDestroy();
    }
	@Override
    public void onPause() {
        super.onPause();
    }
	@Override
    public void onResume() {
        super.onResume();
    }			
	private void setupDarkMode() {     
		switch(nekoprefs.getInt("darktheme", 0)) {
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
					checkPromo(promo);
                }).show();
	}
	private void checkPromo(String promo) {
	boolean code1availability = nekoprefs.getBoolean("code1availability", true);
	boolean code2availability = nekoprefs.getBoolean("code2availability", true);
	boolean code3availability = nekoprefs.getBoolean("code3availability", true);
	boolean code4availability = nekoprefs.getBoolean("code4availability", true);
	SharedPreferences.Editor editor = nekoprefs.edit();	
    if(promo.equals(nekoprefs.getString("code1", ""))) {
		if(code1availability) {
		showSnackBar(getString(R.string.code_is_true), Snackbar.LENGTH_LONG);
		mPrefs.addNCoins(202);	
		editor.putBoolean("code1availability", false);
		} else {
		showSnackBar(getString(R.string.code_is_false), Snackbar.LENGTH_LONG);
		}
    } else if(promo.equals(nekoprefs.getString("code2", ""))) {
		if(code2availability) {
		showSnackBar(getString(R.string.code_is_true), Snackbar.LENGTH_LONG);
		mPrefs.addNCoins(400);
		editor.putBoolean("code2availability", false);
		} else {
		showSnackBar(getString(R.string.code_is_false), Snackbar.LENGTH_LONG);
		}
	} else if(promo.equals(nekoprefs.getString("code3", ""))) {
		if(code3availability) {
		showSnackBar(getString(R.string.code_is_true), Snackbar.LENGTH_LONG);
		mPrefs.addNCoins(1000);
        editor.putBoolean("code3availability", false);		
		} else {
		showSnackBar(getString(R.string.code_is_false), Snackbar.LENGTH_LONG);
		}
	} else if(promo.equals(nekoprefs.getString("code4", ""))) {
		if(code4availability) {
		showSnackBar(getString(R.string.code_is_true), Snackbar.LENGTH_LONG);
		mPrefs.addNCoins(10000);	
		editor.putBoolean("code4availability", false);
		} else {
		showSnackBar(getString(R.string.code_is_false), Snackbar.LENGTH_LONG);
		}
	} else if(promo.equals("hello")) {
		showSnackBar("Hi!", Snackbar.LENGTH_SHORT);	
	} else {
		showSnackBar(getString(R.string.wrong_code), Snackbar.LENGTH_LONG);
	}
	editor.apply();
}
	private void setupNavbarListener() {
		navbar.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.collection) {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.container, NekoLand.class, null)
                            .commitNow();
							return true;
			}
               else if(item.getItemId() == R.id.controls) {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.container, CatControlsFragment.class, null)
                            .commitNow();
							return true;
            }
			return false;
        });
	ViewCompat.setOnApplyWindowInsetsListener(navbar, (v, insets) -> {
	int pB = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).top;		
	v.setPadding(0, 0, 0, pB);
    return WindowInsetsCompat.CONSUMED;
});	
	}
	private void setupState() {
	int state = checkState();
	switch(state) {
		case 0:
		//do nothing
		break;
		case 1:
		startActivity(new Intent(this, NekoActivationActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK));;
		break;
		case 2:
		WelcomeDialog();
		break;
	}
}
	public int checkState() {
	    nekoprefs = getSharedPreferences(SETTINGS, MODE_PRIVATE);     
        state = nekoprefs.getInt("state", 1);
		return state;
	}
	private void WelcomeDialog() {
		 new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.app_name_neko)
                    .setIcon(R.drawable.ic_bowl)
					.setCancelable(false)
                    .setMessage(R.string.welcome_dialog)
                    .setPositiveButton(R.string.get_prize, (dialog, id) -> getGift()
					).show();
	}
	private void getGift() {
		Cat cat;
		for(int i = 0; i <= 6; i++) {
		cat = NekoWorker.newRandomCat(this, mPrefs);
		mPrefs.addCat(cat);
		}
		new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.app_name_neko)
                    .setIcon(R.drawable.ic_fullcat_icon)
                    .setMessage(R.string.welcome_dialog_part2)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialog, id) -> showSnackBar(getString(R.string.open_controls_tip), Snackbar.LENGTH_LONG)
					).show();
	}
	private void showSnackBar(String text, int time) {
	Snackbar snackbar = Snackbar.make(cord, text, time);	
	snackbar.setAnchorView(navbar);
    snackbar.show();
	}
}
