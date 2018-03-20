package greedyagents

import GameState
import actions.Action
import java.util.*


/**
 * Created by r.makowiecki on 03/03/2018.
 */
class RandomGreedyAgent : Agent() {

    override fun performTurn(globalGameStateAfterCardDrawing: GameState): List<Action> {
        val gameStateToActionsListMap = mutableMapOf<GameState, List<Action>>()
        generateTurnTransitionalStates(globalGameStateAfterCardDrawing, gameStateToActionsListMap)

        val keyList = gameStateToActionsListMap.keys.toList()
        Collections.shuffle(keyList)
        val movesToPerform = gameStateToActionsListMap[keyList[0]]!!

//        println("Moves to perform: $movesToPerform")

        movesToPerform.forEach { it.resolve(globalGameStateAfterCardDrawing) }
        return movesToPerform
    }
}