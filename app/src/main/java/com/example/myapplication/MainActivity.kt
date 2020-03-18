package com.example.myapplication

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.game_statistics.*
import java.util.*

class MainActivity : AppCompatActivity() {


    private lateinit var viewModel : MyViewModel
    private lateinit var UIController : UIController
    lateinit var gameEngine : GameEngine

    val buttons = arrayOf(
        intArrayOf(R.id.button_row0_col0, R.id.button_row0_col1, R.id.button_row0_col2, R.id.button_row0_col3, R.id.button_row0_col4, R.id.button_row0_col5),
        intArrayOf(R.id.button_row1_col0, R.id.button_row1_col1, R.id.button_row1_col2, R.id.button_row1_col3, R.id.button_row1_col4, R.id.button_row1_col5),
        intArrayOf(R.id.button_row2_col0, R.id.button_row2_col1, R.id.button_row2_col2, R.id.button_row2_col3, R.id.button_row2_col4, R.id.button_row2_col5),
        intArrayOf(R.id.button_row3_col0, R.id.button_row3_col1, R.id.button_row3_col2, R.id.button_row3_col3, R.id.button_row3_col4, R.id.button_row3_col5),
        intArrayOf(R.id.button_row4_col0, R.id.button_row4_col1, R.id.button_row4_col2, R.id.button_row4_col3, R.id.button_row4_col4, R.id.button_row4_col5)
    )

    var playerOneScore = R.id.playerScoreTextView
    var playerTwoScore = R.id.opponentScoreTextView
    val radButton1 = R.id.first_radio


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MyViewModel::class.java)
        gameEngine = viewModel.getGameEngine()
        UIController = UIController(this)
        gameEngine.uiController = UIController

        playerScoreTextView.text = gameEngine.playerOneScore.toString()
        opponentScoreTextView.text = gameEngine.playerTwoScore.toString()
        redrawBoard()
        if (!gameEngine.timerHasStarted) {
            gameEngine.activateAITimer()
        }

    }

    fun clickTile(view : View) {
        if (!gameEngine.isAiTurn()) {
            val tr = gameEngine.makeTurn(view.resources.getResourceEntryName(view.id))
            UIController.changeUIAccordingToResult(tr)
        }
    }

    fun playerControlChanged(view : View) {
        val switchId = view.resources.getResourceEntryName(view.id)
        if (switchId == "switch1") {
            gameEngine.playerOneIsRealPlayer = (view as Switch).isChecked
            Log.d("TAG", "Player one is now real player: " + view.isChecked)
        } else if (switchId == "switch2"){
            gameEngine.playerTwoIsRealPlayer = (view as Switch).isChecked
            Log.d("TAG", "Player two is now real player: " + view.isChecked)
        }
    }

    fun whoGoesFirst(view : View) {
        val radButtonId = view.resources.getResourceEntryName(view.id)
        gameEngine.playerOnesTurn = radButtonId == "first_radio"
    }

    fun restartAll(view : View) {
        gameEngine.initParams()
        gameEngine.reset()

        findViewById<RadioButton>(radButton1).isChecked = true
        redrawBoard()


        if (!gameEngine.timerHasStarted) {
            gameEngine.activateAITimer()
        }
    }

    fun redrawBoard() {
        for (tileRow in gameEngine.gameBoard) {
            for (tile in tileRow) {
                val color = if (tile.label == "X") Color.BLUE
                else if (tile.label == "O") Color.YELLOW
                else ContextCompat.getColor(applicationContext, R.color.gameBoardButtonBackgroundColor)

                findViewById<Button>(buttons[tile.y][tile.x]).setBackgroundColor(color)
            }
        }
    }
}
