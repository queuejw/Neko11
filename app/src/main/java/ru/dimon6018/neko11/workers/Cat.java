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

import static android.os.Build.VERSION_CODES.P;
import static ru.dimon6018.neko11.ui.fragments.NekoLandFragment.CHAN_ID;
import static ru.dimon6018.neko11.workers.NekoWorker.title_message;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Person;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import ru.dimon6018.neko11.NekoGeneralActivity;
import ru.dimon6018.neko11.R;

/** It's a cat. */
public class Cat extends Drawable {
    public static final long[] PURR = {0, 40, 20, 40, 20, 40, 20, 40, 20, 40, 20, 40};

    public static final boolean ALL_CATS_IN_ONE_CONVERSATION = true;

    public static final String GLOBAL_SHORTCUT_ID = "ru.dimon6018.neko11:allcats";
    public static final String SHORTCUT_ID_PREFIX = "ru.dimon6018.neko11:cat:";

    private Random mNotSoRandom;
    private Bitmap mBitmap;
    private final long mSeed;
    private String mName;
    private int mBodyColor;
    private int mFootType;
    private int mAge;
    private String mStatus;
    private String mFirstMessage;
    private boolean mBowTie;

    static final Random RANDOM = new Random();

    private synchronized Random notSoRandom(long seed) {
        if (mNotSoRandom == null) {
            mNotSoRandom = new Random();
            mNotSoRandom.setSeed(seed);
        }
        return mNotSoRandom;
    }

    public static float frandrange(Random r, float a, float b) {
        return (b - a) * r.nextFloat() + a;
    }

    public static Object choose(Random r, Object... l) {
        return l[r.nextInt(l.length)];
    }

    public static int chooseP(Random r, int[] a) {
        return chooseP(r, a, 1001);
    }

    public static int chooseP(Random r, int[] a, int sum) {
        int pct = r.nextInt(sum);
        final int stop = a.length - 2;
        int i = 0;
        while (i < stop) {
            pct -= a[i];
            if (pct < 0) break;
            i += 2;
        }
        return a[i + 1];
    }

    public static final int[] P_BODY_COLORS = {
            180, 0xFF212121, // black
            180, 0xFFFFFFFF, // white
            140, 0xFF616161, // gray
            140, 0xFF795548, // brown
            100, 0xFF90A4AE, // steel
            100, 0xFFFFF9C4, // buff
            100, 0xFFFF8F00, // orange
            5, 0xFF29B6F6, // blue..?
            5, 0xFFFFCDD2, // pink!?
            5, 0xFFCE93D8, // purple?!?!?
            4, 0xFF43A047, // yeah, why not green
            1, 0,          // ?!?!?!
    };

    public static final int[] P_COLLAR_COLORS = {
            250, 0xFFFFFFFF,
            250, 0xFF000000,
            250, 0xFFF44336,
            50, 0xFF1976D2,
            50, 0xFFFDD835,
            50, 0xFFFB8C00,
            50, 0xFFF48FB1,
            50, 0xFF4CAF50,
    };

    public static final int[] P_BELLY_COLORS = {
            750, 0,
            250, 0xFFFFFFFF,
    };

    public static final int[] P_DARK_SPOT_COLORS = {
            700, 0,
            250, 0xFF212121,
            50, 0xFF6D4C41,
    };

    public static final int[] P_LIGHT_SPOT_COLORS = {
            700, 0,
            300, 0xFFFFFFFF,
    };

    private final CatParts D;

