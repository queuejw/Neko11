package ru.dimon6018.neko11.ui.activities;

import static ru.dimon6018.neko11.NekoGeneralActivity.showSnackBar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.MenuRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.dimon6018.neko11.BuildConfig;
import ru.dimon6018.neko11.NekoApplication;
import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.workers.BackupParser;
import ru.dimon6018.neko11.workers.PrefState;

public class NekoSettingsActivity extends AppCompatActivity implements PrefState.PrefsListener {

    public static final String SETTINGS = "SettingsPrefs";
	
    public SharedPreferences nekoprefs;
	private PrefState mPrefs;

	boolean isSave = false;
	boolean isRestore = false;
	boolean isDelete = false;
    	MaterialButton opensettingsbtn;
        MaterialSwitch whiteswitch;
        MaterialSwitch linearcontrol;
		MaterialSwitch dyncolor;
		MaterialSwitch autowhiteswitch;
		MaterialSwitch controlsFirst;
		
		MaterialButton accentchoose;	
		MaterialButton standartsort;
		MaterialButton namesort;
		MaterialButton offsort;
		MaterialButton details;
		MaterialButton recovery;
		MaterialButtonToggleGroup sort_group;
		
		Slider limit_slider;
		Slider cat_resizer;

	   ConstraintLayout cord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(NekoApplication.getNekoTheme(this));
        super.onCreate(savedInstanceState);
	  	setContentView(R.layout.neko_settings_activity);
	    mPrefs = new PrefState(this);
        mPrefs.setListener(this);
		Toolbar toolbar = findViewById(R.id.toolbarset);
		cord = findViewById(R.id.cord);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
	    nekoprefs = getSharedPreferences(SETTINGS, MODE_PRIVATE);
		
