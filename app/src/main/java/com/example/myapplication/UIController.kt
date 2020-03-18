package com.example.myapplication

import android.graphics.Color
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat

class UIController(var mainActivity: MainActivity) {

    fun changeUIAccordingToResult(tr : GameEngine.TurnResult) {
        if (tr.phase == GameEngine.Phase.GAME_OVER) {
            return
        }
        val view = mainActivity.findViewById<Button>(mainActivity.buttons[tr.clickedTile!!.y][tr.clickedTile!!.x])

        val chosenColor = Color.RED
        val playerOneTokenColor = Color.BLUE
        val playerTwoTokenColor = Color.YELLOW


        if (tr.success) {
            when(tr.phase) {
                GameEngine.Phase.PLACEMENT -> {
                    if (tr.playerOneTurn) view.setBackgroundColor(playerOneTokenColor)
                    else view.setBackgroundColor(playerTwoTokenColor)
                }
                GameEngine.Phase.CHOOSING -> {
                    if (tr.from != null) {
                        mainActivity.findViewById<Button>(mainActivity.buttons[tr.from!!.y][tr.from!!.x])
                            .setBackgroundColor(if (tr.playerOneTurn) playerOneTokenColor else playerTwoTokenColor)
                    }
                    view.setBackgroundColor(chosenColor)
                }
                GameEngine.Phase.MOVING -> {
                    mainActivity.findViewById<Button>(mainActivity.buttons[tr.from!!.y][tr.from!!.x]).setBackgroundColor(
                        ContextCompat.getColor(mainActivity.applicationContext, R.color.gameBoardButtonBackgroundColor))
                    if (tr.playerOneTurn) view.setBackgroundColor(playerOneTokenColor) else view.setBackgroundColor(playerTwoTokenColor)
                }
                GameEngine.Phase.REMOVING -> {
                    view.setBackgroundColor(ContextCompat.getColor(mainActivity.applicationContext, R.color.gameBoardButtonBackgroundColor))
                    mainActivity.runOnUiThread { mainActivity.findViewById<TextView>(mainActivity.playerOneScore).setText(mainActivity.gameEngine.playerOneScore.toString())}
                    mainActivity.runOnUiThread {mainActivity.findViewById<TextView>(mainActivity.playerTwoScore).setText(mainActivity.gameEngine.playerTwoScore.toString())}

                }
            }
        } else {
            if (tr.phase == GameEngine.Phase.MOVING) {
                mainActivity.findViewById<Button>(mainActivity.buttons[tr.from!!.y][tr.from!!.x]).setBackgroundColor(if (tr.playerOneTurn) playerOneTokenColor else playerTwoTokenColor)
            }
        }
    }
}