package ru.dimon6018.neko11.ui.activities;

import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import com.google.android.material.color.DynamicColors;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;

import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.ui.fragments.NekoLand;

public class NekoAchievementsActivity extends AppCompatActivity {

    private SharedPreferences nekoprefs;

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
	
	String message;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    nekoprefs = getSharedPreferences(SETTINGS, MODE_PRIVATE);
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
		
		progressSetup();
		
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		
        Toolbar toolbar = findViewById(R.id.toolbarachiev);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
	}
	private void progressSetup() {
		int NUMCATS = nekoprefs.getInt("num", 0);
	   
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
		
		boolean gift1_enabled = nekoprefs.getBoolean("gift1_enabled", true);
		boolean gift2_enabled = nekoprefs.getBoolean("gift2_enabled", true);
		boolean gift3_enabled = nekoprefs.getBoolean("gift3_enabled", true);
		boolean gift4_enabled = nekoprefs.getBoolean("gift4_enabled", true);
		
		if(progress1dstatus >= 100) {
		  gift1.setEnabled(true); 	
		  if(!gift1_enabled) {
			gift1.setText(R.string.gift_not_enabled);
		  }
	   }		   
	   if(progress2dstatus >= 100) {
		gift2.setEnabled(true);   
         if(!gift2_enabled)	{	
		 gift2.setText(R.string.gift_not_enabled);
		  }
	   }	
	   if(progress3dstatus >= 100) {
		gift3.setEnabled(true);   
         if(!gift3_enabled)	{
		 gift3.setText(R.string.gift_not_enabled);	 	  
		  }
	   }
	   if(progress4dstatus >= 100) {
		gift4.setEnabled(true);   
         if(!gift4_enabled)	{
		 gift4.setText(R.string.gift_not_enabled);	  
		  }
	   }	
	}
	private void checkGift() {
	boolean gift1_enabled = nekoprefs.getBoolean("gift1_enabled", true);
	boolean gift2_enabled = nekoprefs.getBoolean("gift2_enabled", true);
	boolean gift3_enabled = nekoprefs.getBoolean("gift3_enabled", true);
	boolean gift4_enabled = nekoprefs.getBoolean("gift4_enabled", true);	
	
	gift1.setOnClickListener(v -> {
	if(gift1_enabled) { 
	//gen $ save code and show dialog

	} else {
	//show dialog with code	
	}	  
	});
	gift2.setOnClickListener(v -> {
	if(gift1_enabled) { 
	//gen $ save code and show dialog
	} else {
	//show dialog with code	
	}	  
	});
	gift3.setOnClickListener(v -> {
	if(gift1_enabled) { 
	//gen $ save code and show dialog

	} else {
	//show dialog with code	
	}	  
	});
	gift4.setOnClickListener(v -> {
	if(gift1_enabled) { 
	//gen $ save code and show dialog
	message = "available";
    showDialog(message);
	} else {
	//show dialog with code	
	message = "unavailable";
    showDialog(message);
	}	  
	});
	}
	private void showDialog(String message) {
		new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.achievements)
				.setMessage(message)
                .setIcon(R.drawable.key)
				.setPositiveButton(android.R.string.ok, null)
                .show();
	}
	private void genCode(int num) {
	String[] seasons  = new String[] {"1", "a", "2", "b", "3", "c", "4", "d", "5", "e", "6"};
	for(int i = 0; i <= 8; i++) {
		
	}
	}
}

