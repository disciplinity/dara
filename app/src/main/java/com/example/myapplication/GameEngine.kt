package com.example.myapplication

import android.util.Log
import java.util.*
import kotlin.math.abs

class GameEngine() {

    data class Tile(var label: String) {
        var x: Int = -1
        var y: Int = -1

        override fun toString(): String {
            return String.format("Tile %s [%d,%d]", label, x, y)
        }
    }

    data class TurnResult(var phase: Phase, var success: Boolean, var playerOneTurn: Boolean, var from: Tile?) {
        var clickedTile : Tile? = null // ultimate handicap
    }

    enum class Phase {
        PLACEMENT,
        CHOOSING,
        MOVING,
        REMOVING,
        GAME_OVER
    }

    val boardWidth = 6
    val boardHeight = 5
    var playerOnesTurn = true
    var canChangeWhosTurnItIs = true
    var playerOneIsRealPlayer = true
    var playerTwoIsRealPlayer = false

    private val playerOneLabel = "X"
    private val playerTwoLabel = "O"

    private var playerOneTokensNotPlaced = 12
    private var playerTwoTokensNotPlaced = 12
    private var playerOneTokensPlaced = 0
    private var playerTwoTokensPlaced = 0
    var playerOneScore = 0
    var playerTwoScore = 0


    var currentlyChosenTile : Tile? = null
    private var hadACombo = false

    private var ai1 = AI(true, this)
    private var ai2 = AI(false, this)

    lateinit var gameBoard: Array<Array<Tile>>
    lateinit var uiController : UIController

    init {
        generateBoard()
    }

    var timerHasStarted = false
    lateinit var timer : Timer

    fun activateAITimer() {
        timer = Timer()
        var timerTask = object : TimerTask() {
            override fun run() {
                if (isAiTurn()) {
                    val turnResult = startAI()
                    uiController.changeUIAccordingToResult(turnResult)
                }
            }
        }
        timer.scheduleAtFixedRate(timerTask,0,1000)
        timerHasStarted = true
    }

    fun stopAITimer() {
        if (timerHasStarted) {
            timerHasStarted = false
            timer.cancel()
        }
    }

    private fun generateBoard() {
        gameBoard = Array(boardHeight) { Array(boardWidth) { Tile("")} }
        for (y in 0 until boardHeight) {
            for (x in 0 until boardWidth) {
                gameBoard[y][x].x = x
                gameBoard[y][x].y = y
            }
        }
    }

    fun makeTurn(tile : Tile) : TurnResult {
        return makeTurn(getButtonNameByTile(tile))
    }

    fun makeTurn(buttonName: String) : TurnResult {
        if (canChangeWhosTurnItIs) canChangeWhosTurnItIs = false
        val currentPhase = findCurrentPhase()

        val tilePos = getTileRowAndColByButtonName(buttonName)
        val tile = gameBoard[tilePos[0]][tilePos[1]]
        val token = if (playerOnesTurn) playerOneLabel else playerTwoLabel

        val turnResult : TurnResult
        when (currentPhase) {
            Phase.PLACEMENT -> {
                if (canPlaceTokenOnTile(tile, token)) {
                    if (playerOnesTurn) {
                        playerOneTokensNotPlaced--
                        playerOneTokensPlaced++
                    } else {
                        playerTwoTokensNotPlaced--
                        playerTwoTokensPlaced++
                    }
                    tile.label = token
                    turnResult = TurnResult(currentPhase, true, playerOnesTurn, null)
                    playerOnesTurn = !playerOnesTurn
                    Log.d("TAG", "$token PLACED ON $tile")
                } else {
                    Log.d("TAG", "$token CANNOT BE PLACED ON $tile")
                    turnResult = TurnResult(currentPhase, false, playerOnesTurn, null)
                }
            }

            Phase.CHOOSING -> {
                if (tile.label == token) {
                    Log.d("TAG", "TILE $tile HAS BEEN CHOSEN")
                    currentlyChosenTile = tile
                    turnResult =  TurnResult(currentPhase, true, playerOnesTurn, null)
                } else {
                    Log.d(
                        "TAG",
                        "TILE $tile CANNOT BE CHOSEN BY PLAYER WHOSE TOKEN IS $token"
                    )
                    turnResult =  TurnResult(currentPhase, false, playerOnesTurn, null)

                }
            }

            Phase.MOVING -> {
                if (tile.label.isEmpty()) {

                    currentlyChosenTile!!.label = ""
                    var adjHor = countAdjacentHorizontal(tile, token)
                    var adjVer = countAdjacentVertical(tile, token)

                    if (adjHor < 3 && adjVer < 3) {
                        if (!canJumpFromTo(currentlyChosenTile!!, tile)) {
                            Log.d("TAG", "TRYING TO MOVE FROM " + currentlyChosenTile + " TO " + tile +
                                    " -- CAN ONLY MOVE TO ADJACENT TILES")
                            currentlyChosenTile!!.label = token
                            turnResult = TurnResult(currentPhase, false, playerOnesTurn, currentlyChosenTile)
                            currentlyChosenTile = null

                        } else {
                            Log.d("TAG", "MOVED FROM $currentlyChosenTile TO $tile")

                            turnResult =
                                TurnResult(currentPhase, true, playerOnesTurn, currentlyChosenTile)
                            tile.label = token
                            currentlyChosenTile = null

                            if (adjHor == 2 || adjVer == 2) {
                                hadACombo = true
                                Log.d("TAG", "COMBO!!!")
                            } else {
                                playerOnesTurn = !playerOnesTurn
                                Log.d("TAG", "NOT A COMBO")
                            }
                        }

                    } else {
                        currentlyChosenTile!!.label = token
                        turnResult =  TurnResult(currentPhase, false, playerOnesTurn, currentlyChosenTile)
                        Log.d("TAG", "4 IN A ROW -- CAN'T MOVE FROM $currentlyChosenTile TO $tile")
                        currentlyChosenTile = null
                    }
                } else {
                    turnResult =  TurnResult(currentPhase, false, playerOnesTurn, currentlyChosenTile)
                    Log.d("TAG",
                        "NOT AN EMPTY TILE --  CAN'T MOVE FROM $currentlyChosenTile TO $tile"
                    )
                    currentlyChosenTile = null
                }
            }

            Phase.REMOVING -> {
                if (tile.label.isNotEmpty() && tile.label != token) {
                    Log.d("TAG", "REMOVING TOKEN FROM $tile")
                    turnResult =  TurnResult(currentPhase, true, playerOnesTurn, null)
                    tile.label = ""
                    if (playerOnesTurn) playerOneScore++ else playerTwoScore++
                    playerOnesTurn = !playerOnesTurn
                    hadACombo = false
                } else {
                    turnResult =  TurnResult(currentPhase, false, playerOnesTurn, null)
                    Log.d("TAG", "CAN'T REMOVE TOKEN FROM $tile")
                }
            }

            Phase.GAME_OVER -> {
                turnResult =  TurnResult(currentPhase, true, playerOnesTurn, null)
                Log.d("TAG", "GAME OVER!")
            }
        }


        turnResult.clickedTile = tile
        return turnResult
    }

