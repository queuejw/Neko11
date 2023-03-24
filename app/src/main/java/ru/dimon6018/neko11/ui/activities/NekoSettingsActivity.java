package ru.dimon6018.neko11.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.core.view.WindowCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import androidx.annotation.MenuRes;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.PopupMenu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.color.DynamicColors;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;

import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.workers.PrefState;
import ru.dimon6018.neko11.workers.NekoWorker;
import ru.dimon6018.neko11.workers.Cat;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.core.view.WindowCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import android.app.UiModeManager;

import java.io.File;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import android.os.Environment;

public class NekoSettingsActivity extends AppCompatActivity implements PrefState.PrefsListener {

    public static final String STATEPREF = "statepref";
    public static final String SETTINGS = "SettingsPrefs";
	
    public SharedPreferences nekoprefs;
	
	private PrefState mPrefs;
	
    	MaterialButton opensettingsbtn;
        MaterialSwitch whiteswitch;
        MaterialSwitch linearcontrol;
		MaterialSwitch dyncolor;
		MaterialSwitch autowhiteswitch;
		MaterialButton accentchoose;
		MaterialButton save;
		MaterialButton restore;
		
		MaterialButton standartsort;
		MaterialButton namesort;
		MaterialButton offsort;
		
		MaterialButtonToggleGroup sort_group;
		
		Slider limit_slider;
		
		Animation scale;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    nekoprefs = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
	int THEME = nekoprefs.getInt("theme", 0);
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
        super.onCreate(savedInstanceState);
		setContentView(R.layout.neko_settings_activity);
	    mPrefs = new PrefState(this);
        mPrefs.setListener(this);
		Toolbar toolbar = findViewById(R.id.toolbarset);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
	   
        opensettingsbtn = findViewById(R.id.opensettingsbtn);
        whiteswitch = findViewById(R.id.white_switch);
        linearcontrol = findViewById(R.id.linear_controls_enable);
		dyncolor = findViewById(R.id.dynamic_color);
		autowhiteswitch = findViewById(R.id.white_switch_auto);
		accentchoose = findViewById(R.id.choosetheme);
		sort_group = findViewById(R.id.sortgroup);
		
		standartsort = findViewById(R.id.sortstandart);
		namesort = findViewById(R.id.sortname);
		offsort = findViewById(R.id.sortoff);
		
		save = findViewById(R.id.backup_save);
		restore = findViewById(R.id.backup_restore);
		
		limit_slider = findViewById(R.id.max_cat_line_slider);
		
		//version_num = findViewById(R.id.ver_num);
							
		setupScreen();
		setupClickListeners();
		
