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
package ru.dimon6018.neko11.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.dimon6018.neko11.R
import ru.dimon6018.neko11.controls.CatControlsFragment
import ru.dimon6018.neko11.ui.activities.NekoSettingsActivity
import ru.dimon6018.neko11.ui.fragments.NekoLandFragment
import ru.dimon6018.neko11.workers.Cat.Companion.create
import ru.dimon6018.neko11.workers.NekoToiletWorker.Companion.stopToiletWork
import java.util.Random
import java.util.concurrent.TimeUnit

class NekoWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val state: Result
        val context = applicationContext
        state = try {
            stopToiletWork(context)
            triggerFoodResponse(context)
            val prefs = PrefState(context)
            prefs.clearActionsBlock()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
        stopFoodWork(context)
        isWorkScheduled = false
        PrefState.isLuckyBoosterActive = false
        return state
    }

    companion object {
        private var CAT_NOTIFICATION = 1
        private var CAT_CAPTURE_PROB = 1.02f // generous
        private var SECONDS: Long = 1000
        private var MINUTES = 60 * SECONDS
        private var INTERVAL_JITTER_FRAC = 0.251f
        var title_message: String? = null
        var isWorkScheduled = false
        @JvmStatic
        fun setupNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val noman = context.getSystemService(NotificationManager::class.java)
                val eggChan = NotificationChannel(NekoLandFragment.CHAN_ID,
                        context.getString(R.string.notification_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT)
                eggChan.setSound(Uri.EMPTY, Notification.AUDIO_ATTRIBUTES_DEFAULT)
                eggChan.setVibrationPattern(Cat.PURR)
                eggChan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                noman.createNotificationChannel(eggChan)
            }
        }

        private fun triggerFoodResponse(context: Context) {
            val prefs = PrefState(context)
            val food = prefs.foodState
            if (food != 0) {
                prefs.foodState = 0 // nom
                if (Cat.RANDOM.nextFloat() <= CAT_CAPTURE_PROB) {
                    val cat: Cat?
                    val cats = prefs.cats
                    val probs = context.resources.getIntArray(R.array.food_new_cat_prob)
                    val waterLevel = prefs.waterState / 2
                    val newCatProb: Float = (if (food < probs.size) probs[food] else waterLevel).toFloat()
                    if (cats.isEmpty() || (Cat.RANDOM.nextFloat() <= newCatProb) and (waterLevel >= 50f)) {
                        title_message = context.getString(R.string.notification_title)
                        cat = newRandomCat(context, prefs, false)
                        prefs.waterState -= CatControlsFragment.randomWater
                    } else {
                        title_message = context.getString(R.string.notification_title_return)
                        cat = getExistingCat(prefs)
                    }
                    notifyCat(context, cat, title_message)
                }
            }
        }

        fun notifyCat(context: Context, cat: Cat?, message: String?) {
            title_message = message
            val noman = context.getSystemService(NotificationManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val builder = cat!!.buildNotificationP(context)
                noman.notify(cat.shortcutId, CAT_NOTIFICATION, builder.build())
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val builder = cat!!.buildNotificationO(context)
                noman.notify(cat.shortcutId, CAT_NOTIFICATION, builder.build())
            } else {
                val builder = cat!!.buildNotificationN(context)
                noman.notify(cat.shortcutId, CAT_NOTIFICATION, builder.build())
            }
        }

        fun newRandomCat(context: Context, prefs: PrefState, giftMode: Boolean?): Cat {
            val nekoprefs = context.getSharedPreferences(NekoSettingsActivity.SETTINGS, Context.MODE_PRIVATE)
            val cat = create(context)
            var age = Random().nextInt(5)
            if(age == 0) {
                age = 1
            }
            prefs.setAge(cat, age)
            val cats = prefs.cats
            var a = cats.size
            val random = Random(cat.seed)
            prefs.addCat(cat)
            prefs.addNCoins(random.nextInt(125))
            if (!giftMode!!) {
                while (a != 0) {
                    a -= 1
                    if (cat == cats[a]) {
                        continue
                    }
                    if (Random().nextInt(10) < 5) {
                        val cat2 = cats[a]
                        val mood = prefs.getMoodPref(cat2)
                        prefs.setMood(cat2, mood - 1)
                        if (prefs.getMoodPref(cat2) <= 1) {
                            prefs.setCatDirty(true, cat2.seed)
                        }
                        if (prefs.getMoodPref(cat2) <= -3 && nekoprefs.getBoolean("gameplayFeature1", true)) {
                            prefs.setCatDirty(true, cat2.seed)
                            prefs.removeCat(cat2)
                            prefs.setRunDialog(true)
                        }
                    }
                }
            }
            return cat
        }

        fun getExistingCat(prefs: PrefState): Cat? {
            val cats = prefs.cats
            return if (cats.isEmpty()) null else cats[Random().nextInt(cats.size)]
        }

        fun scheduleFoodWork(context: Context?, intervalMinutes: Int) {
            var interval = intervalMinutes * MINUTES
            val jitter = (INTERVAL_JITTER_FRAC * interval).toLong()
            interval += (Math.random() * (2 * jitter)).toLong() - jitter
            val workFoodRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(NekoWorker::class.java)
                    .addTag("FOODWORK")
                    .setInitialDelay(interval / MINUTES, TimeUnit.MINUTES)
                    .build()
            WorkManager.getInstance(context!!).enqueue(workFoodRequest)
            isWorkScheduled = true
        }

        fun stopFoodWork(context: Context?) {
            WorkManager.getInstance(context!!).cancelAllWorkByTag("FOODWORK")
            isWorkScheduled = false
        }
    }
}