    private fun getTileRowAndColByButtonName(buttonName: String) : Array<Int> {
        val split = buttonName.split("_").toTypedArray()
        val row = split[1].takeLast(1).toInt()
        val col = split[2].takeLast(1).toInt()
        return arrayOf(row, col)
    }

    private fun getButtonNameByTile(tile : Tile) : String {
        return String.format("button_row%d_col%d", tile.y, tile.x)
    }

    private fun canJumpFromTo(from: Tile, to: Tile) : Boolean {
        return (abs(from.x - to.x) == 1 && from.y == to.y) || (abs(from.y - to.y) == 1 && from.x == to.x)
    }

    private fun allTokensArePlaced() : Boolean {
        return playerOneTokensNotPlaced == 0 && playerTwoTokensNotPlaced == 0
    }

    private fun isGameOver() : Boolean {
        return playerOneScore == 12 || playerTwoScore == 12
    }

    private fun canPlaceTokenOnTile(tile : Tile, token : String) : Boolean {
        return tile.label.isEmpty() && countAdjacentHorizontal(tile, token) < 2 && countAdjacentVertical(tile, token) < 2
    }

    fun countAdjacentHorizontal(tile : Tile, token : String) : Int {
        var adjacentTokens = 0
        // count to right
        if (tile.x < boardWidth - 1) {
            for (x in tile.x + 1 until boardWidth) {
                if (gameBoard[tile.y][x].label == token) {
                    adjacentTokens++
                } else {
                    break
                }
            }
        }
        // count to left
        if (tile.x > 0) {
            for (x in tile.x - 1 downTo 0) {
                if (gameBoard[tile.y][x].label == token) {
                    adjacentTokens++
                } else {
                    break
                }
            }
        }
        return adjacentTokens
    }

    fun countAdjacentVertical(tile : Tile, token : String) : Int {
        var adjacentTokens = 0

        // Count to bottom
        if (tile.y < boardHeight - 1) {
            for (y in tile.y + 1 until boardHeight) {
                if (gameBoard[y][tile.x].label == token) {
                    adjacentTokens++
                } else {
                    break
                }
            }
        }

        // Count to top
        if (tile.y > 0) {
            for (y in tile.y - 1 downTo 0) {
                if (gameBoard[y][tile.x].label == token) {
                    adjacentTokens++
                } else {
                    break
                }
            }
        }
        return adjacentTokens
    }

    fun findCurrentPhase() : Phase {
        if (!allTokensArePlaced()) {
            return Phase.PLACEMENT
        }
        if (hadACombo) {
            return Phase.REMOVING
        }

        if (isGameOver()) {
            return Phase.GAME_OVER
        }

        return if (currentlyChosenTile == null) {
            Phase.CHOOSING
        } else {
            Phase.MOVING
        }
    }

    fun isAiTurn() : Boolean {
        return (playerOnesTurn && !playerOneIsRealPlayer) || (!playerOnesTurn && !playerTwoIsRealPlayer)
    }


    fun startAI() : TurnResult {
        if (playerOnesTurn) {
            return ai1.act()
        } else {
            return ai2.act()
        }
    }

    fun initParams() {
        playerOneTokensNotPlaced = 12
        playerTwoTokensNotPlaced = 12
        playerOneTokensPlaced = 0
        playerTwoTokensPlaced = 0
        playerOneScore = 0
        playerTwoScore = 0
        canChangeWhosTurnItIs = true
        playerOnesTurn = true

        if (timerHasStarted) {
            stopAITimer() // stops previous timer
            activateAITimer()
        }


        currentlyChosenTile = null

        if (playerOnesTurn) {
            ai1 = AI(playerOnesTurn, this)
            ai2 = AI(!playerOnesTurn, this)
        } else {
            ai1 = AI(!playerOnesTurn, this)
            ai2 = AI(playerOnesTurn, this)
        }

    }

    fun reset() {
        for (tileRow in gameBoard) {
            for (tile in tileRow) {
                tile.label = ""
            }
        }
        initParams()
    }
}