/*
 * Copyright (C) 2020 The Android Open Source Project
 * Copyright (C) 2017, 2018, 2019 Christopher Blay <chris.b.blay@gmail.com>
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

package ru.dimon6018.neko11.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.dimon6018.neko11.R;

public class PrefState implements OnSharedPreferenceChangeListener {
    public static final String FILE_NAME = "mPrefs";
    private static final String FOOD_STATE = "food";
	private static final String SORT_STATE = "sort";
	private static final String CATS_LIMIT = "limit";
	public static final String ICON_SIZE = "size";
	private static final String NCOINS = "nCoins";
    public static final String TOY_STATE = "toy";
    public static final String WATER_STATE = "water";
    public static final String CAT_KEY_PREFIX = "cat:";
    public static final String CAT_KEY_PREFIX_MOOD = "mood:";
    public static final String MOOD_BOOSTER = "mood_booster";
    public static final String LUCKY_BOOSTER = "lucky_booster";
    private final Context mContext;
    private final SharedPreferences mPrefs;
    private PrefsListener mListener;

    public static boolean isLuckyBoosterActive;

    public PrefState(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(FILE_NAME, 0);
    }

    // Can also be used for renaming.
    public void addCat(Cat cat) {
        mPrefs.edit()
              .putString(CAT_KEY_PREFIX + (cat.getSeed()), cat.getName())
              .apply();
    }
    public void removeCat(Cat cat) {
        mPrefs.edit()
                .remove(CAT_KEY_PREFIX + (cat.getSeed()))
                .apply();
    }
    public void setMood(Cat cat, String mood) {
        mPrefs.edit()
                .putString(CAT_KEY_PREFIX_MOOD + (cat.getSeed()), mood)
                .apply();
    }
    public List<Cat> getCats() {
        ArrayList<Cat> cats = new ArrayList<>();
        Map<String, ?> map = mPrefs.getAll();
        for (String key : map.keySet()) {
            if (key.startsWith(CAT_KEY_PREFIX)) {
                long seed = Long.parseLong(key.substring(CAT_KEY_PREFIX.length()));
                String mood_value = mPrefs.getString(CAT_KEY_PREFIX_MOOD + seed, "");
                Cat cat = new Cat(mContext, seed, String.valueOf(map.get(key)));
                cat.setName(String.valueOf(map.get(key)));
                if(mood_value.equals("")) {
                    mood_value = mContext.getString(R.string.mood3);
                }
                setMood(cat, mood_value);
                cats.add(cat);
            }
        }
        return cats;
    }
    public String getMoodPref(Cat cat) {
        return mPrefs.getString(CAT_KEY_PREFIX_MOOD + cat.getSeed(), "");
    }
    public int getToyState() {
        return mPrefs.getInt(TOY_STATE, 0);
    }
    public void setToyState(int toystate) {
        mPrefs.edit().putInt(TOY_STATE, toystate).apply();
    }
    public int getFoodState() {
        return mPrefs.getInt(FOOD_STATE, 0);
    }
    public void setFoodState(int foodState) {
        mPrefs.edit().putInt(FOOD_STATE, foodState).apply();
    }
    public float getWaterState() {
        return mPrefs.getFloat(WATER_STATE, 0f);
    }
    public void setWaterState(float waterState) {
        mPrefs.edit().putFloat(WATER_STATE, waterState).apply();
    }

    public void setListener(PrefsListener listener) {
        mListener = listener;
        if (mListener != null) {
            mPrefs.registerOnSharedPreferenceChangeListener(this);
        } else {
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mListener.onPrefsChanged();
    }

    public interface PrefsListener {
        void onPrefsChanged();
    }
	public int getSortState() {
        return mPrefs.getInt(SORT_STATE, 1);
    }
    public void setSortState(int sortState) {
        mPrefs.edit().putInt(SORT_STATE, sortState).apply();
    }
	public int getCatsInLineLimit() {
        return mPrefs.getInt(CATS_LIMIT, 3);
    }
    public void setCatsInLineLimit(int limit) {
        mPrefs.edit().putInt(CATS_LIMIT, limit).apply();
    }
	public int getNCoins() {
        return mPrefs.getInt(NCOINS, 0);
    }
    public int getMoodBoosters() {
        return mPrefs.getInt(MOOD_BOOSTER, 0);
    }
    public int getLuckyBoosters() {
        return mPrefs.getInt(LUCKY_BOOSTER, 0);
    }
    public void addNCoins(int coins) {
		int currentCoins = getNCoins();
		int newCoins = currentCoins + coins;
        mPrefs.edit().putInt(NCOINS, newCoins).apply();
    }
	public void removeNCoins(int coins) {
		int currentCoins = getNCoins();
		int newCoins = currentCoins - coins;
        mPrefs.edit().putInt(NCOINS, newCoins).apply();
    }
    public void addMoodBooster(int boosters) {
        int currentBoosters = getMoodBoosters();
        int newBoosters = currentBoosters + boosters;
        mPrefs.edit().putInt(MOOD_BOOSTER, newBoosters).apply();
    }
    public void removeMoodBooster(int boosters) {
        int currentBoosters = getMoodBoosters();
        int newBoosters = currentBoosters - boosters;
        mPrefs.edit().putInt(MOOD_BOOSTER, newBoosters).apply();
    }
    public void addLuckyBooster(int boosters) {
        int currentBoosters = getLuckyBoosters();
        int newBoosters = currentBoosters + boosters;
        mPrefs.edit().putInt(LUCKY_BOOSTER, newBoosters).apply();
    }
    public void removeLuckyBooster(int boosters) {
        int currentBoosters = getLuckyBoosters();
        int newBoosters = currentBoosters - boosters;
        mPrefs.edit().putInt(LUCKY_BOOSTER, newBoosters).apply();
    }
	public int getCatIconSize() {
        return mPrefs.getInt(ICON_SIZE, 150);
    }
    public void setCatIconSize(int size) {
        mPrefs.edit().putInt(ICON_SIZE, size).apply();
    }
    public void wipeData() {
        mPrefs.edit().clear().apply();
    }
}
