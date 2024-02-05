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

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import ru.dimon6018.neko11.NekoGeneralActivity
import ru.dimon6018.neko11.R
import ru.dimon6018.neko11.ui.fragments.NekoLandFragment
import ru.dimon6018.neko11.workers.skins.CatSkins
import java.io.ByteArrayOutputStream
import java.lang.reflect.InvocationTargetException
import java.util.Random
import kotlin.math.abs
import kotlin.math.min


/** It's a cat.  */
class Cat(context: Context, seed: Long, name: String?) : Drawable() {
    private var mNotSoRandom: Random? = null
    private var mBitmap: Bitmap? = null
    var name: String? = null
    val seed: Long
    var bodyColor: Int
    val age: Int
    val status: String
    private var mFirstMessage: String
    private val mBowTie: Boolean
    @Synchronized
    private fun notSoRandom(seed: Long): Random {
        if (mNotSoRandom == null) {
            mNotSoRandom = Random()
            mNotSoRandom!!.setSeed(seed)
        }
        return mNotSoRandom!!
    }

    private val d: CatParts

    init {
        d = CatParts(context, seed)
        val prefs = PrefState(context)
        val res = context.resources
        this.seed = seed
        this.name = name
        val nsr = notSoRandom(seed)
        // age
        age = prefs.getCatAge(seed)
        // set funny status
        status = res.getStringArray(R.array.cat_status)[nsr.nextInt(res.getStringArray(R.array.cat_status).size)]
        // body color
        bodyColor = chooseP(nsr, P_BODY_COLORS)
        mBowTie = nsr.nextFloat() < 0.1f
        if (bodyColor == 0) bodyColor = Color.HSVToColor(floatArrayOf(
                nsr.nextFloat() * 360f, frandrange(nsr, 0.5f, 1f), frandrange(nsr, 0.5f, 1f)))
        tint(bodyColor, d.body, d.head, d.leg1, d.leg2, d.leg3, d.leg4, d.tail,
                d.leftEar, d.rightEar, d.foot1, d.foot2, d.foot3, d.foot4, d.tailCap)
        tint(0x20000000, d.leg2Shadow, d.tailShadow)
        if (isDark(bodyColor)) {
            tint(-0x1, d.leftEye, d.rightEye, d.mouth, d.nose)
        }
        tint(if (isDark(bodyColor)) -0x106566 else 0x20D50000, d.leftEarInside, d.rightEarInside)
        tint(chooseP(nsr, P_BELLY_COLORS), d.belly)
        tint(chooseP(nsr, P_BELLY_COLORS), d.back)
        val faceColor = chooseP(nsr, P_BELLY_COLORS)
        tint(faceColor, d.faceSpot)
        if (!isDark(faceColor)) {
            tint(-0x1000000, d.mouth, d.nose)
        }
        if (nsr.nextFloat() < 0.25f) {
            tint(-0x1, d.foot1, d.foot2, d.foot3, d.foot4)
        } else {
            if (nsr.nextFloat() < 0.25f) {
                tint(-0x1, d.foot1, d.foot3)
            } else if (nsr.nextFloat() < 0.25f) {
                tint(-0x1, d.foot2, d.foot4)
            } else if (nsr.nextFloat() < 0.1f) {
                tint(-0x1, choose(nsr, d.foot1!!, d.foot2!!, d.foot3!!, d.foot4!!) as Drawable)
            }
        }
        tint(if (nsr.nextFloat() < 0.333f) -0x1 else bodyColor, d.tailCap)
        val capColor = chooseP(nsr, if (isDark(bodyColor)) P_LIGHT_SPOT_COLORS else P_DARK_SPOT_COLORS)
        tint(capColor, d.cap)
        val collarColor = chooseP(nsr, P_COLLAR_COLORS)
        tint(collarColor, d.collar)
        tint(if (mBowTie) collarColor else 0, d.bowtie)
        val messages = res.getStringArray(
                if (nsr.nextFloat() < 0.1f) R.array.rare_cat_messages else R.array.cat_messages)
        mFirstMessage = choose(nsr, *messages) as String
        if (nsr.nextFloat() < 0.5f) mFirstMessage = mFirstMessage + mFirstMessage + mFirstMessage
    }

