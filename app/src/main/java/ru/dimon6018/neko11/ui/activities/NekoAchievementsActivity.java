package ru.dimon6018.neko11.ui.activities;

import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;
import static ru.dimon6018.neko11.ui.fragments.NekoLandFragment.EXPORT_BITMAP_SIZE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

import java.util.Random;

import ru.dimon6018.neko11.NekoApplication;
import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.workers.Cat;
import ru.dimon6018.neko11.workers.PrefState;

public class NekoAchievementsActivity extends AppCompatActivity implements PrefState.PrefsListener {

	private SharedPreferences nekoprefs;
	private PrefState mPrefs;

	MaterialTextView coins;

	LinearProgressIndicator progress1;
	LinearProgressIndicator progress2;
	LinearProgressIndicator progress3;
	LinearProgressIndicator progress4;

	public int progress1dstatus;
	public int progress2dstatus;
	public int progress3dstatus;
	public int progress4dstatus;

	MaterialButton gift1;
	MaterialButton gift2;
	MaterialButton gift3;
	MaterialButton gift4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(NekoApplication.getNekoTheme(this));
		super.onCreate(savedInstanceState);
		setContentView(R.layout.neko_achievements_activity);

		mPrefs = new PrefState(this);
		mPrefs.setListener(this);
		nekoprefs = getSharedPreferences(SETTINGS, MODE_PRIVATE);
		progressSetup();

		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