		scale = AnimationUtils.loadAnimation(this, R.anim.scale);
    }
		public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
	}
	
	@Override
    public void onPrefsChanged() {
    }
	
	private void setupClickListeners() {
	sort_group.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {	
	@Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
       if(checkedId == R.id.sortname) {
		if(isChecked) {
			mPrefs.setSortState(2);
		}
	   }
		if(checkedId ==  R.id.sortstandart) {
		if(isChecked) {
			mPrefs.setSortState(1);
		}
		}	
		if(checkedId == R.id.sortoff) {
		if(isChecked) {
			mPrefs.setSortState(0);
		}
		}
			}
	});
    accentchoose.setOnClickListener(v -> showMenu(v, R.menu.neko_colors));	
	save.setOnClickListener(v -> {
		Cat cat;
		for(int i = 0; i <= 10; i++) {
		cat = NekoWorker.newRandomCat(this, mPrefs);
	}
	});	
	opensettingsbtn.setOnClickListener(v -> {
		    opensettingsbtn.startAnimation(scale);
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
        whiteswitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = nekoprefs.edit();
            if (isChecked) {
                editor.putInt("darktheme", 1);
				editor.apply();
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					UiModeManager UImanager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);	
					UImanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
				} else {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				}
            } else {
                editor.putInt("darktheme", 0);
				editor.apply();
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					UiModeManager UImanager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);		
					UImanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
				} else {
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				}
            }
        });
		autowhiteswitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = nekoprefs.edit();
            if (isChecked) {
				whiteswitch.setEnabled(false);
				whiteswitch.setChecked(false);
				editor.putInt("darktheme", 2);
				editor.apply();
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else {
				whiteswitch.setEnabled(true);
				editor.putInt("darktheme", 0);
				editor.apply();
            }
        });
		dyncolor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = nekoprefs.edit();
            if (isChecked) {
                editor.putInt("theme", 8);
				editor.apply();
            } else {
                editor.putInt("theme", 0);
				editor.apply();
            }
        });
        linearcontrol.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = nekoprefs.edit(); 
			if (isChecked) {       
			editor.putBoolean("linear_control", true);        		
			} else {
			editor.putBoolean("linear_control", false);	
			}
			 editor.apply();			 
        });
        limit_slider.addOnChangeListener((rangeSlider, value, fromUser) -> {
		int valueNew = Math.round(value);	
		mPrefs.setCatsInLineLimit(valueNew);
	});
}	
	private void showMenu(View v, @MenuRes int menuRes) {
	SharedPreferences nekoprefs = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
	int THEME = nekoprefs.getInt("theme", 0);
    PopupMenu popup = new PopupMenu(this, v);
    popup.getMenuInflater().inflate(menuRes, popup.getMenu());
    popup.setOnMenuItemClickListener(
        menuItem -> {
			SharedPreferences.Editor editor = nekoprefs.edit();
			if(menuItem.getItemId() == R.id.pink_theme) {
				  editor.putInt("theme", 1);
				  editor.apply();
				  themeChanged(v);
			}
			else if(menuItem.getItemId() == R.id.red_theme) {
				  editor.putInt("theme", 2);
				  editor.apply(); 
				  themeChanged(v);
	        }
			else if(menuItem.getItemId() == R.id.orange_theme) {
				  editor.putInt("theme", 3);
				  editor.apply();
				  themeChanged(v);
			}
			else if(menuItem.getItemId() == R.id.green_theme) {
				  editor.putInt("theme", 4);
				  editor.apply(); 
				  themeChanged(v);
			}
			else if(menuItem.getItemId() == R.id.lime_theme) {
				  editor.putInt("theme", 5);
				  editor.apply(); 
				  themeChanged(v);
			}
			else if(menuItem.getItemId() == R.id.aqua_theme) {
				  editor.putInt("theme", 6);
				  editor.apply(); 
				  themeChanged(v);
 			}
			else if(menuItem.getItemId() == R.id.blue_theme) {
				  editor.putInt("theme", 7);
				  editor.apply();   
				  themeChanged(v);
			}
			else if(menuItem.getItemId() == R.id.purple_theme) {
				  editor.putInt("theme", 0);
				  editor.apply();
 				  themeChanged(v);
			}		
          return true;
        });
    popup.show();
	  }
	  private void themeChanged(View view) {
      Snackbar.make(view, R.string.themeChanged, Snackbar.LENGTH_SHORT).show();
	  }
	private void setupScreen() {
	int THEME = nekoprefs.getInt("theme", 0);	
	int DARK_ENABLE = nekoprefs.getInt("darktheme", 0);	
	boolean LINEAR_CONTROL = nekoprefs.getBoolean("linear_control", false);		
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dyncolor.setEnabled(true);
        } else {
            dyncolor.setEnabled(false);
        }		
        linearcontrol.setChecked(LINEAR_CONTROL);
	
		if(THEME == 8) {
		    dyncolor.setChecked(true);
		} else {
			dyncolor.setChecked(false);
		}
		limit_slider.setValue(mPrefs.getCatsInLineLimit());
		
		switch(DARK_ENABLE) {
			case 0:
			 whiteswitch.setChecked(false);
			 autowhiteswitch.setEnabled(true);
			 break;
			case 1:
			 whiteswitch.setChecked(true);
			 break;
			case 2:
			 autowhiteswitch.setChecked(true);
			 whiteswitch.setEnabled(false);
			 break;
			default:
             whiteswitch.setChecked(false);
             break;		
		}	
		switch(mPrefs.getSortState()) {
		case 2:
        namesort.setChecked(true);
		break;
		case 1:
        standartsort.setChecked(true);
		break;
		case 0:
		offsort.setChecked(true);
		break;
		}
	}
}