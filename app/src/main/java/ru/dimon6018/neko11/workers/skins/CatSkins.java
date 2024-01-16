package ru.dimon6018.neko11.workers.skins;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.workers.PrefState;

public class CatSkins {
    public static Drawable getCatHat(Context context, Long seed) {
        Drawable result;
        PrefState mPrefs = new PrefState(context);
        switch (mPrefs.getCatHatCode(seed)) {
            case 0 -> result = AppCompatResources.getDrawable(context, R.drawable.nothing);
            case 1 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_1);
            case 2 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_2);
            case 3 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_3);
            case 4 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_4);
            case 5 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_5);
            case 6 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_6);
            case 7 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_7);
            case 8 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_8);
            case 9 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_9);
            case 10 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_10);
            case 11 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_11);
            case 12 -> result = AppCompatResources.getDrawable(context, R.mipmap.hat_12);
            default -> result = AppCompatResources.getDrawable(context, R.drawable.nothing);
        }
        return result;
    }
    public static Drawable getCatSuit(Context context, Long seed) {
        Drawable result;
        PrefState mPrefs = new PrefState(context);
        switch (mPrefs.getCatSuitCode(seed)) {
            case 0 -> result = AppCompatResources.getDrawable(context, R.drawable.nothing);
            case 1 -> result = AppCompatResources.getDrawable(context, R.mipmap.suit_1);
            case 2 -> result = AppCompatResources.getDrawable(context, R.mipmap.suit_2);
            case 3 -> result = AppCompatResources.getDrawable(context, R.mipmap.suit_3);
            case 4 -> result = AppCompatResources.getDrawable(context, R.mipmap.suit_4);
            case 5 -> result = AppCompatResources.getDrawable(context, R.mipmap.suit_5);
            case 6 -> result = AppCompatResources.getDrawable(context, R.mipmap.suit_6);
            case 7 -> result = AppCompatResources.getDrawable(context, R.mipmap.suit_7);
            case 8 -> result = AppCompatResources.getDrawable(context, R.mipmap.suit_8);
            case 9 -> result = AppCompatResources.getDrawable(context, R.mipmap.suit_9);
            case 10 -> result = AppCompatResources.getDrawable(context, R.mipmap.suit_10);
            case 11 -> result = AppCompatResources.getDrawable(context, R.mipmap.suit_11);
            default -> result = AppCompatResources.getDrawable(context, R.drawable.nothing);
        }
        return result;
    }
    public static Drawable getCatDirty(Context context, Long seed) {
        Drawable result;
        PrefState mPrefs = new PrefState(context);
        if (mPrefs.getCatDirty(seed)) {
            result = AppCompatResources.getDrawable(context, R.drawable.cat_dirt);
        } else {
            result = AppCompatResources.getDrawable(context, R.drawable.nothing);
        }
        return result;
    }
}
