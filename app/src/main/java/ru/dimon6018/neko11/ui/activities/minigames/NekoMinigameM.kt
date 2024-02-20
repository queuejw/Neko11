package ru.dimon6018.neko11.ui.activities.minigames

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
import ru.dimon6018.neko11.R
import ru.dimon6018.neko11.workers.Cat

class NekoMinigameM : Activity() {

    private var mLand: MLand? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.minigame_1)
        mLand = findViewById<View>(R.id.world) as MLand
        mLand!!.setScoreFieldHolder(findViewById<View>(R.id.scores) as ViewGroup)
        val welcome = findViewById<View>(R.id.welcome)
        mLand!!.setSplash(welcome)
        val numControllers = mLand!!.getGameControllers().size
        if (numControllers > 0) {
            mLand!!.setupPlayers(numControllers)
        }
        val plus: AppCompatImageButton = findViewById(R.id.player_plus_button)
        val minus: AppCompatImageButton = findViewById(R.id.player_minus_button)
        val start: FrameLayout = findViewById(R.id.play_button)
        start.setOnClickListener {
            plus.visibility = View.INVISIBLE
            minus.visibility = View.INVISIBLE
            mLand!!.start(true)
        }
        plus.setOnClickListener {
            playerPlus()
        }
        minus.setOnClickListener {
            playerMinus()
        }
    }

    private fun updateSplashPlayers() {
        val n = mLand!!.numPlayers
        val minus = findViewById<View>(R.id.player_minus_button)
        val plus = findViewById<View>(R.id.player_plus_button)
        if (n == 1) {
            minus.visibility = View.INVISIBLE
            plus.visibility = View.VISIBLE
            plus.requestFocus()
        } else if (n == MLand.MAX_PLAYERS) {
            minus.visibility = View.VISIBLE
            plus.visibility = View.INVISIBLE
            minus.requestFocus()
        } else {
            minus.visibility = View.VISIBLE
            plus.visibility = View.VISIBLE
        }
    }

    public override fun onPause() {
        mLand!!.stop()
        super.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
        finish()
    }
    public override fun onResume() {
        super.onResume()
        mLand!!.onAttachedToWindow() // resets and starts animation
        updateSplashPlayers()
        mLand!!.showSplash()
    }
    private fun playerMinus() {
        mLand!!.removePlayer()
        updateSplashPlayers()
    }

    private fun playerPlus() {
        mLand!!.addPlayer()
        updateSplashPlayers()
    }
    companion object {

        private var catM: Cat? = null

        fun setCat(newCat: Cat) {
            catM = newCat
        }
        fun getCat(): Cat {
            return catM!!
        }
    }
}