		new Thread(() -> {
		setupScreen();
		setupClickListeners();
		}).start();
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
	sort_group.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
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
});
    accentchoose.setOnClickListener(v -> showMenu(v, R.menu.neko_colors));

	recovery.setOnClickListener(v -> startRecovery(this));

	opensettingsbtn.setOnClickListener(v -> {
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
			editor.putBoolean("linear_control", isChecked);
			editor.apply();			 
        });
	controlsFirst.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = nekoprefs.edit();
			editor.putBoolean("controlsFirst", isChecked);
			editor.apply();			 
        });
    limit_slider.addOnChangeListener((rangeSlider, value, fromUser) -> {
		int valueNew = Math.round(value);	
		mPrefs.setCatsInLineLimit(valueNew);
	});
	cat_resizer.addOnChangeListener((rangeSlider, value, fromUser) -> {
		int valueNew = Math.round(value);	
		mPrefs.setCatIconSize(valueNew);
	});
	details.setOnClickListener(v -> {
		final Context context = new ContextThemeWrapper(this, getTheme());	
        View view = LayoutInflater.from(context).inflate(R.layout.neko_info_dialog, null);
        MaterialTextView androidtxt = view.findViewById(R.id.neko_info_android_ver);
		MaterialTextView codetxt = view.findViewById(R.id.neko_info_ver_code);
		MaterialTextView nametxt = view.findViewById(R.id.neko_info_ver_name);
		nametxt.setText(getString(R.string.neko_info_ver_name, BuildConfig.VERSION_NAME));
		codetxt.setText(getString(R.string.neko_info_ver_code, BuildConfig.VERSION_CODE));
		androidtxt.setText(getString(R.string.neko_info_android_ver, VERSION.SDK_INT));
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.details)
                .setIcon(R.drawable.ic_info)
                .setView(view)
				.setNegativeButton(android.R.string.ok, null)
                .show();
		});
}
	public void startRecovery(Context context) {
		isSave = false;
		isRestore = false;
		isDelete = false;
		BottomSheetDialog bottomsheet = new BottomSheetDialog(context);
		bottomsheet.setContentView(R.layout.neko_settings_bottomsheet);
		bottomsheet.setDismissWithAnimation(true);
		View bottomSheetInternal = bottomsheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
		BottomSheetBehavior.from(bottomSheetInternal).setPeekHeight(context.getResources().getDimensionPixelSize(R.dimen.bottomsheet));

		MaterialButton savebtn = bottomSheetInternal.findViewById(R.id.save);
		MaterialButton restorebtn = bottomSheetInternal.findViewById(R.id.restore);
		MaterialButton deletebtn = bottomSheetInternal.findViewById(R.id.delete);
		MaterialButton nextbtn = bottomSheetInternal.findViewById(R.id.next);
		MaterialTextView getperms = bottomSheetInternal.findViewById(R.id.force_get_perms);

		savebtn.setOnClickListener(view -> {
			if(!isSave) {
				isSave = true;
				isRestore = false;
				isDelete = false;
			} else {
				isSave = false;
			}
			updateRecoveryScreen(nextbtn, savebtn, restorebtn, deletebtn);
		});
		restorebtn.setOnClickListener(view -> {
			if(!isRestore) {
				isRestore = true;
				isSave = false;
				isDelete = false;
			} else {
				isRestore = false;
			}
			updateRecoveryScreen(nextbtn, savebtn, restorebtn, deletebtn);
		});
		deletebtn.setOnClickListener(view -> {
			if(!isDelete) {
				isDelete = true;
				isRestore = false;
				isSave = false;
			} else {
				isDelete = false;
			}
			updateRecoveryScreen(nextbtn, savebtn, restorebtn, deletebtn);
		});
		nextbtn.setOnClickListener(view -> {
			if(isSave) {
				bottomsheet.setContentView(R.layout.neko_settings_bottomsheet_save_file);
				savePrefs();
				new Thread() {
					@Override
					public void run() {
						try {
							Thread.sleep(2000);
							bottomsheet.dismiss();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				}.start();
			}
			if(isRestore) {
				bottomsheet.setContentView(R.layout.neko_settings_bottomsheet_select_file);
				MaterialButton select = bottomSheetInternal.findViewById(R.id.choose);
				select.setOnClickListener(view1 -> {
					openFile();
				    bottomsheet.dismiss();
						});
			}
			if(isDelete) {
				bottomsheet.setContentView(R.layout.neko_settings_bottomsheet_process);
				new Thread() {
					@Override
					public void run() {
						try {
							mPrefs.wipeData();
							Thread.sleep(1200);
							bottomsheet.dismiss();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						bottomSheetInternal.post(() ->
								new MaterialAlertDialogBuilder(context)
								.setTitle(R.string.backup_title)
								.setIcon(R.drawable.ic_backup_done)
								.setMessage(R.string.task_success)
								.setNegativeButton(android.R.string.ok, null)
								.show());
					}
				}.start();
			}
	});
		getperms.setOnClickListener(view -> checkPerms());
		bottomsheet.show();
	}
	private void updateRecoveryScreen(View view, View view2, View view3, View view4) {
		view.setEnabled(isSave || isDelete || isRestore);
		int color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondaryContainer, Color.GREEN);
		int transparent = this.getResources().getColor(android.R.color.transparent);
		if(isSave) {view2.setBackgroundColor(color); } else { view2.setBackgroundColor(transparent); }
		if(isRestore) { view3.setBackgroundColor(color); } else { view3.setBackgroundColor(transparent); }
		if(isDelete) { view4.setBackgroundColor(color); } else { view4.setBackgroundColor(transparent); }
	}
	@SuppressLint("SdCardPath")
	public void savePrefs() {
		File ff;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			ff = new File(this.getDataDir()
					+ "/shared_prefs/mPrefs.xml");
		} else {
			ff = new File("/data/data/"
					+ this.getPackageName()
					+ "/shared_prefs/mPrefs.xml");
		}
		Log.e("backup", ff.getPath());
		try {
			File f1 = new File(ff.getPath());
			File f2 = new File(Environment.getExternalStorageDirectory() + "/Neko11mBackup.xml");
			InputStream in = new FileInputStream(f1);

			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			showSaveDialog(true, "");
		} catch (IOException ex) {
			showSaveDialog(false, String.valueOf(ex));
		}
	}
	private void showSaveDialog(boolean success, String ex) {
		if(success) {
			new MaterialAlertDialogBuilder(this)
					.setTitle(R.string.backup_title)
					.setIcon(R.drawable.ic_backup_done)
					.setMessage(R.string.backup_done)
					.setNegativeButton(android.R.string.ok, null)
					.show();
		} else {
			new MaterialAlertDialogBuilder(this)
					.setTitle(R.string.backup_title)
					.setIcon(R.drawable.ic_backup_error)
					.setMessage(R.string.backup_failed)
					.setNegativeButton(android.R.string.ok, null)
					.setPositiveButton(R.string.details, (dialog, which) ->
							new MaterialAlertDialogBuilder(this)
									.setMessage("See: " + ex)
									.setNegativeButton(android.R.string.ok, null)
									.show())
					.show();
			}
		}
	private void openFile() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("text/xml");
		startActivityForResult(intent, 200);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode,
								 Intent resultData) {
		super.onActivityResult(requestCode, resultCode, resultData);
		if (requestCode == 200 && resultCode == Activity.RESULT_OK && resultData != null) {
			Context context = this;
			BottomSheetDialog bottomsheet = new BottomSheetDialog(context);
			bottomsheet.setContentView(R.layout.neko_settings_bottomsheet_process);
			bottomsheet.setDismissWithAnimation(true);
			View bottomSheetInternal = bottomsheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
			BottomSheetBehavior.from(bottomSheetInternal).setPeekHeight(context.getResources().getDimensionPixelSize(R.dimen.bottomsheet));
			bottomsheet.show();
				try {
					mPrefs.wipeData();
					Uri uri = resultData.getData();
					File file = new File(getRealPathFromURI(uri, this));
					InputStream is = new BufferedInputStream(new FileInputStream(file));
					BackupParser backuper = new BackupParser();
					backuper.parse(is, this);
					is.close();
					new Thread() {
						@Override
						public void run() {
							try {
								Thread.sleep(1100);
								bottomsheet.dismiss();
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
							bottomSheetInternal.post(() ->
									new MaterialAlertDialogBuilder(context)
											.setTitle(R.string.backup_title)
											.setIcon(R.drawable.ic_backup_done)
											.setMessage(R.string.restore_done)
											.setNegativeButton(android.R.string.ok, null)
											.show());
						}
					}.start();
				} catch (IOException | XmlPullParserException e) {
					bottomsheet.dismiss();
					new MaterialAlertDialogBuilder(this)
							.setTitle(R.string.backup_title)
							.setIcon(R.drawable.ic_backup_error)
							.setMessage(R.string.restore_failed)
							.setNegativeButton(android.R.string.ok, null)
							.setPositiveButton(R.string.details, (dialog, which) ->
									new MaterialAlertDialogBuilder(this)
											.setMessage("See: " + e)
											.setNegativeButton(android.R.string.ok, null)
											.show())
							.show();
					throw new RuntimeException(e);
				}
			}
		showSnackBar(getString(R.string.task_success), 5, cord);
		}
		//Thanks https://stackoverflow.com/a/72444629
	private static String getRealPathFromURI(Uri uri, Context context) {
		Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
		int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
		returnCursor.moveToFirst();
		String name = (returnCursor.getString(nameIndex));
		File file = new File(context.getFilesDir(), name);
		try {
			InputStream inputStream = context.getContentResolver().openInputStream(uri);
			FileOutputStream outputStream = new FileOutputStream(file);
			int read = 0;
			int maxBufferSize = 1 * 1024 * 1024;
			int bytesAvailable = inputStream.available();

			//int bufferSize = 1024;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);

			final byte[] buffers = new byte[bufferSize];
			while ((read = inputStream.read(buffers)) != -1) {
				outputStream.write(buffers, 0, read);
			}
			inputStream.close();
			outputStream.close();
		} catch (Exception e) {
			Log.e("ParseURI", "Exception. See: " + e);
		}
		return file.getPath();
	}
	public void checkPerms() {
		Intent i = new Intent();
		Uri uri = Uri.fromParts("package", getPackageName(), null);
		if (Build.VERSION.SDK_INT >= 30) {
			if (!Environment.isExternalStorageManager()) {
				i.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		} else {
			i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setData(uri);
			startActivity(i);
		}
	}
	private void showMenu(View v, @MenuRes int menuRes) {
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
	@SuppressLint("SuspiciousIndentation")
	private void setupScreen() {
	    opensettingsbtn = findViewById(R.id.opensettingsbtn);
        whiteswitch = findViewById(R.id.white_switch);
        linearcontrol = findViewById(R.id.linear_controls_enable);
		dyncolor = findViewById(R.id.dynamic_color);
		autowhiteswitch = findViewById(R.id.white_switch_auto);
		accentchoose = findViewById(R.id.choosetheme);
		sort_group = findViewById(R.id.sortgroup);	
		standartsort = findViewById(R.id.sortstandart);
		namesort = findViewById(R.id.sortname);
		controlsFirst = findViewById(R.id.is_first_controls);
		offsort = findViewById(R.id.sortoff);	
		limit_slider = findViewById(R.id.max_cat_line_slider);	
		cat_resizer = findViewById(R.id.cat_size_slider);	
		details = findViewById(R.id.get_neko_info);
		recovery = findViewById(R.id.recovery_btn);
	int THEME = nekoprefs.getInt("theme", 0);	
	int DARK_ENABLE = nekoprefs.getInt("darktheme", 0);	
	boolean LINEAR_CONTROL = nekoprefs.getBoolean("linear_control", false);	
    boolean CONTROLS_FIRST = nekoprefs.getBoolean("controlsFirst", false);
		dyncolor.setEnabled(VERSION.SDK_INT >= Build.VERSION_CODES.S);
        linearcontrol.setChecked(LINEAR_CONTROL);

		dyncolor.setChecked(THEME == 8);
		controlsFirst.setChecked(CONTROLS_FIRST);
		limit_slider.setValue(mPrefs.getCatsInLineLimit());
		cat_resizer.setValue(mPrefs.getCatIconSize());

		switch (DARK_ENABLE) {
			case 0 -> {
				whiteswitch.setChecked(false);
				autowhiteswitch.setEnabled(true);
			}
			case 1 -> whiteswitch.setChecked(true);
			case 2 -> {
				autowhiteswitch.setChecked(true);
				whiteswitch.setEnabled(false);
			}
			default -> whiteswitch.setChecked(false);
		}
		switch (mPrefs.getSortState()) {
			case 2 -> namesort.setChecked(true);
			case 1 -> standartsort.setChecked(true);
			case 0 -> offsort.setChecked(true);
		}
	}
}