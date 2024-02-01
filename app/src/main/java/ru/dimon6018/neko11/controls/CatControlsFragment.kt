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
package ru.dimon6018.neko11.controls

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import ru.dimon6018.neko11.R
import ru.dimon6018.neko11.ui.activities.NekoSettingsActivity
import ru.dimon6018.neko11.workers.NekoToiletWorker
import ru.dimon6018.neko11.workers.NekoToyWorker
import ru.dimon6018.neko11.workers.NekoWorker
import ru.dimon6018.neko11.workers.PrefState
import ru.dimon6018.neko11.workers.PrefState.PrefsListener
import java.util.Random

class CatControlsFragment : Fragment(), PrefsListener {
    private var foodCard: MaterialCardView? = null
    private var waterCard: MaterialCardView? = null
    private var toyCard: MaterialCardView? = null
    private var toiletCard: MaterialCardView? = null
    private var foodstatusimg: ImageView? = null
    private var toystatusimg: ImageView? = null
    private var waterstatusimg: ImageView? = null
    private var foodstatetxt: MaterialTextView? = null
    private var toystatus: MaterialTextView? = null
    private var toystatetxt: MaterialTextView? = null
    private var toysub: MaterialTextView? = null
    private var foodsub: MaterialTextView? = null
    private var waterstatesub: MaterialTextView? = null
    private var waterDrinkTip: MaterialTextView? = null
    private var toiletStatesub: MaterialTextView? = null
    private var waterstatetxt: MaterialTextView? = null
    private var boosterActivedSub: MaterialTextView? = null
    private var mPrefs: PrefState? = null
    private var nekoprefs: SharedPreferences? = null
    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        nekoprefs = requireActivity().getSharedPreferences(NekoSettingsActivity.SETTINGS, Context.MODE_PRIVATE)
        val linearControl = nekoprefs!!.getBoolean("linear_control", false)
        val layout: Int = if (linearControl) {
            R.layout.fragment_cat_controls_linear
        } else {
            R.layout.fragment_cat_controls
        }
        if (nekoprefs!!.getInt("state", 1) == 2) {
            showTipAgain = false
            createTipDialog(context)
        }
        val view = inflater.inflate(layout, container, false)
        mPrefs = PrefState(requireContext())
        toystatusimg = view.findViewById(R.id.toy_state_img)
        foodCard = view.findViewById(R.id.card_food)
        waterCard = view.findViewById(R.id.card_water)
        toyCard = view.findViewById(R.id.card_toy)
        toiletCard = view.findViewById(R.id.toilet_card)
        foodstatusimg = view.findViewById(R.id.food_state_view)
        toystatusimg = view.findViewById(R.id.toy_state_img)
        toystatus = view.findViewById(R.id.toy_status)
        waterstatusimg = view.findViewById(R.id.water_state_view)
        foodstatetxt = view.findViewById(R.id.food_status_txt)
        toystatetxt = view.findViewById(R.id.toy_state)
        waterstatetxt = view.findViewById(R.id.water_state)
        toysub = view.findViewById(R.id.toy_state_sub)
        foodsub = view.findViewById(R.id.foodsub)
        waterstatesub = view.findViewById(R.id.water_state_sub)
        toiletStatesub = view.findViewById(R.id.toilet_state_sub)
        boosterActivedSub = view.findViewById(R.id.booster_actived_sub)
        waterDrinkTip = view.findViewById(R.id.drink_tip)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = context
        toystatusimg!!.setImageResource(toyiconint)
        setupTiles(context)
        if(nekoprefs?.getBoolean("legacyGameplay", false)!!) {
            toiletCard?.visibility = View.GONE
        }
        updateTiles()
    }
    override fun onResume() {
        super.onResume()
        mPrefs?.setListener(this)
    }
    override fun onPause() {
        mPrefs?.setListener(null)
        super.onPause()
    }
    private fun setupTiles(context: Context?) {
        foodCard!!.setOnClickListener {
            startAnim(foodCard)
            val currentstate = mPrefs!!.foodState
            if (currentstate == 0) {
                if(!mPrefs!!.isLegacyFoodEnabled()) {
                    if (PrefState.isLuckyBoosterActive) {
                        NekoWorker.scheduleFoodWork(context, randomfood / 4)
                    } else {
                        NekoWorker.scheduleFoodWork(context, randomfood)
                    }
                    mPrefs!!.foodState = foodstaterandom
                } else {
                    foodDialog()
                }
            } else {
                mPrefs!!.foodState = 0
                NekoWorker.stopFoodWork(context)
            }
            updateTiles()
        }
        toyCard!!.setOnClickListener {
            startAnim(toyCard)
            if (mPrefs!!.toyState == 0) {
                mPrefs!!.toyState = 1
                NekoToyWorker.scheduleToyWork(context)
            } else {
                mPrefs!!.toyState = 0
                NekoToyWorker.stopToyWork(context)
            }
            updateTiles()
        }
        toiletCard!!.setOnClickListener {
            startAnim(toiletCard)
            if (mPrefs!!.getToiletState() == 0) {
                mPrefs!!.setToiletState(1)
                NekoToiletWorker.scheduleToiletWork(context)
            } else {
                mPrefs!!.setToiletState(0)
                NekoToiletWorker.stopToiletWork(context)
            }
            updateTiles()
        }
        waterCard!!.setOnClickListener {
            startAnim(waterCard)
            if (mPrefs!!.isLegacyFoodEnabled()) {
                waterDialog()
            } else {
                mPrefs!!.waterState = 200f
            }
            updateTiles()
        }
        waterCard!!.setOnLongClickListener {
            startAnim(waterCard)
            mPrefs!!.waterState = 0f
            updateTiles()
            true
        }
    }
    private fun getCurrentFoodIco(context: Context): Drawable? {
        if(!mPrefs!!.isLegacyFoodEnabled()) {
            return if(mPrefs!!.foodState != 0) {
                AppCompatResources.getDrawable(context, R.drawable.ic_foodbowl_filled)
            } else {
                AppCompatResources.getDrawable(context, R.drawable.ic_bowl)
            }
        } else {
            return when(mPrefs!!.foodState) {
                0 -> AppCompatResources.getDrawable(context, R.drawable.ic_bowl)
                1 -> AppCompatResources.getDrawable(context, R.drawable.food_steak)
                2 -> AppCompatResources.getDrawable(context, R.drawable.food_hot_dog)
                3 -> AppCompatResources.getDrawable(context, R.drawable.food_peanut)
                4 -> AppCompatResources.getDrawable(context, R.drawable.food_pasta)
                5 -> AppCompatResources.getDrawable(context, R.drawable.food_drumstick)
                6 -> AppCompatResources.getDrawable(context, R.drawable.food_croissant)
                else -> AppCompatResources.getDrawable(context, R.drawable.ic_bowl)
            }
        }
    }
    private fun getCurrentWaterIco(context: Context): Drawable? {
        val waterml = Math.round(mPrefs!!.waterState).toFloat()
        return if(!mPrefs!!.isLegacyFoodEnabled()) {
            if(waterml <= 25f) {
                AppCompatResources.getDrawable(context, R.drawable.ic_water)
            } else {
                AppCompatResources.getDrawable(context, R.drawable.ic_water_filled)
            }
        } else {
            when(mPrefs!!.getWaterType()) {
                0 -> AppCompatResources.getDrawable(context, R.drawable.ic_water_filled)
                1 -> AppCompatResources.getDrawable(context, R.drawable.drink_milk)
                else -> AppCompatResources.getDrawable(context, R.drawable.ic_water_filled)
            }
        }
    }
    private fun foodDialog() {
        val foodSheet = BottomSheetDialog(requireActivity())
        foodSheet.setContentView(R.layout.food_dialog)
        foodSheet.dismissWithAnimation = true
        val bottomSheetInternal = foodSheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        BottomSheetBehavior.from(bottomSheetInternal!!).peekHeight = requireActivity().resources.getDimensionPixelSize(R.dimen.bottomsheet)
        val food0 = bottomSheetInternal.findViewById<MaterialCardView>(R.id.food_0)
        val food1 = bottomSheetInternal.findViewById<MaterialCardView>(R.id.food_1)
        val food2 = bottomSheetInternal.findViewById<MaterialCardView>(R.id.food_2)
        val food3 = bottomSheetInternal.findViewById<MaterialCardView>(R.id.food_3)
        val food4 = bottomSheetInternal.findViewById<MaterialCardView>(R.id.food_4)
        val food5 = bottomSheetInternal.findViewById<MaterialCardView>(R.id.food_5)
        food0.setOnClickListener {
            mPrefs!!.foodState = 1
            if(!mPrefs!!.isLegacyFoodEnabled()) {
                if (PrefState.isLuckyBoosterActive) {
                    NekoWorker.scheduleFoodWork(context, randomfood / 4)
                } else {
                    NekoWorker.scheduleFoodWork(context, randomfood)
                }
            }
            foodSheet.dismiss()
        }
        food1.setOnClickListener {
            mPrefs!!.foodState = 2
            if(!mPrefs!!.isLegacyFoodEnabled()) {
                if (PrefState.isLuckyBoosterActive) {
                    NekoWorker.scheduleFoodWork(context, randomfood / 4)
                } else {
                    NekoWorker.scheduleFoodWork(context, randomfood)
                }
            }
            foodSheet.dismiss()
        }
        food2.setOnClickListener {
            mPrefs!!.foodState = 3
            if(!mPrefs!!.isLegacyFoodEnabled()) {
                if (PrefState.isLuckyBoosterActive) {
                    NekoWorker.scheduleFoodWork(context, randomfood / 4)
                } else {
                    NekoWorker.scheduleFoodWork(context, randomfood)
                }
            }
            foodSheet.dismiss()
        }
        food3.setOnClickListener {
            mPrefs!!.foodState = 4
            if(!mPrefs!!.isLegacyFoodEnabled()) {
                if (PrefState.isLuckyBoosterActive) {
                    NekoWorker.scheduleFoodWork(context, randomfood / 4)
                } else {
                    NekoWorker.scheduleFoodWork(context, randomfood)
                }
            }
            foodSheet.dismiss()
        }
        food4.setOnClickListener {
            mPrefs!!.foodState = 5
            if(!mPrefs!!.isLegacyFoodEnabled()) {
                if (PrefState.isLuckyBoosterActive) {
                    NekoWorker.scheduleFoodWork(context, randomfood / 4)
                } else {
                    NekoWorker.scheduleFoodWork(context, randomfood)
                }
            }
            foodSheet.dismiss()
        }
        food5.setOnClickListener {
            mPrefs!!.foodState = 6
            if(!mPrefs!!.isLegacyFoodEnabled()) {
                if (PrefState.isLuckyBoosterActive) {
                    NekoWorker.scheduleFoodWork(context, randomfood / 4)
                } else {
                    NekoWorker.scheduleFoodWork(context, randomfood)
                }
            }
            foodSheet.dismiss()
        }
        foodSheet.show()
    }
    private fun waterDialog() {
        val waterSheet = BottomSheetDialog(requireActivity())
        waterSheet.setContentView(R.layout.water_dialog)
        waterSheet.dismissWithAnimation = true
        val bottomSheetInternal = waterSheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        BottomSheetBehavior.from(bottomSheetInternal!!).peekHeight = requireActivity().resources.getDimensionPixelSize(R.dimen.bottomsheet)
        val drink0 = bottomSheetInternal.findViewById<MaterialCardView>(R.id.drink_0)
        val drink1 = bottomSheetInternal.findViewById<MaterialCardView>(R.id.drink_1)
        drink0.setOnClickListener {
            mPrefs!!.setWaterType(0)
            mPrefs!!.waterState = 200f
            waterSheet.dismiss()
        }
        drink1.setOnClickListener {
            mPrefs!!.setWaterType(1)
            mPrefs!!.waterState = 200f
            waterSheet.dismiss()
        }
        waterSheet.show()
    }
    override fun onPrefsChanged() {
        updateTiles()
    }
    private fun updateTiles() {
        //update food card
        if (mPrefs!!.foodState == 0) {
            foodstatetxt!!.setText(R.string.control_food_status_empty)
            foodsub!!.visibility = View.VISIBLE
            foodCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.foodbg2))
        } else {
            foodstatetxt!!.setText(R.string.control_food_status_full)
            foodsub!!.visibility = View.INVISIBLE
            foodCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.foodbg))
        }
        foodstatusimg!!.setImageDrawable(getCurrentFoodIco(requireContext()))
        if (PrefState.isLuckyBoosterActive) {
            boosterActivedSub!!.visibility = View.VISIBLE
        } else {
            boosterActivedSub!!.visibility = View.GONE
        }
        //update water card
        val waterml = Math.round(mPrefs!!.waterState).toFloat()
        waterstatetxt!!.text = resources.getString(R.string.water_state_ml, waterml)
        if (waterml >= 100f) {
            when(mPrefs!!.getWaterType()) {
                0 -> waterCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.waterbg))
                1 -> waterCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.milkbg))
            }
            waterstatesub!!.visibility = View.INVISIBLE
        } else {
            when(mPrefs!!.getWaterType()) {
                0 -> waterCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.waterbg2))
                1 -> waterCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.milkbg2))
            }
            waterstatesub!!.visibility = View.VISIBLE
        }
        if (waterml <= 25f) {
            when(mPrefs!!.getWaterType()) {
                0 -> waterCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.waterbg3))
                1 -> waterCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.milkbg3))
            }
            waterstatesub!!.visibility = View.VISIBLE
        }
        when(mPrefs!!.getWaterType()) {
            0 -> waterDrinkTip!!.text = getString(R.string.control_water_title)
            1 -> {
                waterDrinkTip!!.text = getString(R.string.drink_milk)
                waterDrinkTip!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_light2))
            }
        }
        waterstatusimg!!.setImageDrawable(getCurrentWaterIco(requireContext()))
        //update toy card
        if (mPrefs!!.toyState == 1) {
            toyCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.toybg))
            toystatus!!.visibility = View.VISIBLE
            toysub!!.visibility = View.INVISIBLE
        } else {
            toyCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.toybg2))
            toystatus!!.visibility = View.INVISIBLE
            toysub!!.visibility = View.VISIBLE
        }
        //update toilet card
        if (mPrefs!!.getToiletState() == 0) {
            toiletCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.toiletbg2))
            toiletStatesub!!.visibility = View.VISIBLE
        } else {
            toiletCard!!.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.toiletbg))
            toiletStatesub!!.visibility = View.INVISIBLE
        }
    }

    private fun startAnim(view: View?) {
        view?.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_out))
        view?.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in))
    }

    private fun createTipDialog(context: Context?) {
        if (!showTipAgain) {
            if (context != null) {
                MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.app_name_neko)
                        .setIcon(R.drawable.ic_bowl)
                        .setMessage(R.string.welcome_dialog_part3)
                        .setPositiveButton(android.R.string.ok, null).show()
            }
        }
    }
    companion object {
        var showTipAgain = true
        val randomfood = Random().nextInt(248 - 10 + 1) + 10
        val foodstaterandom = Random().nextInt(11 - 1 + 1) + 1
        @JvmField
        val randomWater = Random().nextInt(150 - 12 + 1) + 5
        private var TOY_ICONS = intArrayOf(R.drawable.ic_toy_ball, R.drawable.ic_toy_fish, R.drawable.ic_toy_mouse, R.drawable.ic_toy_laser)
        var toyiconint = TOY_ICONS[Random().nextInt(TOY_ICONS.size)]
    }
}