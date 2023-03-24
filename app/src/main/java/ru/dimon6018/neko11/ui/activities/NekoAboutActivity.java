package ru.dimon6018.neko11.ui.activities;

import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;
import static ru.dimon6018.neko11.ui.fragments.NekoLand.EXPORT_BITMAP_SIZE;

import android.app.UiModeManager;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.view.WindowCompat;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;

import ru.dimon6018.neko11.workers.Cat;
import ru.dimon6018.neko11.R;

public class NekoAboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	SharedPreferences nekoprefs = getSharedPreferences(SETTINGS, MODE_PRIVATE);
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
		
		MaterialButton github = findViewById(R.id.github_button);
		github.setOnClickListener(v -> {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/queuejw/Neko11"));
		startActivity(intent);
		});
		setupCatImage();
    }
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
	private void setupCatImage() {
	ImageView imageViewCat = findViewById(R.id.imageViewCat);	
	Cat cat;
	cat = Cat.create(this);
	//Bitmap bitmap = cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE);
	imageViewCat.setImageBitmap(cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE));
	}
}
