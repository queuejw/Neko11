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
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import ru.dimon6018.neko11.NekoGeneralActivity;
import ru.dimon6018.neko11.R;
import ru.dimon6018.neko11.workers.Cat;
import ru.dimon6018.neko11.workers.NekoWorker;
import ru.dimon6018.neko11.workers.PrefState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static ru.dimon6018.neko11.controls.CatControlsFragment.randomfood;
import static ru.dimon6018.neko11.ui.activities.NekoSettingsActivity.SETTINGS;
import static ru.dimon6018.neko11.workers.NekoWorker.isWorkScheduled;

public class NekoLandFragment extends Fragment implements PrefState.PrefsListener {
    public static String CHAN_ID = "NEKO";

    private static final int STORAGE_PERM_REQUEST = 123;
	
    public static final int EXPORT_BITMAP_SIZE = 600;
	
    private PrefState mPrefs;
    private CatAdapter mAdapter;
    private Cat mPendingShareCat;
	
    int numCats;
    int numCatsP;

	public SharedPreferences nekoprefs;
	private RecyclerView recyclerView;
    private LinearLayout loadHolder;

    private MaterialTextView counter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
		mPrefs = new PrefState(getContext());
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.neko_activity_content, container, false);
		nekoprefs = requireContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.holder);
        counter = view.findViewById(R.id.catCounter);
        recyclerView.setItemAnimator(null);
        loadHolder = view.findViewById(R.id.loadHolderView);
        mPrefs.setListener(this);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        recyclerView.setAdapter(mAdapter = new CatAdapter());
    }
    @Override
    public void onDestroy() {
		mPrefs.setListener(null);
        super.onDestroy();
    }
    public int updateCats() {
            new Thread(() -> {
                List<Cat> list = mPrefs.getCats();
                Cat[] cats;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //See https://github.com/chris-blay/AndroidNougatEasterEgg/blob/master/workspace/src/com/covertbagel/neko/NekoLand.java
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
                numCatsP = cats.length;
                recyclerView.post(() -> {
                    mAdapter.setCats(cats);
                    updateLM();
                    updateCounter(numCatsP);
                    System.gc();

                }
                );
            }).start();
            return numCatsP;
}
	private void updateLM() {
	recyclerView.setLayoutManager( new GridLayoutManager(getContext(), mPrefs.getCatsInLineLimit()));
	}
  private void updateCounter(int catsNum) {
	SharedPreferences.Editor editor = nekoprefs.edit();
	editor.putInt("num", catsNum);
    editor.apply();
    loadHolder.setVisibility(View.GONE);
    counter.setText(getString(R.string.cat_counter, catsNum));
      if(nekoprefs.getBoolean("iCanEnterCatCount", true)) {
          mPrefs.addcatActionsUseAllTime(catsNum);
          nekoprefs.edit().putBoolean("iCanEnterCatCount", false).apply();
      }
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
     catico.setImageIcon(cat.createIcon(size, size));
     MaterialTextView status = bottomSheetInternal.findViewById(R.id.status_title);
	 MaterialTextView age = bottomSheetInternal.findViewById(R.id.cat_age);
     MaterialTextView mood = bottomSheetInternal.findViewById(R.id.mood_title);
     MaterialTextView actionsLimit = bottomSheetInternal.findViewById(R.id.actionsLimitTip);

     MaterialCardView wash = bottomSheetInternal.findViewById(R.id.wash_cat_sheet);
     MaterialCardView caress = bottomSheetInternal.findViewById(R.id.caress_cat_sheet);
     MaterialCardView touch = bottomSheetInternal.findViewById(R.id.touch_cat_sheet);

	 age.setText(getString(R.string.cat_age, cat.getAge()));
     mood.setText(getString(R.string.mood, mPrefs.getMoodPref(cat)));
     status.setText(getString(R.string.cat_status_string, cat.getStatus()));
     catEditor.setText(cat.getName());
     mPrefs.addcatActionsUseAllTime(1);
     updateCatActions(cat, wash, caress, touch, actionsLimit);
     bottomSheetInternal.findViewById(R.id.delete_sheet).setOnClickListener(v -> {
		bottomsheet.dismiss(); 
	    showCatRemoveDialog(cat, context);
	});
     textLayout.setEndIconOnClickListener(v -> {
                 new MaterialAlertDialogBuilder(context)
                         .setIcon(cat.createIcon(size, size).loadDrawable(context))
                         .setTitle(R.string.rename_title)
                         .setMessage(R.string.name_changed)
                         .setNegativeButton(android.R.string.ok, null)
                         .show();
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
      checkPerms(cat, context, false);
	 });
    bottomSheetInternal.findViewById(R.id.save_sheet_all).setOnClickListener(v -> {
        new MaterialAlertDialogBuilder(context)
                        .setIcon(cat.createIcon(size, size).loadDrawable(context))
                        .setTitle(R.string.save_title_all)
                        .setMessage(R.string.save_all_cats_q)
                        .setPositiveButton(android.R.string.yes, ((dialogInterface, i) -> {
                            bottomsheet.dismiss();
                            checkPerms(cat, context, true);
                        }))
                        .setNegativeButton(android.R.string.no, null)
                        .show();
    });
     bottomSheetInternal.findViewById(R.id.boosters_sheet).setOnClickListener(view -> {
         MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
         View v = this.getLayoutInflater().inflate(R.layout.cat_boosters_dialog, null);
         dialog.setView(v);
         MaterialTextView counter0 = v.findViewById(R.id.counter0);
         MaterialTextView counter1 = v.findViewById(R.id.counter1);
         MaterialCardView item0 = v.findViewById(R.id.moodBooster);
         MaterialCardView item1 = v.findViewById(R.id.luckyBooster);

         counter0.setText(getString(R.string.booster_items, mPrefs.getMoodBoosters()));
         counter1.setText(getString(R.string.booster_items, mPrefs.getLuckyBoosters()));
         item0.setEnabled(mPrefs.getMoodBoosters() != 0);
         item1.setEnabled(mPrefs.getLuckyBoosters() != 0);
         dialog.setCancelable(true);
         dialog.setNegativeButton(android.R.string.cancel, null);
         AlertDialog alertd = dialog.create();
         updateCatActions(cat, wash, caress, touch, actionsLimit);
         item0.setOnClickListener(view1 -> {
             mPrefs.addboostersUseAllTime(1);
             mPrefs.removeMoodBooster(1);
             mPrefs.setMood(cat, getString(R.string.mood5));
             bottomsheet.dismiss();
             alertd.dismiss();
         });
         item1.setOnClickListener(view1 -> {
             mPrefs.addboostersUseAllTime(1);
             if(!PrefState.isLuckyBoosterActive) {
                 mPrefs.removeLuckyBooster(1);
                 PrefState.isLuckyBoosterActive = true;
                 if (isWorkScheduled) {
                     NekoWorker.stopFoodWork(getContext());
                     NekoWorker.scheduleFoodWork(getContext(), randomfood / 4);
                     PrefState.isLuckyBoosterActive = false;
                 }
                 alertd.dismiss();
             } else {
                 new MaterialAlertDialogBuilder(context)
                         .setIcon(cat.createIcon(size, size).loadDrawable(context))
                         .setTitle(R.string.ops)
                         .setMessage(R.string.booster_actived_sub)
                         .setNegativeButton(android.R.string.ok, null)
                         .show();
             }
         });
         alertd.show();
     });
        wash.setOnClickListener(view -> {
            mPrefs.setCanInteract(cat, mPrefs.CanInteract(cat) - 1);
            updateCatActions(cat, wash, caress, touch, actionsLimit);
        Random r = new Random();
        int result = r.nextInt((5) + 1);
        if(result != 1) {
            String[] statusArray = context.getResources().getStringArray(R.array.toy_messages);
            String a = statusArray[r.nextInt(context.getResources().getStringArray(R.array.toy_messages).length)];

            String[] moods = {getString(R.string.mood3) ,getString(R.string.mood4)};
            String b = moods[r.nextInt(moods.length)];
            mPrefs.setMood(cat, b);
            NekoGeneralActivity.showSnackBar(a, Toast.LENGTH_SHORT, bottomSheetInternal);
            NekoWorker.notifyCat(requireContext(), cat, getResources().getString(R.string.meow));
            mPrefs.addNCoins(26);
        } else {
            NekoWorker.notifyCat(requireContext(), cat, getResources().getString(R.string.shh));

            String[] moods = {getString(R.string.mood1) ,getString(R.string.mood2)};
            String b = moods[r.nextInt(moods.length)];

            mPrefs.setMood(cat, b);
            new MaterialAlertDialogBuilder(context)
                    .setIcon(cat.createIcon(size, size).loadDrawable(context))
                    .setTitle(R.string.ops)
                    .setMessage(R.string.action1_fail)
                    .setNegativeButton(android.R.string.ok, null)
                    .show();
        }
        mood.setText(getString(R.string.mood, mPrefs.getMoodPref(cat)));
     });
    caress.setOnClickListener(view -> {
        mPrefs.setCanInteract(cat, mPrefs.CanInteract(cat) - 1);
        updateCatActions(cat, wash, caress, touch, actionsLimit);
        Random r = new Random();
        int result = r.nextInt((5) + 1);
        if(result != 1) {
            String[] moods = {getString(R.string.mood4) ,getString(R.string.mood5)};
            String b = moods[r.nextInt(moods.length)];
            mPrefs.setMood(cat, b);
            NekoGeneralActivity.showSnackBar("❤️", Toast.LENGTH_SHORT, bottomSheetInternal);
            NekoWorker.notifyCat(requireContext(), cat, getResources().getString(R.string.meow));
            mPrefs.addNCoins(31);
        } else {
            NekoWorker.notifyCat(requireContext(), cat, getResources().getString(R.string.shh));
            String[] moods = {getString(R.string.mood1), getString(R.string.mood2), getString(R.string.mood3)};
            String b = moods[r.nextInt(moods.length)];
            mPrefs.setMood(cat, b);
                new MaterialAlertDialogBuilder(context)
                        .setIcon(cat.createIcon(size, size).loadDrawable(context))
                        .setTitle(R.string.ops)
                        .setMessage(R.string.action2_fail)
                        .setNegativeButton(android.R.string.ok, null)
                        .show();
        }
        mood.setText(getString(R.string.mood, mPrefs.getMoodPref(cat)));
     });
   touch.setOnClickListener(view -> {
      mPrefs.setCanInteract(cat, mPrefs.CanInteract(cat) - 1);
      updateCatActions(cat, wash, caress, touch, actionsLimit);
       Random r = new Random();
       int result = r.nextInt((5) + 1);
       if(result != 1) {
           String[] moods = {getString(R.string.mood4) ,getString(R.string.mood5)};
           String b = moods[r.nextInt(moods.length)];
           mPrefs.setMood(cat, b);
           String[] statusArray = context.getResources().getStringArray(R.array.toy_messages);
           String a = statusArray[r.nextInt(context.getResources().getStringArray(R.array.toy_messages).length)];
           NekoWorker.notifyCat(requireContext(), cat, a);
           mPrefs.addNCoins(16);
       } else {
           String[] moods = {getString(R.string.mood1), getString(R.string.mood2), getString(R.string.mood3)};
           String b = moods[r.nextInt(moods.length)];
           mPrefs.setMood(cat, b);
           NekoWorker.notifyCat(requireContext(), cat, getResources().getString(R.string.shh));
               new MaterialAlertDialogBuilder(context)
                       .setIcon(cat.createIcon(size, size).loadDrawable(context))
                       .setTitle(R.string.ops)
                       .setMessage(R.string.touch_cat_error)
                       .setNegativeButton(android.R.string.ok, null)
                       .show();
       }
       mood.setText(getString(R.string.mood, mPrefs.getMoodPref(cat)));
     }); bottomsheet.show();
    }
    private void updateCatActions(Cat cat, View wash, View caress, View touch, View actionsLimit) {
        if(mPrefs.CanInteract(cat) <= 0) {
            wash.setEnabled(false);
            wash.setAlpha(0.5f);
            caress.setEnabled(false);
            caress.setAlpha(0.5f);
            touch.setEnabled(false);
            touch.setAlpha(0.5f);
            actionsLimit.setVisibility(View.VISIBLE);
        }
    }
    private void checkPerms(Cat cat, Context context, boolean iNeedSaveAllCats) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ActivityCompat.checkSelfPermission(context, MANAGE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    mPendingShareCat = cat;
                    requestPermissions(
                            new String[]{MANAGE_EXTERNAL_STORAGE},
                            STORAGE_PERM_REQUEST);
                    return;
                }
            } else {
                    if (ActivityCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        mPendingShareCat = cat;
                        requestPermissions(
                                new String[]{WRITE_EXTERNAL_STORAGE},
                                STORAGE_PERM_REQUEST);
                        return;
                    }
            }
        if(!iNeedSaveAllCats) {
            shareCat(cat, true);
        } else {
            saveAllCatsToGallery();
        }
    }

    private void saveAllCatsToGallery() {
        List<Cat> allCats = mPrefs.getCats();
        new Thread(() -> {
        for(int i = 0; i <= allCats.size() - 1; i++) {
            Cat cat = mPrefs.getCats().get(i);
            shareCat(cat, false);
        }
        recyclerView.post(() ->
                 new MaterialAlertDialogBuilder(getContext())
                .setIcon(getContext().getDrawable(R.drawable.ic_success))
                .setTitle(R.string.save_title)
                .setMessage(R.string.save_all_cats_done)
                .setPositiveButton(android.R.string.ok, null)
                .show());
        }).start();
    }

    private void showCatRemoveDialog(Cat cat, Context context) {
        final int size = context.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
            new MaterialAlertDialogBuilder(context)
                    .setIcon(cat.createIcon(size, size).loadDrawable(context))
                    .setTitle(R.string.delete_cat_title)
                    .setMessage(R.string.delete_cat_message)
                    .setPositiveButton(R.string.delete_cat_title, (dialog, id) -> onCatRemove(cat))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
    }
    private void onCatRemove(Cat cat) {
         Random random = new Random(cat.getSeed());
		 mPrefs.removeCat(cat);
         mPrefs.addNCoins(random.nextInt(50 - 10) + 5);
    }
    private void showCatFull(Cat cat) {
            final Context context = new ContextThemeWrapper(getContext(),
                    requireActivity().getTheme());
            View view = LayoutInflater.from(context).inflate(R.layout.cat_fullscreen_view, null);
            final ImageView ico = view.findViewById(R.id.cat_ico);
            ico.setImageBitmap(cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE));
            new MaterialAlertDialogBuilder(context)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
    }
    @Override
    public void onPrefsChanged() {
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
            holder.imageView.setImageIcon(mCats[position].createIcon(mPrefs.getCatIconSize(), mPrefs.getCatIconSize()));
            holder.textView.setText(mCats[position].getName());
            holder.itemView.setOnClickListener(v -> onCatClick(mCats[holder.getAbsoluteAdapterPosition()]));
	}
        @Override
        public int getItemCount() {
            return mCats.length;
        }
    }

//save cat as PNG
    private void shareCat(Cat cat, boolean iShouldShowDialog) {
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
                if(iShouldShowDialog) {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setTitle(R.string.app_name_neko)
                            .setIcon(R.drawable.ic_success)
                            .setMessage(getString(R.string.picture_saved_successful, cat.getName()))
                            .setNegativeButton(android.R.string.ok, null)
                            .show();
                }
            } catch (IOException e) {
                if(iShouldShowDialog) {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setTitle(R.string.app_name_neko)
                            .setIcon(R.drawable.ic_warning)
                            .setMessage(R.string.permission_denied)
                            .setNegativeButton(android.R.string.ok, null)
                            .show();
                }
            }
        }
    }
    @SuppressWarnings("deprecation")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERM_REQUEST) {
            if (mPendingShareCat != null) {
                shareCat(mPendingShareCat, true);
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
            return oldCats.getName().equals(newCats.getName());
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
    super.onResume();
    mPrefs.setListener(this);
    numCats = updateCats();
}
	@Override
	public void onPause() {
    mPrefs.setListener(null);
	super.onPause();
	}
}
