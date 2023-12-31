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
package ru.dimon6018.neko11.workers

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.dimon6018.neko11.R
import java.util.Random
import java.util.concurrent.TimeUnit

class NekoToyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val state: Result
        val context = applicationContext
        val prefs = PrefState(context)
        val meowArray = context.resources.getStringArray(R.array.toy_messages)
        val meowMeowString = meowArray[Random().nextInt(meowArray.size)]
        state = try {
            val cat = NekoWorker.getExistingCat(prefs)
            if (cat != null) {
                NekoWorker.notifyCat(context, cat, meowMeowString)
                prefs.setMood(cat, 5)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
        prefs.toyState = 0
        return state
    }

    companion object {
        fun scheduleToyWork(context: Context?) {
            val toymin = 15
            val toymax = 60
            val toydelayrandom = Random().nextInt(toymax - toymin + 1) + toymin
            val workToyRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(NekoToyWorker::class.java)
                    .addTag("TOYWORK")
                    .setInitialDelay(toydelayrandom.toLong(), TimeUnit.MINUTES)
                    .build()
            WorkManager.getInstance(context!!).enqueue(workToyRequest)
        }

        fun stopToyWork(context: Context?) {
            WorkManager.getInstance(context!!).cancelAllWorkByTag("TOYWORK")
        }
    }
}
