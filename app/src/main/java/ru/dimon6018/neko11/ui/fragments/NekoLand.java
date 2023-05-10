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
import static ru.dimon6018.neko11.workers.NekoWorker.title_message;
import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.NekoGeneralActivity;
import ru.dimon6018.neko11.workers.Cat;
import ru.dimon6018.neko11.workers.NekoToyWorker;
import ru.dimon6018.neko11.workers.NekoWorker;
import ru.dimon6018.neko11.workers.PrefState;

public class NekoLand extends Fragment implements PrefState.PrefsListener {
    public static String CHAN_ID = "NEKO";

    private static final int STORAGE_PERM_REQUEST = 123;
	
	public static final int EXPORT_BITMAP_SIZE = 600;
	
    private PrefState mPrefs;
    private CatAdapter mAdapter;
    private Cat mPendingShareCat;
	private int numCats;
	
	public boolean isNekoLandOpened;
	
	private MaterialTextView counter;
	public SharedPreferences nekoprefs;
	private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
		mPrefs = new PrefState(getContext());
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {	
		nekoprefs = getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return inflater.inflate(R.layout.neko_activity_content, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mPrefs.setListener(this);
		counter = view.findViewById(R.id.counter);	
		recyclerView = view.findViewById(R.id.holder);
        recyclerView.setAdapter(mAdapter = new CatAdapter());	
		updateLM();
	    numCats = updateCats();	
    }
    @Override
    public void onDestroy() {
		mPrefs.setListener(null);
        super.onDestroy();
    }
    public int updateCats() {
	Cat[] cats;
	List<Cat> list = mPrefs.getCats();	
	//See https://github.com/chris-blay/AndroidNougatEasterEgg/blob/master/workspace/src/com/covertbagel/neko/NekoLand.java	
	switch(mPrefs.getSortState()) {
		//by color	
            case 1:
			final float[] hsv = new float[3];
            list.sort((cat, cat2) -> {
                Color.colorToHSV(cat.getBodyColor(), hsv);
                float bodyH1 = hsv[0];
                Color.colorToHSV(cat2.getBodyColor(), hsv);
                float bodyH2 = hsv[0];
                return Float.compare(bodyH1, bodyH2);
            });
			break;
		//by name	
			case 2:
			 list.sort((cat, cat2) -> 
			 cat.getName().compareTo(cat2.getName()));
			break;	
		//off	
			case 0:
			break;
        }	
	cats = list.toArray(new Cat[0]);
	int catsNum = cats.length;
	if(isNekoLandOpened) {
	updateCounter(catsNum);
	mAdapter.setCats(cats);	
	}
	return catsNum;
}
	private void updateLM() {	
	recyclerView.setLayoutManager(new GridLayoutManager(getContext(), mPrefs.getCatsInLineLimit()));
	}
  private void updateCounter(int cats) {	
    counter.setText(getString(R.string.cat_counter, cats));
	SharedPreferences.Editor editor = nekoprefs.edit();
	editor.putInt("num", cats);
    editor.apply();
}
    private void onCatClick(Cat cat) {
	 Random random = new Random(cat.getSeed());	
	 Context context = getContext();
	 String[] statusArray = getResources().getStringArray(R.array.cat_status); 
     BottomSheetDialog bottomsheet = new BottomSheetDialog(getContext());   
     bottomsheet.setContentView(R.layout.neko_bottomsheet);
	 bottomsheet.setDismissWithAnimation(true);
	 View bottomSheetInternal = bottomsheet.findViewById(R.id.design_bottom_sheet); 
     BottomSheetBehavior.from(bottomSheetInternal).setPeekHeight(context.getResources().getDimensionPixelSize(R.dimen.bottomsheet));
	 
	 ImageView save = bottomSheetInternal.findViewById(R.id.save_sheet);
	 ImageView del = bottomSheetInternal.findViewById(R.id.delete_sheet);
	 ImageView zoom = bottomSheetInternal.findViewById(R.id.zoom);
	 TextInputLayout textLayout = bottomSheetInternal.findViewById(R.id.catNameField);
	 EditText catEditor = bottomSheetInternal.findViewById(R.id.catEditName);
	 ImageView catico = bottomSheetInternal.findViewById(R.id.cat_icon);
	 catico.setImageIcon(cat.createNotificationLargeIcon(getContext()));
     MaterialTextView status = bottomSheetInternal.findViewById(R.id.status_title);
	 MaterialTextView age = bottomSheetInternal.findViewById(R.id.cat_age);
	 
	 age.setText(getString(R.string.cat_age, random.nextInt(18 - 1) + 1)); 
     status.setText(getString(R.string.cat_status_string, statusArray[random.nextInt(statusArray.length)]));
	 catEditor.setText(cat.getName());
	 
	del.setOnClickListener(v -> {
		bottomsheet.dismiss(); 
	    showCatRemoveDialog(cat, context);
	});
	textLayout.setEndIconOnClickListener(v -> {	
	 Snackbar.make(bottomSheetInternal, R.string.name_changed, Snackbar.LENGTH_LONG).show();	
     bottomsheet.dismiss();		 
	 cat.setName(catEditor.getText().toString().trim());
     mPrefs.addCat(cat);	 
	});
	zoom.setOnClickListener(v -> {
		 bottomsheet.dismiss();
	     showCatFull(cat);	
	});	
	 save.setOnClickListener(v -> {
	  bottomsheet.dismiss(); 
	  if (ActivityCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)
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
	
	private void showCatRemoveDialog(Cat cat, Context context) {
     final int size = context.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
	 new MaterialAlertDialogBuilder(context)
                .setIcon(cat.createIcon(size, size).loadDrawable(context))
                .setTitle(R.string.delete_cat_title)
				.setMessage(R.string.delete_cat_message)
                .setPositiveButton(R.string.delete_cat, (dialog, id) -> onCatRemove(cat))
				.setNegativeButton(android.R.string.cancel, null)
                .show();	
	}	
    private void onCatRemove(Cat cat) {
		 mPrefs.removeCat(cat);
    }
	private void showCatFull(Cat cat) {
	final Context context = new ContextThemeWrapper(getContext(),
                requireActivity().getTheme());
        View view = LayoutInflater.from(context).inflate(R.layout.cat_fullscreen_view, null);
        final ImageView ico = view.findViewById(R.id.cat_ico);
		ico.setImageBitmap(cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE));
        new MaterialAlertDialogBuilder(context)
                .setIcon(R.drawable.ic_zoom)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();	
	}
    @Override
    public void onPrefsChanged() {
		updateLM();
		if(isNekoLandOpened) {
		updateCats();	
		}
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
        @Override
        public void onBindViewHolder(final CatHolder holder, int position) {
            holder.imageView.setImageIcon(mCats[position].createIcon(mPrefs.getCatIconSize(), mPrefs.getCatIconSize()));
			holder.textView.setText(mCats[position].getName());
            holder.itemView.setOnClickListener(v -> 
			onCatClick(mCats[holder.getAdapterPosition()]));
	}
        @Override
        public int getItemCount() {
            return mCats.length;
        }
    }

//save cat as PNG
    private void shareCat(Cat cat) {
        final File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Cats");
        if (!dir.exists() && !dir.mkdirs()) {
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
                Uri uri = FileProvider.getUriForFile(requireActivity(), "ru.dimon6018.neko11.fileprovider", png);
                new MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.app_name_neko)
                        .setIcon(R.drawable.ic_success)
                        .setMessage(getString(R.string.picture_saved_successful, cat.getName()))              
                        .setNegativeButton(android.R.string.ok, null)
                        .show();
            } catch (IOException e) {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.app_name_neko)
                        .setIcon(R.drawable.ic_warning)
                        .setMessage(R.string.permission_denied)
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

        public CatHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(android.R.id.icon);
            textView = itemView.findViewById(android.R.id.title);
        }
    }
	@Override
	public void onResume() {
	isNekoLandOpened = true;
	updateCats();
	super.onResume();
}
	@Override
	public void onPause() {
	isNekoLandOpened = false;
	super.onPause();
	}
}