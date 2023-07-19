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

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;
import static ru.dimon6018.neko11.workers.Cat.CatParts.setHatDrawable;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import ru.dimon6018.neko11.NekoGeneralActivity;
import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.workers.Cat;
import ru.dimon6018.neko11.workers.PrefState;

public class NekoLandFragment extends Fragment implements PrefState.PrefsListener {
    public static String CHAN_ID = "NEKO";

    private static final int STORAGE_PERM_REQUEST = 123;
	
    public static final int EXPORT_BITMAP_SIZE = 600;
	
    private PrefState mPrefs;
    private CatAdapter mAdapter;
    private Cat mPendingShareCat;
	
	private int numCats;
	
	private MaterialTextView counter;
	public SharedPreferences nekoprefs;
	private RecyclerView recyclerView;

    RecyclerView.RecycledViewPool Pool = new RecyclerView.RecycledViewPool();


    @Override
    public void onCreate(Bundle savedInstanceState) {
		mPrefs = new PrefState(getContext());
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {	
		nekoprefs = requireContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        mPrefs.setListener(this);
        return inflater.inflate(R.layout.neko_activity_content, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
	 	counter = view.findViewById(R.id.counter);
		recyclerView = view.findViewById(R.id.holder);
        recyclerView.setRecycledViewPool(Pool);
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        switch (mPrefs.getSortState()) {
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
                list.sort(Comparator.comparing(Cat::getName));
                break;
            //off
            case 0:
                break;
        }
    }
	cats = list.toArray(new Cat[0]);
	int catsNum = cats.length;
	updateCounter(catsNum);
	mAdapter.setCats(cats);
	return catsNum;
}
	private void updateLM() {
    GridLayoutManager grid = new GridLayoutManager(getContext(), mPrefs.getCatsInLineLimit());
    grid.setInitialPrefetchItemCount(24);
	recyclerView.setLayoutManager(grid);
	}
  private void updateCounter(int catsNum) {
    counter.setText(getString(R.string.cat_counter, catsNum));
	SharedPreferences.Editor editor = nekoprefs.edit();
	editor.putInt("num", catsNum);
    editor.apply();
}
    private void onCatClick(Cat cat) {
	 Context context = requireContext();
     BottomSheetDialog bottomsheet = new BottomSheetDialog(context);
     bottomsheet.setContentView(R.layout.neko_cat_bottomsheet);
     bottomsheet.setDismissWithAnimation(true);
	 View bottomSheetInternal = bottomsheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
     BottomSheetBehavior.from(bottomSheetInternal).setPeekHeight(context.getResources().getDimensionPixelSize(R.dimen.bottomsheet));
	 TextInputLayout textLayout = bottomSheetInternal.findViewById(R.id.catNameField);
	 EditText catEditor = bottomSheetInternal.findViewById(R.id.catEditName);
	 ImageView catico = bottomSheetInternal.findViewById(R.id.cat_icon);
     int size = context.getResources().getDimensionPixelSize(R.dimen.neko_display_size);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            catico.setImageIcon(cat.createIcon(size, size));
        } else {
            catico.setImageBitmap(cat.createBitmap(size, size));
        }
     MaterialTextView status = bottomSheetInternal.findViewById(R.id.status_title);
	 MaterialTextView age = bottomSheetInternal.findViewById(R.id.cat_age);

	 age.setText(getString(R.string.cat_age, cat.getAge()));
     status.setText(getString(R.string.cat_status_string, cat.getStatus()));
     catEditor.setText(cat.getName());

     bottomSheetInternal.findViewById(R.id.delete_sheet).setOnClickListener(v -> {
		bottomsheet.dismiss(); 
	    showCatRemoveDialog(cat, context);
	});
     textLayout.setEndIconOnClickListener(v -> {
     NekoGeneralActivity.showSnackBar(getString(R.string.name_changed), 3, bottomSheetInternal);
     bottomsheet.dismiss();
     cat.setName(catEditor.getText().toString().trim());
     mPrefs.addCat(cat);	 
	});
     catico.setOnClickListener(v -> {
		 bottomsheet.dismiss();
	     showCatFull(cat);	
	});
        bottomSheetInternal.findViewById(R.id.save_sheet).setOnClickListener(v -> {
	  bottomsheet.dismiss();
         checkPerms(cat, context);
	 });
    bottomSheetInternal.findViewById(R.id.wash_cat_sheet).setOnClickListener(view -> {
         NekoGeneralActivity.showSnackBar("вы искупали кота", 3, bottomSheetInternal);
     });
    bottomSheetInternal.findViewById(R.id.caress_cat_sheet).setOnClickListener(view -> {
         NekoGeneralActivity.showSnackBar("вы погладили кота", 3, bottomSheetInternal);
     });
   bottomSheetInternal.findViewById(R.id.touch_cat_sheet).setOnClickListener(view -> {
         NekoGeneralActivity.showSnackBar("вы потрогали кота", 3, bottomSheetInternal);
     });
   bottomSheetInternal.findViewById(R.id.skins_sheet).setOnClickListener(view -> {
   });
	 bottomsheet.show();
    }
    private void checkPerms(Cat cat, Context context) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ActivityCompat.checkSelfPermission(context, MANAGE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    mPendingShareCat = cat;
                    requestPermissions(
                            new String[]{MANAGE_EXTERNAL_STORAGE},
                            STORAGE_PERM_REQUEST);
                    return;
                }
                shareCat(cat);
            } else {
                    if (ActivityCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        mPendingShareCat = cat;
                        requestPermissions(
                                new String[]{WRITE_EXTERNAL_STORAGE},
                                STORAGE_PERM_REQUEST);
                        return;
                    }
                    shareCat(cat);
                }
            }
	private void showCatRemoveDialog(Cat cat, Context context) {
        final int size = context.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            new MaterialAlertDialogBuilder(context)
                    .setIcon(cat.createIcon(size, size).loadDrawable(context))
                    .setTitle(R.string.delete_cat_title)
                    .setMessage(R.string.delete_cat_message)
                    .setPositiveButton(R.string.delete_cat, (dialog, id) -> onCatRemove(cat))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            new MaterialAlertDialogBuilder(context)
                    .setIcon(R.drawable.ic_fullcat_icon)
                    .setTitle(R.string.delete_cat_title)
                    .setMessage(R.string.delete_cat_message)
                    .setPositiveButton(R.string.delete_cat, (dialog, id) -> onCatRemove(cat))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }
    private void onCatRemove(Cat cat) {
		 mPrefs.removeCat(cat);
    }
	private void showCatFull(Cat cat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final Context context = new ContextThemeWrapper(getContext(),
                    requireActivity().getTheme());
            View view = LayoutInflater.from(context).inflate(R.layout.cat_fullscreen_view, null);
            final ImageView ico = view.findViewById(R.id.cat_ico);
            ico.setImageBitmap(cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE));
            new MaterialAlertDialogBuilder(context)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } else {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.cat_fullscreen_view, null);
            final ImageView ico = view.findViewById(R.id.cat_ico);
            ico.setImageBitmap(cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE));
            new MaterialAlertDialogBuilder(getContext())
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }
    @Override
    public void onPrefsChanged() {
		updateLM();
		updateCats();
    }

    private class CatAdapter extends RecyclerView.Adapter<CatHolder> {

        private Cat[] mCats;

        public void setCats(Cat[] cats) {
        mCats = cats;
        List<Cat> catList = new LinkedList<>();
        DiffUtilCallback diffUtilCallback = new DiffUtilCallback(mPrefs.getCats(), catList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtilCallback, false);
        diffResult.dispatchUpdatesTo(mAdapter);
    }
        @NonNull
        @Override
        public CatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CatHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cat_view, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull final CatHolder holder, int position) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.imageView.setImageIcon(mCats[position].createIcon(mPrefs.getCatIconSize(), mPrefs.getCatIconSize()));
            } else {
                holder.imageView.setImageBitmap(mCats[position].createBitmap(mPrefs.getCatIconSize(), mPrefs.getCatIconSize()));
            }
            holder.textView.setText(mCats[position].getName());
            holder.itemView.setOnClickListener(v ->
			onCatClick(mCats[holder.getAbsoluteAdapterPosition()]));
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
    public static class DiffUtilCallback extends DiffUtil.Callback {

        private final List<Cat> oldCatsList;
        private final List<Cat> newCatsList;

        public DiffUtilCallback(List<Cat> oldList, List<Cat> newList) {
            this.oldCatsList = oldList;
            this.newCatsList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldCatsList.size();
        }

        @Override
        public int getNewListSize() {
            return newCatsList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Cat oldCats = oldCatsList.get(oldItemPosition);
            Cat newCats = newCatsList.get(newItemPosition);
            return oldCats.getSeed() == newCats.getSeed();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Cat oldCats = oldCatsList.get(oldItemPosition);
            Cat newCats = newCatsList.get(newItemPosition);
            return oldCats.getName().equals(newCats.getName())
                    && oldCats.getSeed() == newCats.getSeed();
        }
    }
    private static class CatHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textView;

        public CatHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.icon);
            textView = itemView.findViewById(R.id.title);
        }
    }
	@Override
	public void onResume() {
    mPrefs.setListener(this);
    updateCats();
	super.onResume();
}
	@Override
	public void onPause() {
    mPrefs.setListener(null);
	super.onPause();
	}
}
