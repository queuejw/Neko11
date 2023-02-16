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

import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MenuItem;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Build;
import android.view.ViewGroup;
import android.app.UiModeManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.annotation.MenuRes;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import ru.dimon6018.neko11.R;

import ru.dimon6018.neko11.ui.oobe.NekoOOBE1Fragment;

public class NekoOOBE2Fragment extends Fragment {

    MaterialTextView tip;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.neko_oobe_frame2, container, false);

        MaterialButton back = view.findViewById(R.id.oobe_back_2);
		tip = view.findViewById(R.id.tip);
        MaterialButton next = view.findViewById(R.id.next_oobe_2);
		MaterialButton theme = view.findViewById(R.id.choosetheme_oobe);
		
		tip.setVisibility(View.INVISIBLE);
		
        back.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.container_oobe, NekoOOBE1Fragment.class, null)
                    .commit();
        });
		theme.setOnClickListener(v -> showMenu(v, R.menu.neko_colors));
        return view;
    }
	private void showMenu(View v, @MenuRes int menuRes) {
	SharedPreferences nekoprefs = requireActivity().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
	int THEME = nekoprefs.getInt("theme", 0);
    PopupMenu popup = new PopupMenu(getContext(), v);
    popup.getMenuInflater().inflate(menuRes, popup.getMenu());
    popup.setOnMenuItemClickListener(
        menuItem -> {
			SharedPreferences.Editor editor = nekoprefs.edit();
			switch (menuItem.getItemId()) {
				case R.id.pink_theme:
				  editor.putInt("theme", 1);
				  editor.apply();
				  tip.setVisibility(View.VISIBLE);
				  break;			
				case R.id.red_theme:
				  editor.putInt("theme", 2);
				  editor.apply(); 
				  tip.setVisibility(View.VISIBLE);
				  break;
				case R.id.orange_theme:
				  editor.putInt("theme", 3);
				  editor.apply();
				  tip.setVisibility(View.VISIBLE);
				  break;
				case R.id.green_theme:
				  editor.putInt("theme", 4);
				  editor.apply(); 
				  tip.setVisibility(View.VISIBLE);
				  break;
				case R.id.lime_theme:
				  editor.putInt("theme", 5);
				  editor.apply(); 
				  tip.setVisibility(View.VISIBLE);
				  break;
				case R.id.aqua_theme:
				  editor.putInt("theme", 6);
				  editor.apply(); 
				  tip.setVisibility(View.VISIBLE);
 				 break; 
				case R.id.blue_theme:
				  editor.putInt("theme", 7);
				  editor.apply(); 
				  tip.setVisibility(View.VISIBLE);
				  break;
				case R.id.purple_theme:
				  editor.putInt("theme", 0);
				  editor.apply();
				  tip.setVisibility(View.VISIBLE);
				  break;
			   default:
            	  editor.putInt("theme", 0);
				  editor.apply();
				  tip.setVisibility(View.VISIBLE);
                  break;
			}		
          return true;
        });	
    popup.show();
	  }
}