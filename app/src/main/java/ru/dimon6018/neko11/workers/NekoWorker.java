/*
 * Copyright (C) 2020 The Android Open Source Project
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

import static ru.dimon6018.neko11.controls.CatControlsFragment.randomWater;
import static ru.dimon6018.neko11.ui.fragments.NekoLandFragment.CHAN_ID;
import static ru.dimon6018.neko11.workers.Cat.PURR;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ru.dimon6018.neko11.R;

public class NekoWorker extends Worker {

    public static int CAT_NOTIFICATION = 1;
    public static float CAT_CAPTURE_PROB = 1.02f; // generous

    public static long SECONDS = 1000;
    public static long MINUTES = 60 * SECONDS;

    public static float INTERVAL_JITTER_FRAC = 0.251f;

    public static String title_message;
    public static boolean isWorkScheduled;

    public NekoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void setupNotificationChannels(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager noman = context.getSystemService(NotificationManager.class);
            NotificationChannel eggChan = new NotificationChannel(CHAN_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            eggChan.setSound(Uri.EMPTY, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            eggChan.setVibrationPattern(PURR);
            eggChan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            noman.createNotificationChannel(eggChan);
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        Result state;
        Context context = getApplicationContext();
            try {
                triggerFoodResponse(context);
                final PrefState prefs = new PrefState(context);
                prefs.addCatsAllTime(1);
                prefs.clearActionsBlock();
                state = Result.success();
            } catch (Exception e) {
                state = Result.failure();
            }
            stopFoodWork(context);
            isWorkScheduled = false;
            PrefState.isLuckyBoosterActive = false;
            return state;
    }
    private static void triggerFoodResponse(Context context) {
        final PrefState prefs = new PrefState(context);
        int food = prefs.getFoodState();
        if (food != 0) {
            prefs.setFoodState(0); // nom
            if (Cat.RANDOM.nextFloat() <= CAT_CAPTURE_PROB) {
                Cat cat;
                List<Cat> cats = prefs.getCats();
                final int[] probs = context.getResources().getIntArray(R.array.food_new_cat_prob);
                final float waterLevel = prefs.getWaterState() / 2; // water is 0..200
                final float new_cat_prob = ((food < probs.length)
                        ? probs[food]
                        : waterLevel) / 100f;
                if (cats.size() == 0 || Cat.RANDOM.nextFloat() <= new_cat_prob & waterLevel >= 40f ) {
                    title_message = context.getString(R.string.notification_title);
                    cat = newRandomCat(context, prefs);
                    prefs.setWaterState(prefs.getWaterState() - randomWater);
                } else {
                    title_message = context.getString(R.string.notification_title_return);
                    cat = getExistingCat(prefs);
                }
                notifyCat(context, cat, title_message);
            }
        }
    }

    public static void notifyCat(Context context, Cat cat, String message) {
        title_message = message;
        NotificationManager noman = context.getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            final Notification.Builder builder = cat.buildNotificationP(context);
            noman.notify(cat.getShortcutId(), CAT_NOTIFICATION, builder.build());
        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            final Notification.Builder builder = cat.buildNotificationO(context);
            noman.notify(cat.getShortcutId(), CAT_NOTIFICATION, builder.build());
        } else {
            final NotificationCompat.Builder builder = cat.buildNotificationN(context);
            noman.notify(cat.getShortcutId(), CAT_NOTIFICATION, builder.build());
        }

    }

    public static Cat newRandomCat(Context context, PrefState prefs) {
        final Cat cat = Cat.create(context);
		Random random = new Random(cat.getSeed());	
        prefs.addCat(cat);
	 	prefs.addNCoins(random.nextInt(80 - 9) + 5);
        return cat;
    }

    public static Cat getExistingCat(PrefState prefs) {
        final List<Cat> cats = prefs.getCats();
        if (cats.size() == 0) return null;
        return cats.get(new Random().nextInt(cats.size()));
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    public static void scheduleFoodWork(Context context, int intervalMinutes) {
        long interval = intervalMinutes * MINUTES;
        long jitter = (long) (INTERVAL_JITTER_FRAC * interval);
        interval += (long) (Math.random() * (2 * jitter)) - jitter;

        OneTimeWorkRequest workFoodRequest =
                new OneTimeWorkRequest.Builder(NekoWorker.class)
                        .addTag("FOODWORK")
                        .setInitialDelay(interval / MINUTES, TimeUnit.MINUTES)
                        .build();
        WorkManager.getInstance(context).enqueue(workFoodRequest);
        Log.w("FOOD_TIME", String.valueOf(interval / MINUTES));
        isWorkScheduled = true;
    }
    public static void stopFoodWork(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("FOODWORK");
        isWorkScheduled = false;
    }
}
