package greedy_agents

import GameState
import actions.Action
import java.util.*


/**
 * Created by r.makowiecki on 03/03/2018.
 */
class RandomGreedyAgent : GreedyAgent() {

    override fun performTurn(gameStateAfterCardDrawing: GameState) {
        val gameStateToActionsListMap = mutableMapOf<GameState, List<Action>>()
        generateTurnTransitionalStates(gameStateAfterCardDrawing, mutableListOf(), gameStateToActionsListMap)

        val keyList = gameStateToActionsListMap.keys.toList()
        Collections.shuffle(keyList)
        val movesToPerform = gameStateToActionsListMap[keyList[0]]

        println("Moves to perform: $movesToPerform")
    }
}