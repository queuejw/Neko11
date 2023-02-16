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

package ru.dimon6018.neko11.ui.fragments;

import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.card.MaterialCardView;

import ru.dimon6018.neko11.workers.NekoToyWorker;
import ru.dimon6018.neko11.NekoGeneralActivity;
import ru.dimon6018.neko11.workers.NekoWorker;
import ru.dimon6018.neko11.workers.PrefState;
import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.R.dimen;
import java.util.Random;

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

    int tileh;
    int tilew;

    private PrefState mPrefs;
	
	final static int foodMin = 10;
    final static int foodMax = 244;
    public final static int randomfood = new Random().nextInt((foodMax - foodMin) + 1) + foodMin;

    final static int foodMinS = 1;
    final static int foodMaxS = 10;
    public final static int foodstaterandom = new Random().nextInt((foodMaxS - foodMinS) + 1) + foodMinS;

    final static int waterMin = 10;
    final static int waterMax = 120;
    public final static int randomWater = new Random().nextInt((waterMax - waterMin) + 1) + foodMin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        tileh = requireActivity().getResources().getDimensionPixelSize(R.dimen.tile_height);
        tilew = requireActivity().getResources().getDimensionPixelSize(R.dimen.tile_width);

        int[] TOY_ICONS = {R.drawable.ic_toy_ball, R.drawable.ic_toy_fish, R.drawable.ic_toy_mouse, R.drawable.ic_toy_laser};
        int toyiconint = TOY_ICONS[new Random().nextInt(TOY_ICONS.length)];

        SharedPreferences nekoprefs = requireActivity().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);

        boolean LINEAR_CONTROL = nekoprefs.getBoolean("linear_control", false);
        int layout;

        if (LINEAR_CONTROL) {
            layout = R.layout.fragment_cat_controls_linear;
        } else {
            layout = R.layout.fragment_cat_controls;
        }
        View view = inflater.inflate(layout, container, false);
        mPrefs = new PrefState(getContext());
        mPrefs.setListener(this);

        int foodState = mPrefs.getFoodState();
        if (foodState != 0) {
        }
        waterstatetxt = view.findViewById(R.id.water_state);
        float waterml = mPrefs.getWaterState();
        if (waterml <= 0f) {
            mPrefs.setWaterState(0f);
            String waterstnull = getResources().getString(R.string.water_state_ml, waterml);
            waterstatetxt.setText(waterstnull);
        }

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

        foodcard.setMinimumHeight(tileh);
        foodcard.setMinimumWidth(tilew);

        watercard.setMinimumHeight(tileh);
        watercard.setMinimumWidth(tilew);

        toycard.setMinimumHeight(tileh);
        toycard.setMinimumWidth(tilew);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        foodcard.setOnClickListener(v -> {
            Log.i("CatControls", "Food Clicked");
            int currentstate = mPrefs.getFoodState();
            if (currentstate == 0) {
                foodcard.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_out));
                foodcard.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_in));
                Log.i("CatControls", "Register job");
                NekoWorker.scheduleFoodWork(getContext(), randomfood);
                mPrefs.setFoodState(foodstaterandom);
            } else {
                foodcard.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_out));
                foodcard.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_in));
                Log.i("CatControls", "Cancel job");
                mPrefs.setFoodState(0);
                NekoWorker.stopFoodWork();
            }
            updateTiles(view);
        });
        toycard.setOnClickListener(v -> {
            toycard.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_out));
            toycard.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_in));
            if (mPrefs.getToyState() == 0) {
                  mPrefs.setToyState(1);
                  NekoToyWorker.scheduleToyWork(getContext());
            } else {
                mPrefs.setToyState(0);
                NekoToyWorker.stopToyWork();
            }
            updateTiles(view);
        });
        watercard.setOnClickListener(v -> {
            watercard.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_out));
            watercard.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_in));
            mPrefs.setWaterState(200f);
            updateTiles(view);
		});
		watercard.setOnLongClickListener(v -> {
                watercard.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_out));
                watercard.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_in));
                mPrefs.setWaterState(0f);
                updateTiles(view);	
				return true;
        });
        updateTiles(view);
    }

    @Override
    public void onPrefsChanged() {
    }

    private void updateTiles(View view) {  
     //update food card
        int currentstate = mPrefs.getFoodState();
        if (currentstate == 0) {
            foodstatusimg.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_bowl));
            foodstatetxt.setText(R.string.control_food_status_empty);
            foodsub.setVisibility(View.VISIBLE);
            foodcard.setCardBackgroundColor((requireActivity()).getColor(R.color.foodbg2));
        } else {
            foodstatusimg.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_foodbowl_filled));
            foodstatetxt.setText(R.string.control_food_status_full);
            foodsub.setVisibility(View.INVISIBLE);
            foodcard.setCardBackgroundColor((requireActivity()).getColor(R.color.foodbg));
        }
		//update water card state
        float waterml = Math.round(mPrefs.getWaterState());
        String waterst = getResources().getString(R.string.water_state_ml, waterml);
        waterstatetxt.setText(waterst);
        if (waterml >= 100f) {
            watercard.setCardBackgroundColor((requireActivity()).getColor(R.color.waterbg));
            waterstatusimg.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_water_filled));
            waterstatesub.setVisibility(View.INVISIBLE);
        } else {
            watercard.setCardBackgroundColor((requireActivity()).getColor(R.color.waterbg2));
            waterstatusimg.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_water));
            waterstatesub.setVisibility(View.VISIBLE);
        }
        if (waterml <= 0) {
            watercard.setCardBackgroundColor((requireActivity()).getColor(R.color.waterbg3));
            waterstatusimg.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_water));
            waterstatesub.setVisibility(View.VISIBLE);
        }
		//update toy card state
        if(mPrefs.getToyState() >= 1) {
            toycard.setCardBackgroundColor(requireActivity().getColor(R.color.toybg));
            toystatus.setVisibility(View.VISIBLE);
            toysub.setVisibility(View.INVISIBLE);
        } else {
            toycard.setCardBackgroundColor(requireActivity().getColor(R.color.toybg2));
            toystatus.setVisibility(View.INVISIBLE);
            toysub.setVisibility(View.VISIBLE);
        }
    }
}