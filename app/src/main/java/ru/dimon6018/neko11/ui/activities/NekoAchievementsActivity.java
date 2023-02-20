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

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.button.MaterialButton;

import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.ui.fragments.NekoLand;

public class NekoAchievementsActivity extends AppCompatActivity {
	
	private int num;

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
		
		progress1 = findViewById(R.id.achiev_1_progress);
		progress2 = findViewById(R.id.achiev_2_progress);
		progress3 = findViewById(R.id.achiev_3_progress);
		progress4 = findViewById(R.id.achiev_4_progress);
		
		gift1 = findViewById(R.id.get_prize_1);
		gift2 = findViewById(R.id.get_prize_2);
		gift3 = findViewById(R.id.get_prize_3);
		gift4 = findViewById(R.id.get_prize_4);
		
        Toolbar toolbar = findViewById(R.id.toolbarachiev);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		
		getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
		
		Insetter.builder()
       .padding(WindowInsetsCompat.Type.statusBars())
       .applyToView(toolbar);
	   
	   int NUMCATS = nekoprefs.getInt("num", 0);
	   num = NUMCATS;
	   
	   progress1dstatus = num * 10;
	   progress2dstatus = num * 2;
	   progress3dstatus = num;
	   progress4dstatus = num / 10;
	   
	   progress1.setProgress(progress1dstatus);
	   progress2.setProgress(progress2dstatus);
	   progress3.setProgress(progress3dstatus); 
	   progress4.setProgress(progress4dstatus);
	   
	   if(progress1dstatus >= 100) {
         gift1.setEnabled(true);
	   }		   
	   if(progress2dstatus >= 100) {
         gift2.setEnabled(true);
	   }	
	   if(progress3dstatus >= 100) {
         gift3.setEnabled(true);
	   }
	   if(progress4dstatus >= 100) {
         gift4.setEnabled(true);
	   }	
	}
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
	}
}

