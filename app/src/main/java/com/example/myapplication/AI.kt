package com.example.myapplication

import android.util.Log

class AI(val playerOne: Boolean, val gameEngine : GameEngine) {

    var moveToX = -1
    var moveToY = -1

    fun act() : GameEngine.TurnResult {
        var tr : GameEngine.TurnResult? = null
        val p = gameEngine.findCurrentPhase()
        if (p == GameEngine.Phase.PLACEMENT) {
            tr = chooseTileToPlaceTokenOn()
        } else if (p == GameEngine.Phase.CHOOSING) {
            tr = chooseTile()
        } else if (p == GameEngine.Phase.MOVING) {
            tr = gameEngine.makeTurn(gameEngine.gameBoard[moveToY][moveToX])
        } else if (p == GameEngine.Phase.REMOVING) {
            tr = chooseOpponentTileToRemove()
        } else {
            tr = GameEngine.TurnResult(GameEngine.Phase.GAME_OVER, true, playerOne, null)
        }
        return tr
    }

    /**
     * Try placing on the first available tile in a random row -- first try is always [0,0]
     */
    private fun chooseTileToPlaceTokenOn() : GameEngine.TurnResult {
        var resultFromMovingToTile = gameEngine.makeTurn(gameEngine.gameBoard[0][0])

        while (!resultFromMovingToTile.success) {
            val randomRow = (0..4).random()
            for (tile in gameEngine.gameBoard[randomRow]) {
                resultFromMovingToTile = gameEngine.makeTurn(tile)
                if (resultFromMovingToTile.success) break
            }
        }
        return resultFromMovingToTile
    }

    /**
     * A MONSTROSITY. THE METHOD TO RULE THEM ALL. BE AFRAID.
     * Choose a tile from where a move is possible -- prioritize a combo move
     */
    fun chooseTile() : GameEngine.TurnResult {
        var myToken = if (playerOne) "X" else "O"
        var chosenTile : GameEngine.Tile? = null
        moveToX = 0
        moveToY = 0
        outer@ for (tileRow in gameEngine.gameBoard) {
            for (tile in tileRow) {
                if (tile.label == myToken) {

                    // find adjacent
                    for (i in -1..1) {
                        // find adjacent horizontally
                        val adjX = tile.x+i
                        // only adjacent that are within board's space
                        if (adjX >= 0 && adjX <= gameEngine.boardWidth - 1) {
                            val adjacentTile = gameEngine.gameBoard[tile.y][adjX]
                            // we care only about empty adjacent
                            if (adjacentTile.label == "") {
                                // imitate going to that tile from where you are for adjacent counting
                                adjacentTile.label = myToken
                                tile.label = ""
                                // count adjacent of the tile that we imitated stepping on
                                val adjHor = gameEngine.countAdjacentHorizontal(adjacentTile, myToken)
                                val adjVer = gameEngine.countAdjacentVertical(adjacentTile, myToken)

                                // reverse the imitation, no longer needed since we got adjHor and adjVer
                                adjacentTile.label = ""
                                tile.label = myToken
                                // check if combo
                                if ((adjHor == 2 && adjVer <= 2) || (adjHor <= 2 && adjVer == 2)) {
                                    chosenTile = tile
                                    // For the MOVING phase , after we'll be actually moving to that place. Currently we're at CHOOSING phase.
                                    moveToX = adjX
                                    moveToY = tile.y
                                    // we're done after finding a combo, that's our best move
                                    break@outer
                                } else if (adjHor <= 2 && adjVer <= 2) { // check if not combo but a valid move
                                    chosenTile = tile
                                    moveToX = adjX
                                    moveToY = tile.y
                                    // we do not break here because we might fight a combo further on
                                }
                            }
                        }

                        val adjY = tile.y+i
                        // only adjacent that are within board's space
                        if (adjY >= 0 && adjY <= gameEngine.boardHeight - 1) {
                            val adjacentTile = gameEngine.gameBoard[adjY][tile.x]
                            // we care only about empty adjacent
                            if (adjacentTile.label == "") {
                                // imitate going to that tile from where you are for adjacent counting
                                adjacentTile.label = myToken
                                tile.label = ""
                                // count adjacent of the tile that we imitated stepping on
                                val adjHor = gameEngine.countAdjacentHorizontal(adjacentTile, myToken)
                                val adjVer = gameEngine.countAdjacentVertical(adjacentTile, myToken)

                                // reverse the imitation, no longer needed since we got adjHor and adjVer
                                adjacentTile.label = ""
                                tile.label = myToken
                                // check if combo
                                if ((adjHor == 2 && adjVer <= 2) || (adjHor <= 2 && adjVer == 2)) {
                                    chosenTile = tile
                                    // For the MOVING phase , after we'll be actually moving to that place. Currently we're at CHOOSING phase.
                                    moveToX = tile.x
                                    moveToY = adjY
                                    // we're done after finding a combo, that's our best move
                                    break@outer
                                } else if (adjHor <= 2 && adjVer <= 2) { // check if not combo but a valid move
                                    chosenTile = tile
                                    moveToX = tile.x
                                    moveToY = adjY
                                    // we do not break here because we might fight a combo further on
                                }
                            }
                        }
                    }
                }
            }
        }
        if (chosenTile == null) return GameEngine.TurnResult(GameEngine.Phase.GAME_OVER, true, playerOne, null)
        else return gameEngine.makeTurn(chosenTile)
    }

    private fun chooseOpponentTileToRemove() : GameEngine.TurnResult {
        val opponentToken = if (playerOne) "O" else "X"
        var result : GameEngine.TurnResult? = null
        outer@for (tileRow in gameEngine.gameBoard) {
            for (tile in tileRow) {
                if (tile.label == opponentToken) {
                    result = gameEngine.makeTurn(tile)
                    break@outer

                }
            }
        }
        return result!!
    }

}