    @RequiresApi(VERSION_CODES.P)
    fun buildNotificationP(context: Context): Notification.Builder {
        val extras = Bundle()
        extras.putString("android.substName", context.getString(R.string.notification_name))
        val notificationIcon = createNotificationLargeIcon(context)
        val intent = Intent(Intent.ACTION_MAIN)
                .setClass(context, NekoGeneralActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val shortcut = ShortcutInfo.Builder(context, shortcutId)
                .setActivity(intent.component!!)
                .setIntent(intent)
                .setShortLabel(name!!)
                .setIcon(createShortcutIcon(context))
                .build()
        context.getSystemService(ShortcutManager::class.java).addDynamicShortcuts(listOf(shortcut))
        return Notification.Builder(context, NekoLandFragment.CHAN_ID)
                .setSmallIcon(Icon.createWithResource(context, R.drawable.stat_icon))
                .setLargeIcon(notificationIcon)
                .setColor(bodyColor)
                .setContentTitle(NekoWorker.title_message)
                .setShowWhen(true)
                .setCategory(Notification.CATEGORY_STATUS)
                .setContentText(name)
                .setContentIntent(
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(true)
                .setStyle(Notification.MessagingStyle(createPerson(context))
                        .addMessage(NekoWorker.title_message + mFirstMessage, System.currentTimeMillis(), createPerson(context))
                        .setConversationTitle(name)
                )
                .setShortcutId(shortcutId)
                .addExtras(extras)
    }

    @RequiresApi(VERSION_CODES.P)
    private fun createPerson(context: Context): Person {
        val notificationIcon = createNotificationLargeIcon(context)
        return Person.Builder()
                .setName(name)
                .setIcon(notificationIcon)
                .setBot(true)
                .setKey(shortcutId)
                .build()
    }

    @RequiresApi(VERSION_CODES.O)
    fun buildNotificationO(context: Context): Notification.Builder {
        val extras = Bundle()
        extras.putString("android.substName", context.getString(R.string.notification_name))
        val notificationIcon = createNotificationLargeIcon(context)
        val intent = Intent(Intent.ACTION_MAIN)
                .setClass(context, NekoGeneralActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val shortcut = ShortcutInfo.Builder(context, shortcutId)
                .setActivity(intent.component!!)
                .setIntent(intent)
                .setShortLabel(name!!)
                .setIcon(createShortcutIcon(context))
                .build()
        context.getSystemService(ShortcutManager::class.java).addDynamicShortcuts(listOf(shortcut))
        return Notification.Builder(context, NekoLandFragment.CHAN_ID)
                .setSmallIcon(Icon.createWithResource(context, R.drawable.stat_icon))
                .setLargeIcon(notificationIcon)
                .setColor(bodyColor)
                .setContentTitle(NekoWorker.title_message + mFirstMessage)
                .setShowWhen(true)
                .setCategory(Notification.CATEGORY_STATUS)
                .setContentText(name)
                .setContentIntent(
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(true)
                .setShortcutId(shortcutId)
                .addExtras(extras)
    }

    fun buildNotificationM(context: Context): NotificationCompat.Builder {
        val extras = Bundle()
        extras.putString("android.substName", context.getString(R.string.app_name))
        val intent = Intent(Intent.ACTION_MAIN)
                .setClass(context, NekoGeneralActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return NotificationCompat.Builder(context, NekoLandFragment.CHAN_ID)
                .setSmallIcon(R.drawable.stat_icon)
                .setContentTitle(NekoWorker.title_message + mFirstMessage)
                .setContentText(name)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(true)
                .setVibrate(PURR)
    }
    override fun draw(canvas: Canvas) {
        val w = min(getBounds().width(), getBounds().height())
        if (mBitmap == null || mBitmap!!.getWidth() != w || mBitmap!!.getHeight() != w) {
            mBitmap = Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888)
            val bitCanvas = Canvas(mBitmap!!)
            slowDraw(bitCanvas, 0, 0, w, w)
        }
        canvas.drawBitmap(mBitmap!!, 0f, 0f, null)
    }

    private fun slowDraw(canvas: Canvas, x: Int, y: Int, w: Int, h: Int) {
        for (i in d.drawingOrdr.indices) {
            val d2 = d.drawingOrdr[i]
            if(d2 == d.hat) {
                d2!!.setBounds(x, y / 10, x + w, ((y + h) / 1.15f).toInt())
                d2.draw(canvas)
                continue
            }
            if(d2 == d.suit) {
                d2!!.setBounds(x, y * 2, x + w, (y + h) + 5)
                d2.draw(canvas)
                continue
            }
            d2!!.setBounds(x, y, x + w, y + h)
            d2.draw(canvas)
        }
    }
    fun createIconBitmap(w: Int, h: Int, icon: Int): Bitmap {
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val pt = Paint()
        val hsv = FloatArray(3)
        Color.colorToHSV(bodyColor, hsv)
        hsv[2] = if (hsv[2] > 0.5f) hsv[2] - 0.25f else hsv[2] + 0.25f
        pt.setColor(Color.HSVToColor(hsv))
        val r = (w / 2).toFloat()
        when (icon) {
            0 -> {}
            1 -> canvas.drawCircle(r, r, r, pt)
            2 -> canvas.drawColor(Color.HSVToColor(hsv))
        }
        val m = w / 10
        slowDraw(canvas, m, m, w - m - m, h - m - m)
        return result
    }

    @RequiresApi(VERSION_CODES.M)
    private fun createNotificationLargeIcon(context: Context): Icon? {
        return if (Build.VERSION.SDK_INT >= VERSION_CODES.P) {
            recompressIconP(createIcon(context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width), context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height), PrefState(context).getIconBackground()))
        } else {
            recompressIconO(createIconBitmap(context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width), context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height),  PrefState(context).getIconBackground()))
        }
    }

