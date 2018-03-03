package greedy_agents

import GameState
import actions.Action

/**
 * Created by r.makowiecki on 03/03/2018.
 */
class AggresiveGreedyAgent : GreedyAgent() {

    override fun performTurn(gameStateAfterCardDrawing: GameState) {

        val gameStateToActionsList = mutableMapOf<GameState, List<Action>>()
        generateTurnTransitionalStates(gameStateAfterCardDrawing, mutableListOf(), gameStateToActionsList)

        gameStateToActionsList.forEach { key, values ->
            println(key)
            println("\t\t$values")
            println("")
        }
    }

    override fun evaluateGameState(gameState: GameState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}