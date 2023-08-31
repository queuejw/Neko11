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

package ru.dimon6018.neko11.controls;

import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.Random;

import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.workers.NekoToyWorker;
import ru.dimon6018.neko11.workers.NekoWorker;
import ru.dimon6018.neko11.workers.PrefState;

public class CatControlsFragment extends Fragment implements PrefState.PrefsListener {

    MaterialCardView foodcard;
    MaterialCardView watercard;
    MaterialCardView toycard;

    ImageView foodstatusimg;
    ImageView toystatusimg;
    ImageView waterstatusimg;

    MaterialTextView foodstatetxt;
    MaterialTextView toystatus;
    MaterialTextView toystatetxt;
    MaterialTextView toysub;
    MaterialTextView foodsub;
    MaterialTextView waterstatesub;
    MaterialTextView waterstatetxt;

    MaterialTextView booster_actived_sub;
	
    private PrefState mPrefs;
	SharedPreferences nekoprefs;
	
    public final static int randomfood = new Random().nextInt((244 - 10) + 1) + 10;
    public final static int foodstaterandom = new Random().nextInt((11 - 1) + 1) + 1;
    public final static int randomWater = new Random().nextInt((121 - 12) + 1) + 5;

    static int[] TOY_ICONS = {R.drawable.ic_toy_ball, R.drawable.ic_toy_fish, R.drawable.ic_toy_mouse, R.drawable.ic_toy_laser};
    static int toyiconint = TOY_ICONS[new Random().nextInt(TOY_ICONS.length)];
	
    @SuppressLint("SuspiciousIndentation")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        nekoprefs = requireActivity().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);

        boolean LINEAR_CONTROL = nekoprefs.getBoolean("linear_control", false);
		int state = nekoprefs.getInt("state", 1);
        int layout;
        if (LINEAR_CONTROL) {
            layout = R.layout.fragment_cat_controls_linear;
        } else {
            layout = R.layout.fragment_cat_controls;
        }
        View view = inflater.inflate(layout, container, false);
        mPrefs = new PrefState(getContext());
        mPrefs.setListener(this);

        toystatusimg = view.findViewById(R.id.toy_state_img);
        toystatusimg.setImageResource(toyiconint);
        foodcard = view.findViewById(R.id.card_food);
        watercard = view.findViewById(R.id.card_water);
        toycard = view.findViewById(R.id.card_toy);	
	    foodstatusimg = view.findViewById(R.id.food_state_view);
        toystatusimg = view.findViewById(R.id.toy_state_img);
        toystatus = view.findViewById(R.id.toy_status);
        waterstatusimg = view.findViewById(R.id.water_state_view);
        foodstatetxt = view.findViewById(R.id.food_status_txt);
        toystatetxt = view.findViewById(R.id.toy_state);
        waterstatetxt = view.findViewById(R.id.water_state);
        toysub = view.findViewById(R.id.toy_state_sub);
        foodsub = view.findViewById(R.id.foodsub);
        waterstatesub = view.findViewById(R.id.water_state_sub);
        booster_actived_sub = view.findViewById(R.id.booster_actived_sub);
		if(state == 2) {
		createTipDialog();	
		}
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getContext();
        foodcard.setOnClickListener(v -> {
			startAnim(foodcard);
            int currentstate = mPrefs.getFoodState();
            if (currentstate == 0) {
                if(PrefState.isLuckyBoosterActive) {
                    NekoWorker.scheduleFoodWork(context, randomfood / 4);
                } else {
                    NekoWorker.scheduleFoodWork(context, randomfood);
                }
                mPrefs.setFoodState(foodstaterandom);
            } else {
                mPrefs.setFoodState(0);
                NekoWorker.stopFoodWork(context);
            }
            updateTiles();
        });
        toycard.setOnClickListener(v -> {
			startAnim(toycard);
            if (mPrefs.getToyState() == 0) {
                  mPrefs.setToyState(1);
                  NekoToyWorker.scheduleToyWork(context);
            } else {
                mPrefs.setToyState(0);
                NekoToyWorker.stopToyWork(context);
            }
            updateTiles();
        });
        watercard.setOnClickListener(v -> {
			startAnim(watercard);
            mPrefs.addWaterMl(Math.round(mPrefs.getWaterState()));
            mPrefs.setWaterState(200f);
            updateTiles();
		});
        watercard.setOnLongClickListener(v -> {
			    startAnim(watercard);
                mPrefs.addWaterMl(Math.round(mPrefs.getWaterState()));
                mPrefs.setWaterState(0f);
                updateTiles();
                return true;
        });
        updateTiles();
    }

    @Override
    public void onPrefsChanged() {
        updateTiles();
    }

    private void updateTiles() {
     //update food card
        if (mPrefs.getFoodState() == 0) {
            foodstatusimg.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_bowl));
            foodstatetxt.setText(R.string.control_food_status_empty);
            foodsub.setVisibility(View.VISIBLE);
            foodcard.setCardBackgroundColor(getResources().getColor(R.color.foodbg2));
        } else {
            foodstatusimg.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_foodbowl_filled));
            foodstatetxt.setText(R.string.control_food_status_full);
            foodsub.setVisibility(View.INVISIBLE);
            foodcard.setCardBackgroundColor(getResources().getColor(R.color.foodbg));
        }
        if(PrefState.isLuckyBoosterActive) {
            booster_actived_sub.setVisibility(View.VISIBLE);
        } else {
            booster_actived_sub.setVisibility(View.GONE);
        }
		//update water card
        float waterml = Math.round(mPrefs.getWaterState());
        waterstatetxt.setText(getResources().getString(R.string.water_state_ml, waterml));
        if (waterml >= 100f) {
            watercard.setCardBackgroundColor(getResources().getColor(R.color.waterbg));
            waterstatusimg.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_water_filled));
            waterstatesub.setVisibility(View.INVISIBLE);
        } else {
            watercard.setCardBackgroundColor(getResources().getColor(R.color.waterbg2));
            waterstatusimg.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_water));
            waterstatesub.setVisibility(View.VISIBLE);
        }
		//update toy card
        if(mPrefs.getToyState() == 1) {
            toycard.setCardBackgroundColor(getResources().getColor(R.color.toybg));
            toystatus.setVisibility(View.VISIBLE);
            toysub.setVisibility(View.INVISIBLE);
        } else {
            toycard.setCardBackgroundColor(getResources().getColor(R.color.toybg2));
            toystatus.setVisibility(View.INVISIBLE);
            toysub.setVisibility(View.VISIBLE);
        }
    }
	private void startAnim(View view) {
	view.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
	view.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
	}
	private void createTipDialog() {
	new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.app_name_neko)
                    .setIcon(R.drawable.ic_bowl)
                    .setMessage(R.string.welcome_dialog_part3)
                    .setPositiveButton(android.R.string.ok, null).show();
	}
}