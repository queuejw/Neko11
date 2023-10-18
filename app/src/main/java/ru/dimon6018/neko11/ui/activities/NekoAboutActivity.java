package ru.dimon6018.neko11.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.elevation.SurfaceColors;
import ru.dimon6018.neko11.NekoApplication;
import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.workers.Cat;

import static ru.dimon6018.neko11.ui.fragments.NekoLandFragment.EXPORT_BITMAP_SIZE;

public class NekoAboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		setTheme(NekoApplication.getNekoTheme(this));
        super.onCreate(savedInstanceState);
	    setContentView(R.layout.neko_about_activity);
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Toolbar toolbar = findViewById(R.id.toolbarabout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
		MaterialButton github = findViewById(R.id.github_button);
		MaterialButton tg = findViewById(R.id.telegram_button);
		github.setOnClickListener(v -> openWeb(this,"https://github.com/queuejw/Neko11"));
		tg.setOnClickListener(v -> openWeb(this,"https://t.me/nekoapp_news"));
		setupCatImage();
		CoordinatorLayout cord = findViewById(R.id.coordinatorabout);
		ViewCompat.setOnApplyWindowInsetsListener(cord, (v, insets) -> {
			int pB = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
			int tB = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
			v.setPadding(0, tB, 0, pB);
			return WindowInsetsCompat.CONSUMED;
		});
    }
	public static void openWeb(Activity activity, String link) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		activity.startActivity(intent);
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
	imageViewCat.setImageBitmap(cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE));
	}
}
