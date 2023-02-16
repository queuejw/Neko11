package ru.dimon6018.neko11.ui.activities;

import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;
import android.os.Bundle;
import android.view.MenuItem;
import android.content.Context;
import androidx.core.view.WindowCompat;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ru.dimon6018.neko11.R;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.button.MaterialButton;
import android.view.Window;
import com.google.android.material.color.DynamicColors;
import android.content.SharedPreferences;
import android.app.UiModeManager;

public class NekoAboutActivity extends AppCompatActivity {

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
		setContentView(R.layout.neko_about_activity);
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Toolbar toolbar = findViewById(R.id.toolbarabout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
	    getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
		
		MaterialButton gitlab = findViewById(R.id.gitlab_button);
		gitlab.setOnClickListener(v -> {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/project-neko/neko11"));
		startActivity(intent);
		});
    }
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