		Toolbar toolbar = findViewById(R.id.toolbarachiev);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setupShop();
	}

	@Override
	public void onPrefsChanged() {
		progressSetup();
	}
	private void setupShop() {
		ImageView imageViewCat = findViewById(R.id.random_cat_icon);
		Cat cat;
		cat = Cat.create(this);
		imageViewCat.setImageBitmap(cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE));
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	private void progressSetup() {
		int NUMCATS = nekoprefs.getInt("num", 0);
		coins = findViewById(R.id.coins);
		coins.setText(getString(R.string.coins, mPrefs.getNCoins()));

		progress1dstatus = NUMCATS * 10;
		progress2dstatus = NUMCATS * 2;
		progress3dstatus = NUMCATS;
		progress4dstatus = NUMCATS / 10;

		progress1 = findViewById(R.id.achiev_1_progress);
		progress2 = findViewById(R.id.achiev_2_progress);
		progress3 = findViewById(R.id.achiev_3_progress);
		progress4 = findViewById(R.id.achiev_4_progress);

		progress1.setProgress(progress1dstatus);
		progress2.setProgress(progress2dstatus);
		progress3.setProgress(progress3dstatus);
		progress4.setProgress(progress4dstatus);

		gift1 = findViewById(R.id.get_prize_1);
		gift2 = findViewById(R.id.get_prize_2);
		gift3 = findViewById(R.id.get_prize_3);
		gift4 = findViewById(R.id.get_prize_4);

		MaterialCardView mystery = findViewById(R.id.mystery_box_card);
		MaterialCardView cat = findViewById(R.id.random_cat_card);

		boolean gift1_enabled = nekoprefs.getBoolean("gift1_enabled", true);
		boolean gift2_enabled = nekoprefs.getBoolean("gift2_enabled", true);
		boolean gift3_enabled = nekoprefs.getBoolean("gift3_enabled", true);
		boolean gift4_enabled = nekoprefs.getBoolean("gift4_enabled", true);

		if (progress1dstatus >= 100) {
			gift1.setEnabled(true);
			if (!gift1_enabled) {
				gift1.setText(R.string.gift_not_enabled);
			}
		}
		if (progress2dstatus >= 100) {
			gift2.setEnabled(true);
			if (!gift2_enabled) {
				gift2.setText(R.string.gift_not_enabled);
			}
		}
		if (progress3dstatus >= 100) {
			gift3.setEnabled(true);
			if (!gift3_enabled) {
				gift3.setText(R.string.gift_not_enabled);
			}
		}
		if (progress4dstatus >= 100) {
			gift4.setEnabled(true);
			if (!gift4_enabled) {
				gift4.setText(R.string.gift_not_enabled);
			}
		}
		checkGift();

		mystery.setOnClickListener(view -> {
			new MaterialAlertDialogBuilder(this)
					.setTitle(R.string.achievements)
					.setMessage("Открывая этот загадочный кейс вы можете получить что-то из этих вещей: Случайный кот, NCoins, Шляпы.")
					.setIcon(R.drawable.key)
					.setNegativeButton(android.R.string.cancel, null
					).setPositiveButton(R.string.open, (dialog, id) -> OpenMysteryBox()
					).show();
		});
		cat.setOnClickListener(view -> {
			new MaterialAlertDialogBuilder(this)
					.setTitle(R.string.achievements)
					.setMessage("Вы точно хотите купить нового котика?")
					.setIcon(R.drawable.key)
					.setNegativeButton(android.R.string.cancel, null
					).setPositiveButton(android.R.string.yes, (dialog, id) -> GetNewCat()
					).show();
		});
	}

	private void GetNewCat() {
		String mes;
		Drawable ico;
		if (mPrefs.getNCoins() < 150) {
			mes = "Не хватает NCoins. Поймайте котов, чтобы получать их";
			 ico = AppCompatResources.getDrawable(this, R.drawable.key);
		} else {
			Cat cat = Cat.create(this);
			mPrefs.addCat(cat);
			mPrefs.removeNCoins(150);
			ico = new BitmapDrawable(getResources(), cat.createBitmap(24,24));
			mes = "Новый котик теперь живёт у вас. Проверьте его в коллекции";
		}
		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.achievements)
				.setMessage(mes)
				.setIcon(ico)
				.setNegativeButton(android.R.string.ok, null
				).show();
	}
	private void OpenMysteryBox() {
		if (mPrefs.getNCoins() < 300) {
			new MaterialAlertDialogBuilder(this)
					.setTitle(R.string.achievements)
					.setMessage("Не хватает NCoins. Поймайте котов, чтобы получать их")
					.setIcon(R.drawable.key)
					.setNegativeButton(android.R.string.ok, null
					).show();
		} else {
			new MaterialAlertDialogBuilder(this)
					.setTitle(R.string.achievements)
					.setMessage("Оплата прошла успешно, но эта функция еще не готова.")
					.setIcon(R.drawable.key)
					.setNegativeButton(android.R.string.ok, null
					).show();
		}
	}
	private void checkGift() {
		boolean gift1_enabled = nekoprefs.getBoolean("gift1_enabled", true);
		boolean gift2_enabled = nekoprefs.getBoolean("gift2_enabled", true);
		boolean gift3_enabled = nekoprefs.getBoolean("gift3_enabled", true);
		boolean gift4_enabled = nekoprefs.getBoolean("gift4_enabled", true);

		gift1.setOnClickListener(v -> {
			if (gift1_enabled) {
				genCode(1);
			} else {
				getCode(1);
			}
		});
		gift2.setOnClickListener(v -> {
			if (gift2_enabled) {
				genCode(2);
			} else {
				getCode(2);
			}
		});
		gift3.setOnClickListener(v -> {
			if (gift3_enabled) {
				genCode(3);
			} else {
				getCode(3);
			}
		});
		gift4.setOnClickListener(v -> {
			if (gift4_enabled) {
				genCode(4);
			} else {
				getCode(4);
			}
		});
	}

	private void genCode(int num) {
		Random random = new Random();
		String[] symbols = new String[]{"1", "a", "2", "b", "3", "c", "4", "d", "5", "e", "6"};
		String code = "";
		SharedPreferences.Editor editor = nekoprefs.edit();
		for (int i = 0; i <= 11; i++) {
			code = code + symbols[random.nextInt(symbols.length)];
		}
		String message = getString(R.string.new_code_generated, code);
		final String Copycode = code;
		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.achievements)
				.setMessage(message)
				.setIcon(R.drawable.key)
				.setPositiveButton(android.R.string.ok, null
				).setNegativeButton(R.string.copy, (dialog, id) -> copyCode(Copycode)
				).show();
		switch (num) {
			case 1 -> {
				editor.putString("code1", code);
				editor.putBoolean("gift1_enabled", false);
			}
			case 2 -> {
				editor.putString("code2", code);
				editor.putBoolean("gift2_enabled", false);
			}
			case 3 -> {
				editor.putString("code3", code);
				editor.putBoolean("gift3_enabled", false);
			}
			case 4 -> {
				editor.putString("code4", code);
				editor.putBoolean("gift4_enabled", false);
			}
		}
		editor.apply();
		progressSetup();
	}

	private void getCode(int num) {
		String currentCode = switch (num) {
			case 1 -> nekoprefs.getString("code1", "");
			case 2 -> nekoprefs.getString("code2", "");
			case 3 -> nekoprefs.getString("code3", "");
			case 4 -> nekoprefs.getString("code4", "");
			default -> "";
		};
		String message = getString(R.string.get_old_code, currentCode);
		final String code = currentCode;
		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.achievements)
				.setMessage(message)
				.setIcon(R.drawable.key)
				.setPositiveButton(android.R.string.ok, null)
				.setNegativeButton(R.string.copy, (dialog, id) -> copyCode(code)
				).show();
		progressSetup();
	}

	private void copyCode(String code) {
		ClipboardManager clipbrd = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("promo", code);
		clipbrd.setPrimaryClip(clip);
	}
	@Override
	public void onPause() {
		super.onPause();
		mPrefs.setListener(null);
	}
	@Override
	public void onResume() {
		super.onResume();
		mPrefs.setListener(this);
	}
}


