package ru.dimon6018.neko11.ui.activities;

import static ru.dimon6018.neko11.ui.fragments.NekoLandFragment.EXPORT_BITMAP_SIZE;

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

import ru.dimon6018.neko11.NekoApplication;
import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.workers.Cat;

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

		MaterialButton github = findViewById(R.id.github_button);
		MaterialButton tg = findViewById(R.id.telegram_button);
		github.setOnClickListener(v -> openWeb("https://github.com/queuejw/Neko11"));
		tg.setOnClickListener(v -> openWeb("https://t.me/nekoapp_news"));
		setupCatImage();
		CoordinatorLayout cord = findViewById(R.id.coordinatorabout);
		ViewCompat.setOnApplyWindowInsetsListener(cord, (v, insets) -> {
			int pB = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).top;
			v.setPadding(0, 0, 0, pB);
			return WindowInsetsCompat.CONSUMED;
		});
    }
	private void openWeb(String link) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		startActivity(intent);
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