    @RequiresApi(VERSION_CODES.M)
    private fun createShortcutIcon(context: Context): Icon {
        return createIcon(context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width), context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height), PrefState(context).getIconBackground())
    }

    @RequiresApi(VERSION_CODES.M)
    fun createIcon(w: Int, h: Int, icon: Int): Icon {
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val pt = Paint()
        val hsv = FloatArray(3)
        Color.colorToHSV(bodyColor, hsv)
        hsv[2] = if (hsv[2] > 0.5f) hsv[2] - 0.25f else hsv[2] + 0.25f
        pt.setColor(Color.HSVToColor(hsv))
        val r = (w / 2).toFloat()
        when (icon) {
            0 -> {}
            1 -> canvas.drawCircle(r, r, r, pt)
            2 -> canvas.drawColor(Color.HSVToColor(hsv))
        }
        val m = w / 10
        slowDraw(canvas, m, m, w - m - m, h - m - m)
        return Icon.createWithBitmap(result)
    }
    override fun setAlpha(i: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int { return PixelFormat.TRANSLUCENT }

    val shortcutId: String
        get() = if (ALL_CATS_IN_ONE_CONVERSATION) GLOBAL_SHORTCUT_ID else SHORTCUT_ID_PREFIX + seed

    class CatParts(context: Context?, seed: Long) {
        var leftEar: Drawable?
        var rightEar: Drawable?
        var rightEarInside: Drawable?
        var leftEarInside: Drawable?
        var hat: Drawable?
        var head: Drawable?
        var faceSpot: Drawable?
        var cap: Drawable?
        var mouth: Drawable?
        var body: Drawable?
        var suit: Drawable?
        var foot1: Drawable?
        var leg1: Drawable?
        var foot2: Drawable?
        var leg2: Drawable?
        var foot3: Drawable?
        var leg3: Drawable?
        var foot4: Drawable?
        var leg4: Drawable?
        var tail: Drawable?
        var leg2Shadow: Drawable?
        var tailShadow: Drawable?
        var tailCap: Drawable?
        var belly: Drawable?
        var back: Drawable?
        var rightEye: Drawable?
        var leftEye: Drawable?
        var nose: Drawable?
        var bowtie: Drawable?
        var collar: Drawable?
        private var dirt: Drawable?
        var drawingOrdr: Array<Drawable?>

        init {
            body = AppCompatResources.getDrawable(context!!, R.drawable.body)
            head = AppCompatResources.getDrawable(context, R.drawable.head)
            leg1 = AppCompatResources.getDrawable(context, R.drawable.leg1)
            leg2 = AppCompatResources.getDrawable(context, R.drawable.leg2)
            leg3 = AppCompatResources.getDrawable(context, R.drawable.leg3)
            leg4 = AppCompatResources.getDrawable(context, R.drawable.leg4)
            tail = AppCompatResources.getDrawable(context, R.drawable.tail)
            leftEar = AppCompatResources.getDrawable(context, R.drawable.left_ear)
            rightEar = AppCompatResources.getDrawable(context, R.drawable.right_ear)
            rightEarInside = AppCompatResources.getDrawable(context, R.drawable.right_ear_inside)
            leftEarInside = AppCompatResources.getDrawable(context, R.drawable.left_ear_inside)
            faceSpot = AppCompatResources.getDrawable(context, R.drawable.face_spot)
            cap = AppCompatResources.getDrawable(context, R.drawable.cap)
            mouth = AppCompatResources.getDrawable(context, R.drawable.mouth)
            foot4 = AppCompatResources.getDrawable(context, R.drawable.foot4)
            foot3 = AppCompatResources.getDrawable(context, R.drawable.foot3)
            foot1 = AppCompatResources.getDrawable(context, R.drawable.foot1)
            foot2 = AppCompatResources.getDrawable(context, R.drawable.foot2)
            leg2Shadow = AppCompatResources.getDrawable(context, R.drawable.leg2_shadow)
            tailShadow = AppCompatResources.getDrawable(context, R.drawable.tail_shadow)
            tailCap = AppCompatResources.getDrawable(context, R.drawable.tail_cap)
            belly = AppCompatResources.getDrawable(context, R.drawable.belly)
            back = AppCompatResources.getDrawable(context, R.drawable.back)
            rightEye = AppCompatResources.getDrawable(context, R.drawable.right_eye)
            leftEye = AppCompatResources.getDrawable(context, R.drawable.left_eye)
            nose = AppCompatResources.getDrawable(context, R.drawable.nose)
            collar = AppCompatResources.getDrawable(context, R.drawable.collar)
            bowtie = AppCompatResources.getDrawable(context, R.drawable.bowtie)
            hat = CatSkins.getCatHat(context, seed)
            suit = CatSkins.getCatSuit(context, seed)
            dirt = CatSkins.getCatDirty(context, seed)
            drawingOrdr = getDrawingOrder()
        }

        private fun getDrawingOrder(): Array<Drawable?> {
            return arrayOf(
                    collar,
                    leftEar, leftEarInside, rightEar, rightEarInside,
                    head,
                    faceSpot,
                    cap,
                    hat,
                    leftEye, rightEye,
                    nose, mouth,
                    tail, tailCap, tailShadow,
                    foot1, leg1,
                    foot2, leg2,
                    foot3, leg3,
                    foot4, leg4,
                    leg2Shadow,
                    body, belly, bowtie, suit, dirt
            )
        }
    }

    companion object {
        @JvmField
        val PURR = longArrayOf(0, 40, 20, 40, 20, 40, 20, 40, 20, 40, 20, 40)
        const val ALL_CATS_IN_ONE_CONVERSATION = true
        const val GLOBAL_SHORTCUT_ID = "ru.dimon6018.neko11:allcats"
        const val SHORTCUT_ID_PREFIX = "ru.dimon6018.neko11:cat:"
        @JvmField
        val RANDOM = Random()
        fun frandrange(r: Random, a: Float, b: Float): Float {
            return (b - a) * r.nextFloat() + a
        }

        fun choose(r: Random, vararg l: Any): Any {
            return l[r.nextInt(l.size)]
        }

        @JvmOverloads
        fun chooseP(r: Random, a: IntArray, sum: Int = 1001): Int {
            var pct = r.nextInt(sum)
            val stop = a.size - 2
            var i = 0
            while (i < stop) {
                pct -= a[i]
                if (pct < 0) break
                i += 2
            }
            return a[i + 1]
        }

        val P_BODY_COLORS = intArrayOf(
                180, -0xdededf,  // black
                180, -0x1,  // white
                140, -0x9e9e9f,  // gray
                140, -0x86aab8,  // brown
                100, -0x6f5b52,  // steel
                100, -0x63c,  // buff
                100, -0x7100,  // orange
                5, -0xd6490a,  // blue..?
                5, -0x322e,  // pink!?
                5, -0x316c28,  // purple?!?!?
                4, -0xbc5fb9,  // yeah, why not green
                1, 0)
        val P_COLLAR_COLORS = intArrayOf(
                250, -0x1,
                250, -0x1000000,
                250, -0xbbcca,
                50, -0xe6892e,
                50, -0x227cb,
                50, -0x47400,
                50, -0xb704f,
                50, -0xb350b0)
        val P_BELLY_COLORS = intArrayOf(
                750, 0,
                250, -0x1)
        val P_DARK_SPOT_COLORS = intArrayOf(
                700, 0,
                250, -0xdededf,
                50, -0x92b3bf)
        val P_LIGHT_SPOT_COLORS = intArrayOf(
                700, 0,
                300, -0x1)

        fun tint(color: Int, vararg ds: Drawable?) {
            for (d in ds) {
                d?.mutate()?.setTint(color)
            }
        }

        fun isDark(color: Int): Boolean {
            val r = color and 0xFF0000 shr 16
            val g = color and 0x00FF00 shr 8
            val b = color and 0x0000FF
            return r + g + b < 0x80
        }

        @JvmStatic
        fun create(context: Context): Cat {
            val seed = abs(RANDOM.nextLong().toDouble()).toLong()
            return Cat(context, seed, context.getString(
                    R.string.default_cat_name, (seed % 1000).toString()))
        }

        @RequiresApi(VERSION_CODES.P)
        fun recompressIconP(bitmapIcon: Icon): Icon? {
            return if (bitmapIcon.type != Icon.TYPE_BITMAP) bitmapIcon else try {
                @SuppressLint("DiscouragedPrivateApi") val bits = Icon::class.java.getDeclaredMethod("getBitmap").invoke(bitmapIcon) as Bitmap
                val ostream = ByteArrayOutputStream(
                        bits.getWidth() * bits.getHeight() * 2) // guess 50% compression
                val ok = bits.compress(Bitmap.CompressFormat.PNG, 95, ostream)
                if (!ok) null else Icon.createWithData(ostream.toByteArray(), 0, ostream.size())
            } catch (ex: NoSuchMethodException) {
                bitmapIcon
            } catch (ex: IllegalAccessException) {
                bitmapIcon
            } catch (ex: InvocationTargetException) {
                bitmapIcon
            }
        }

        @RequiresApi(VERSION_CODES.M)
        fun recompressIconO(bitmap: Bitmap): Icon? {
            val ostream = ByteArrayOutputStream(
                    bitmap.getWidth() * bitmap.getHeight() * 2) // guess 50% compression
            val ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream)
            return if (ok) Icon.createWithData(ostream.toByteArray(), 0, ostream.size()) else null
        }
    }
}
