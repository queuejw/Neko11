/*
 * Copyright (C) 2023 Dmitry Frolkov <dimon6018t@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.dimon6018.neko11.ui.oobe;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ru.dimon6018.neko11.R;
import com.google.android.material.elevation.SurfaceColors;
import androidx.core.view.WindowCompat;
import android.view.Window;
import androidx.core.view.WindowInsetsCompat;
import dev.chrisbanes.insetter.Insetter;

public class NekoOOBE extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
				
		setContentView(R.layout.neko_oobe);
		
        Toolbar toolbar = findViewById(R.id.toolbaroobe);
        setSupportActionBar(toolbar);
		
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		
		Insetter.builder()
       .padding(WindowInsetsCompat.Type.statusBars())
       .applyToView(toolbar);
	   
	    getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
	   
		getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container_oobe, NekoOOBE1Fragment.class, null)
                .commit();
    } 
}