    public static void tint(int color, Drawable... ds) {
        for (Drawable d : ds) {
            if (d != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    d.mutate().setTint(color);
                }
            }
        }
    }

    public static boolean isDark(int color) {
        final int r = (color & 0xFF0000) >> 16;
        final int g = (color & 0x00FF00) >> 8;
        final int b = color & 0x0000FF;
        return (r + g + b) < 0x80;
    }

    public Cat(Context context, long seed, String name) {
	    D = new CatParts(context);
        mSeed = seed;	
        setName(name);
        final Random nsr = notSoRandom(seed);
        // age
        mAge = nsr.nextInt((18 - 1) + 1);
        // set funny status
        String[] statusArray = context.getResources().getStringArray(R.array.cat_status);
        mStatus = statusArray[nsr.nextInt(context.getResources().getStringArray(R.array.cat_status).length)];

        // body color
        mBodyColor = chooseP(nsr, P_BODY_COLORS);
        if (mBodyColor == 0) mBodyColor = Color.HSVToColor(new float[]{
                nsr.nextFloat() * 360f, frandrange(nsr, 0.5f, 1f), frandrange(nsr, 0.5f, 1f)});

        tint(mBodyColor, D.body, D.head, D.leg1, D.leg2, D.leg3, D.leg4, D.tail,
                D.leftEar, D.rightEar, D.foot1, D.foot2, D.foot3, D.foot4, D.tailCap);
        tint(0x20000000, D.leg2Shadow, D.tailShadow);
        if (isDark(mBodyColor)) {
            tint(0xFFFFFFFF, D.leftEye, D.rightEye, D.mouth, D.nose);
        }
        tint(isDark(mBodyColor) ? 0xFFEF9A9A : 0x20D50000, D.leftEarInside, D.rightEarInside);

        tint(chooseP(nsr, P_BELLY_COLORS), D.belly);
        tint(chooseP(nsr, P_BELLY_COLORS), D.back);
        final int faceColor = chooseP(nsr, P_BELLY_COLORS);
        tint(faceColor, D.faceSpot);
        if (!isDark(faceColor)) {
            tint(0xFF000000, D.mouth, D.nose);
        }

        mFootType = 0;
        if (nsr.nextFloat() < 0.25f) {
            mFootType = 4;
            tint(0xFFFFFFFF, D.foot1, D.foot2, D.foot3, D.foot4);
        } else {
            if (nsr.nextFloat() < 0.25f) {
                mFootType = 2;
                tint(0xFFFFFFFF, D.foot1, D.foot3);
            } else if (nsr.nextFloat() < 0.25f) {
                mFootType = 3; // maybe -2 would be better? meh.
                tint(0xFFFFFFFF, D.foot2, D.foot4);
            } else if (nsr.nextFloat() < 0.1f) {
                mFootType = 1;
                tint(0xFFFFFFFF, (Drawable) choose(nsr, D.foot1, D.foot2, D.foot3, D.foot4));
            }
        }

        tint(nsr.nextFloat() < 0.333f ? 0xFFFFFFFF : mBodyColor, D.tailCap);

        final int capColor = chooseP(nsr, isDark(mBodyColor) ? P_LIGHT_SPOT_COLORS : P_DARK_SPOT_COLORS);
        tint(capColor, D.cap);
        final int collarColor = chooseP(nsr, P_COLLAR_COLORS);
        tint(collarColor, D.collar);
        mBowTie = nsr.nextFloat() < 0.1f;
        tint(mBowTie ? collarColor : 0, D.bowtie);

        String[] messages = context.getResources().getStringArray(
                nsr.nextFloat() < 0.1f ? R.array.rare_cat_messages : R.array.cat_messages);
        mFirstMessage = (String) choose(nsr, (Object[]) messages);
        if (nsr.nextFloat() < 0.5f) mFirstMessage = mFirstMessage + mFirstMessage + mFirstMessage;
	}
    public static Cat create(Context context) {
        final long seed = Math.abs(RANDOM.nextLong());
        return new Cat(context, seed, context.getString(
                R.string.default_cat_name, String.valueOf(seed % 1000)));
    }
    @RequiresApi(P)
    public Notification.Builder buildNotificationP(Context context) {
        final Bundle extras = new Bundle();
        extras.putString("android.substName", context.getString(R.string.notification_name));

        final Icon notificationIcon = createNotificationLargeIcon(context);

        final Intent intent = new Intent(Intent.ACTION_MAIN)
                .setClass(context, NekoGeneralActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ShortcutInfo shortcut = new ShortcutInfo.Builder(context, getShortcutId())
                .setActivity(intent.getComponent())
                .setIntent(intent)
                .setShortLabel(getName())
                .setIcon(createShortcutIcon(context))
                .build();
        context.getSystemService(ShortcutManager.class).addDynamicShortcuts(List.of(shortcut));

        return new Notification.Builder(context, CHAN_ID)
                .setSmallIcon(Icon.createWithResource(context, R.drawable.stat_icon))
                .setLargeIcon(notificationIcon)
                .setColor(getBodyColor())
                .setContentTitle(title_message)
                .setShowWhen(true)
                .setCategory(Notification.CATEGORY_STATUS)
                .setContentText(getName())
                .setContentIntent(
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(true)
                .setStyle(new Notification.MessagingStyle(createPerson(context))
                        .addMessage(title_message +  mFirstMessage, System.currentTimeMillis(), createPerson(context))
                        .setConversationTitle(getName())
                )
                .setShortcutId(getShortcutId())
                .addExtras(extras);
    }
    @RequiresApi(P)
    private Person createPerson(Context context) {
        final Icon notificationIcon = createNotificationLargeIcon(context);
        return new Person.Builder()
                .setName(getName())
                .setIcon(notificationIcon)
                .setBot(true)
                .setKey(getShortcutId())
                .build();
    }
    public Notification.Builder buildNotificationO(Context context) {
        final Bundle extras = new Bundle();
        extras.putString("android.substName", context.getString(R.string.notification_name));

        final Icon notificationIcon = createNotificationLargeIcon(context);

        final Intent intent = new Intent(Intent.ACTION_MAIN)
                .setClass(context, NekoGeneralActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ShortcutInfo shortcut = new ShortcutInfo.Builder(context, getShortcutId())
                .setActivity(intent.getComponent())
                .setIntent(intent)
                .setShortLabel(getName())
                .setIcon(createShortcutIcon(context))
                .build();
        context.getSystemService(ShortcutManager.class).addDynamicShortcuts(List.of(shortcut));

        return new Notification.Builder(context, CHAN_ID)
                .setSmallIcon(Icon.createWithResource(context, R.drawable.stat_icon))
                .setLargeIcon(notificationIcon)
                .setColor(getBodyColor())
                .setContentTitle(title_message +  mFirstMessage)
                .setShowWhen(true)
                .setCategory(Notification.CATEGORY_STATUS)
                .setContentText(getName())
                .setContentIntent(
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(true)
                .setShortcutId(getShortcutId())
                .addExtras(extras);
    }
    public long getSeed() {
        return mSeed;
    }

    @Override
    public void draw(Canvas canvas) {
        final int w = Math.min(getBounds().width(), getBounds().height());

        if (mBitmap == null || mBitmap.getWidth() != w || mBitmap.getHeight() != w) {
            mBitmap = Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888);
            final Canvas bitCanvas = new Canvas(mBitmap);
            slowDraw(bitCanvas, 0, 0, w, w);
        }
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    private void slowDraw(Canvas canvas, int x, int y, int w, int h) {
        for (int i = 0; i < D.drawingOrder.length; i++) {
            final Drawable d = D.drawingOrder[i];
            if (d != null) {
                d.setBounds(x, y, x + w, y + h);
                d.draw(canvas);
            }
        }

    }

    public Bitmap createBitmap(int w, int h) {
        if (mBitmap != null && mBitmap.getWidth() == w && mBitmap.getHeight() == h) {
            return mBitmap.copy(mBitmap.getConfig(), true);
        }
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        slowDraw(new Canvas(result), 0, 0, w, h);
        return result;
    }
    private Bitmap createIconBitmap(int w, int h) {
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(result);
        final Paint pt = new Paint();
        float[] hsv = new float[3];
        Color.colorToHSV(mBodyColor, hsv);
        hsv[2] = (hsv[2]>0.5f)
                ? (hsv[2] - 0.25f)
                : (hsv[2] + 0.25f);
        pt.setColor(Color.HSVToColor(hsv));
        float r = w/2;
        canvas.drawCircle(r, r, r, pt);
        int m = w/10;
        slowDraw(canvas, m, m, w-m-m, h-m-m);
        return result;
    }
    @RequiresApi(P)
    public static Icon recompressIconP(Icon bitmapIcon) {
        if (bitmapIcon.getType() != Icon.TYPE_BITMAP) return bitmapIcon;
        try {
            @SuppressLint("DiscouragedPrivateApi") final Bitmap bits = (Bitmap) Icon.class.getDeclaredMethod("getBitmap").invoke(bitmapIcon);
            final ByteArrayOutputStream ostream = new ByteArrayOutputStream(
                    bits.getWidth() * bits.getHeight() * 2); // guess 50% compression
            final boolean ok = bits.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            if (!ok) return null;
            return Icon.createWithData(ostream.toByteArray(), 0, ostream.size());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            return bitmapIcon;
        }
    }
    public static Icon recompressIconO(Bitmap bitmap) {
        final ByteArrayOutputStream ostream = new ByteArrayOutputStream(
                bitmap.getWidth() * bitmap.getHeight() * 2); // guess 50% compression
        final boolean ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
        return ok ? Icon.createWithData(ostream.toByteArray(), 0, ostream.size()) : null;
    }

    public Icon createNotificationLargeIcon(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return recompressIconP(createIcon(context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width), context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height)));
        } else {
            return recompressIconO(createIconBitmap(context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width), context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height)));
        }
    }
    public Icon createShortcutIcon(Context context) {
        return createIcon(context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width), context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height));
    }

    public Icon createIcon(int w, int h) {
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(result);
        final Paint pt = new Paint();
        float[] hsv = new float[3];
        Color.colorToHSV(mBodyColor, hsv);
        hsv[2] = (hsv[2]>0.5f)
                ? (hsv[2] - 0.25f)
                : (hsv[2] + 0.25f);
        pt.setColor(Color.HSVToColor(hsv));
        float r = w/2;
        canvas.drawCircle(r, r, r, pt);
        int m = w/10;
        slowDraw(canvas, m, m, w-m-m, h-m-m);
        return Icon.createWithBitmap(result);
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public int getBodyColor() {
        return mBodyColor;
    }
    public int getAge() {
        return mAge;
    }
    public String getStatus() {
        return mStatus;
    }
	public boolean getBowTie() {
        return mBowTie;
	}
    public String getShortcutId() {
        return ALL_CATS_IN_ONE_CONVERSATION
                ? GLOBAL_SHORTCUT_ID
                : (SHORTCUT_ID_PREFIX + mSeed);
    }

    public static class CatParts {
        public Drawable leftEar;
        public Drawable rightEar;
        public Drawable rightEarInside;
        public Drawable leftEarInside;
        public Drawable head;
        public Drawable faceSpot;
        public Drawable cap;
        public Drawable mouth;
        public Drawable body;
        public Drawable foot1;
        public Drawable leg1;
        public Drawable foot2;
        public Drawable leg2;
        public Drawable foot3;
        public Drawable leg3;
        public Drawable foot4;
        public Drawable leg4;
        public Drawable tail;
        public Drawable leg2Shadow;
        public Drawable tailShadow;
        public Drawable tailCap;
        public Drawable belly;
        public Drawable back;
        public Drawable rightEye;
        public Drawable leftEye;
        public Drawable nose;
        public Drawable bowtie;
        public Drawable collar;
        public Drawable[] drawingOrder;

        public CatParts(Context context) {
            body = AppCompatResources.getDrawable(context, R.drawable.body);
            head = AppCompatResources.getDrawable(context, R.drawable.head);
            leg1 = AppCompatResources.getDrawable(context, R.drawable.leg1);
            leg2 = AppCompatResources.getDrawable(context, R.drawable.leg2);
            leg3 = AppCompatResources.getDrawable(context, R.drawable.leg3);
            leg4 = AppCompatResources.getDrawable(context, R.drawable.leg4);
            tail = AppCompatResources.getDrawable(context, R.drawable.tail);
            leftEar = AppCompatResources.getDrawable(context,R.drawable.left_ear);
            rightEar = AppCompatResources.getDrawable(context, R.drawable.right_ear);
            rightEarInside = AppCompatResources.getDrawable(context, R.drawable.right_ear_inside);
            leftEarInside = AppCompatResources.getDrawable(context, R.drawable.left_ear_inside);
            faceSpot = AppCompatResources.getDrawable(context, R.drawable.face_spot);
            cap = AppCompatResources.getDrawable(context, R.drawable.cap);
            mouth = AppCompatResources.getDrawable(context, R.drawable.mouth);
            foot4 = AppCompatResources.getDrawable(context, R.drawable.foot4);
            foot3 = AppCompatResources.getDrawable(context, R.drawable.foot3);
            foot1 = AppCompatResources.getDrawable(context, R.drawable.foot1);
            foot2 = AppCompatResources.getDrawable(context, R.drawable.foot2);
            leg2Shadow = AppCompatResources.getDrawable(context, R.drawable.leg2_shadow);
            tailShadow = AppCompatResources.getDrawable(context, R.drawable.tail_shadow);
            tailCap = AppCompatResources.getDrawable(context, R.drawable.tail_cap);
            belly = AppCompatResources.getDrawable(context, R.drawable.belly);
            back = AppCompatResources.getDrawable(context, R.drawable.back);
            rightEye = AppCompatResources.getDrawable(context, R.drawable.right_eye);
            leftEye = AppCompatResources.getDrawable(context, R.drawable.left_eye);
            nose = AppCompatResources.getDrawable(context, R.drawable.nose);
            collar = AppCompatResources.getDrawable(context, R.drawable.collar);
            bowtie = AppCompatResources.getDrawable(context, R.drawable.bowtie);
            drawingOrder = getDrawingOrder();
        }

        private Drawable[] getDrawingOrder() {
            return new Drawable[]{
                    collar,
                    leftEar, leftEarInside, rightEar, rightEarInside,
                    head,
                    faceSpot,
                    cap,
                    leftEye, rightEye,
                    nose, mouth,
                    tail, tailCap, tailShadow,
                    foot1, leg1,
                    foot2, leg2,
                    foot3, leg3,
                    foot4, leg4,
                    leg2Shadow,
                    body, belly,
                    bowtie
            };
        }
    }
}
