package ru.dimon6018.neko11.ui.activities;

import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import ru.dimon6018.neko11.NekoGeneralActivity;
import ru.dimon6018.neko11.R;

public class NekoWarning extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nbackup_load_warning);
        SharedPreferences nekoprefs = getSharedPreferences(SETTINGS, MODE_PRIVATE);
        Button btn = findViewById(R.id.continue_load_nb);
        Button bcp = findViewById(R.id.continue_backup);
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_warning)
                .setMessage(R.string.please_make_backup)
                .setNegativeButton(android.R.string.ok, null)
                .show();
        btn.setOnClickListener(view -> {
            nekoprefs.edit().putBoolean("backupFinished", true).apply();
            startActivity(new Intent(this, NekoGeneralActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK));;
        });
        bcp.setOnClickListener(view -> startActivity(new Intent(NekoWarning.this, NekoSettingsActivity.class)));
    }
}
