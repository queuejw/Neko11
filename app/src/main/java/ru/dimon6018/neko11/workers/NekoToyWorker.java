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

package ru.dimon6018.neko11.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import ru.dimon6018.neko11.R;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NekoToyWorker extends Worker {
	
    public NekoToyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Result state;
        Context context = getApplicationContext();
        final PrefState prefs = new PrefState(context);
		Random random = new Random();
		String[] meow_array = context.getResources().getStringArray(R.array.toy_messages);
		String meow_meow_string = meow_array[random.nextInt(meow_array.length)];
        try {		
            NekoWorker.notifyCat(context, NekoWorker.getExistingCat(prefs), meow_meow_string);
            state = Result.success();
        } catch (Exception e) {
            state = Result.failure();
        }
        prefs.setToyState(0);
        return state;
    }
    public static void scheduleToyWork(Context context) {
        int toymin = 10;
        int toymax = 60;
        int toydelayrandom = new Random().nextInt(toymax - toymin + 1) + toymin;

        OneTimeWorkRequest workToyRequest =
                new OneTimeWorkRequest.Builder(NekoToyWorker.class)
                        .addTag("TOYWORK")
                        .setInitialDelay(toydelayrandom, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(context).enqueue(workToyRequest);
    }
    public static void stopToyWork(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("TOYWORK");
    }
}
