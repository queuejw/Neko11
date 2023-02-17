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

package ru.dimon6018.neko11.ui.fragments;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;
import ru.dimon6018.neko11.NekoGeneralActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import dev.chrisbanes.insetter.Insetter;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

import static ru.dimon6018.neko11.workers.NekoWorker.title_message;
import ru.dimon6018.neko11.workers.Cat;
import ru.dimon6018.neko11.workers.NekoToyWorker;
import ru.dimon6018.neko11.workers.NekoWorker;
import ru.dimon6018.neko11.workers.PrefState;
import ru.dimon6018.neko11.R;

public class NekoLand extends Fragment implements PrefState.PrefsListener {
    public static String CHAN_ID = "NEKO";

    public static boolean DEBUG_NOTIFICATIONS = false;

    private static final int EXPORT_BITMAP_SIZE = 600;

    private static final int STORAGE_PERM_REQUEST = 123;

    private static final boolean CAT_GEN = false;
	
	private boolean paused = true;
	
    private PrefState mPrefs;
    private CatAdapter mAdapter;
    private Cat mPendingShareCat;
	private int numCats;
	
	private MaterialTextView counter;
	public SharedPreferences nekoprefs;
	

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.neko_activity_content, container, false);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mPrefs = new PrefState(getContext());
        mPrefs.setListener(this);
		
        RecyclerView recyclerView = view.findViewById(R.id.holder);
        mAdapter = new CatAdapter();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        nekoprefs = requireActivity().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        boolean CATS_HERE_TIP_HIDE = nekoprefs.getBoolean("cats_here_tip_enabled", true);
		boolean GIFT_ENABLED = nekoprefs.getBoolean("gift", true);
        MaterialTextView cats_here_tip = view.findViewById(R.id.cats_here_tip);
        if(!CATS_HERE_TIP_HIDE) {
            cats_here_tip.setVisibility(View.INVISIBLE);
        }
        cats_here_tip.setOnClickListener(v -> {
            cats_here_tip.setVisibility(View.INVISIBLE);
            SharedPreferences.Editor editor = nekoprefs.edit();
            editor.putBoolean("cats_here_tip_enabled", false);
            editor.apply();
        });
		if(GIFT_ENABLED) {
            WelcomeDialog();
        }
		counter = view.findViewById(R.id.counter);
		numCats = updateCats();
        String count = getResources().getString(R.string.cat_counter, numCats);
        counter.setText(count);
        if(numCats <= 0 ) {
            counter.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
	private void WelcomeDialog() {
		 final PrefState prefs = new PrefState(getContext());
		 final Cat cat;
		 new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.app_name_neko)
                    .setIcon(R.drawable.ic_brush)
                    .setMessage(R.string.welcome_dialog)
                    .setCancelable(false)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(R.string.get, (dialog, id) ->  getGift()
					).show();
			SharedPreferences.Editor editor = nekoprefs.edit();
            editor.putBoolean("gift", false);
            editor.apply();
	}
	private void getGift() {
		Context context = getContext();
		title_message = context.getString(R.string.notification_title);
		final PrefState prefs = new PrefState(context);
		Cat cat;
		cat = NekoWorker.newRandomCat(context, prefs);
		NekoWorker.notifyCat(context, cat, title_message);
		getParentFragmentManager().beginTransaction().replace(R.id.container, new NekoLand()).commit();
	}
    private int updateCats() {
        Cat[] cats;
        if (CAT_GEN) {
            cats = new Cat[15];
            for (int i = 0; i < cats.length; i++) {
                cats[i] = Cat.create(getContext());
            }
        } else {
            final float[] hsv = new float[3];
            List<Cat> list = mPrefs.getCats();
            list.sort((cat, cat2) -> {
                Color.colorToHSV(cat.getBodyColor(), hsv);
                float bodyH1 = hsv[0];
                Color.colorToHSV(cat2.getBodyColor(), hsv);
                float bodyH2 = hsv[0];
                return Float.compare(bodyH1, bodyH2);
            });
            cats = list.toArray(new Cat[0]);
        }
        mAdapter.setCats(cats);
        return cats.length;
    }
	public void updateScreen() {
	getParentFragmentManager().beginTransaction().replace(R.id.container, new NekoLand()).commit();
	}
    private void onCatClick(Cat cat) {
	Random random = new Random(cat.getSeed());	
	final int max = requireActivity().getResources().getDimensionPixelSize(R.dimen.bottomsheet);
	
	 String[] statusArray = getResources().getStringArray(R.array.cat_status);
     String catstatus = statusArray[random.nextInt(statusArray.length)];	 
   	 String statusresult = getResources().getString(R.string.cat_status_string, catstatus);
	 
 
     BottomSheetDialog bottomsheet = new BottomSheetDialog(getContext());   
     bottomsheet.setContentView(R.layout.neko_bottomsheet);
	 bottomsheet.setDismissWithAnimation(true);
	 View bottomSheetInternal = bottomsheet.findViewById(R.id.design_bottom_sheet); 
     BottomSheetBehavior.from(bottomSheetInternal).setPeekHeight(max, true);
	 
	   Insetter.builder()
	   .padding(WindowInsetsCompat.Type.navigationBars())
	   .applyToView(bottomSheetInternal);
	   
	 ImageView catico = bottomSheetInternal.findViewById(R.id.cat_icon);
	 ImageView rename = bottomSheetInternal.findViewById(R.id.rename_sheet);
	 ImageView save = bottomSheetInternal.findViewById(R.id.save_sheet);
	 ImageView del = bottomSheetInternal.findViewById(R.id.delete_sheet);
	 MaterialTextView status = bottomSheetInternal.findViewById(R.id.status_title);
	 MaterialTextView name = bottomSheetInternal.findViewById(R.id.catname);
	 
	 status.setText(statusresult);
	 name.setText(cat.getName());
	 catico.setImageIcon(cat.createNotificationLargeIcon(getContext()));
	 
	 del.setOnClickListener(v -> {
	    onCatRemove(cat);
		bottomsheet.dismiss();
	 });
	 rename.setOnClickListener(v -> {
		 showNameDialog(cat);
		 bottomsheet.dismiss();
		});
	 save.setOnClickListener(v -> {
	  bottomsheet.dismiss(); 
	  if (ActivityCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    mPendingShareCat = cat;
                    requestPermissions(
                            new String[]{WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERM_REQUEST);
                    return;
                }
                shareCat(cat);				
	 });	 
	 bottomsheet.show();
    }

    private void onCatRemove(Cat cat) {
		 mPrefs.removeCat(cat);
		 getParentFragmentManager().beginTransaction().replace(R.id.container, new NekoLand()).commit();
    }
    private void showNameDialog(final Cat cat) {
        final Context context = new ContextThemeWrapper(getContext(),
                requireActivity().getTheme());
        View view = LayoutInflater.from(context).inflate(R.layout.edit_text, null);
        final EditText text = view.findViewById(android.R.id.edit);
        text.setText(cat.getName());
        text.setSelection(cat.getName().length());
        final int size = context.getResources()
                .getDimensionPixelSize(android.R.dimen.app_icon_size);
        Drawable catIcon = cat.createIcon(size, size).loadDrawable(getActivity());
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.rename_cat_title)
                .setIcon(catIcon)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    cat.setName(text.getText().toString().trim());
                    mPrefs.addCat(cat);
                }).show();
    }

    @Override
    public void onPrefsChanged() {
        updateCats();
    }

    private class CatAdapter extends RecyclerView.Adapter<CatHolder> {

        private Cat[] mCats;

        public void setCats(Cat[] cats) {
            mCats = cats;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CatHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cat_view, parent, false));
        }
        @SuppressWarnings({"deprecation", "ObsoleteSdkInt"})
        @SuppressLint("RecyclerView")
        @Override
        public void onBindViewHolder(final CatHolder holder, int position) {
            final int size = requireActivity().getResources().getDimensionPixelSize(R.dimen.neko_display_size);
            holder.imageView.setImageIcon(mCats[position].createIcon(size, size));
            holder.textView.setText(mCats[position].getName());
            holder.itemView.setOnClickListener(v -> onCatClick(mCats[holder.getAdapterPosition()]));
        }
        @Override
        public int getItemCount() {
            return mCats.length;
        }
    }

    private void shareCat(Cat cat) {
        final File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Cats");
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e("NekoLand", "save: error: can't create Pictures directory");
            return;
        }
        final File png = new File(dir, cat.getName().replaceAll("[/ #:]+", "_") + ".png");
        Bitmap bitmap = cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE);
        if (bitmap != null) {
            try {
                OutputStream os = new FileOutputStream(png);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
                os.close();
                MediaScannerConnection.scanFile(
                        getActivity(),
                        new String[]{png.toString()},
                        new String[]{"image/png"},
                        null);
                Log.v("Neko", "cat file: " + png);
                Uri uri = FileProvider.getUriForFile(requireActivity(), "ru.dimon6018.neko11.fileprovider", png);
                Log.v("Neko", "cat uri: " + uri);

                String name = cat.getName();
                String message = getResources().getString(R.string.picture_saved_successful, name);

                new MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.app_name_neko)
                        .setIcon(R.drawable.ic_success)
                        .setMessage(message)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.ok, null)
                        .show();
            } catch (IOException e) {
                Log.e("NekoLand", "save: error: " + e);
                new MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.app_name_neko)
                        .setIcon(R.drawable.ic_warning)
                        .setMessage(R.string.permission_denied)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.ok, null)
                        .show();
            }
        }
    }
    @SuppressWarnings("deprecation")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERM_REQUEST) {
            if (mPendingShareCat != null) {
                shareCat(mPendingShareCat);
                mPendingShareCat = null;
            }
        }
    }
    private static class CatHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textView;
        private final View contextGroup;
        private final View delete;
        private final View save;

        public CatHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(android.R.id.icon);
            textView = itemView.findViewById(android.R.id.title);
            contextGroup = itemView.findViewById(R.id.contextGroup);
            delete = itemView.findViewById(R.id.deleteText);
            save = itemView.findViewById(R.id.saveText);
        }
    }
	@Override
	public void onResume() {		
	super.onResume();
}
	@Override
	public void onPause() {
	super.onPause();
	}
}