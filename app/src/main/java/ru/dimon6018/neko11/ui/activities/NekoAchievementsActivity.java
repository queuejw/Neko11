package ru.dimon6018.neko11.ui.activities;

import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.elevation.SurfaceColors;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import dev.chrisbanes.insetter.Insetter;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;

import ru.dimon6018.neko11.R;

public class NekoAchievementsActivity extends AppCompatActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences nekoprefs = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
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
		setContentView(R.layout.neko_achievements_activity);
        Toolbar toolbar = findViewById(R.id.toolbarachiev);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		
		getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
		
		Insetter.builder()
       .padding(WindowInsetsCompat.Type.statusBars())
       .applyToView(toolbar);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
	}
}

