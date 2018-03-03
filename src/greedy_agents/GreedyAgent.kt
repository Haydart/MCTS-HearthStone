package greedy_agents

import GameState
import actions.Action
import actions.EndTurn
import pop

/**
 * Created by r.makowiecki on 03/03/2018.
 */
abstract class GreedyAgent {
    abstract fun performTurn(gameStateAfterCardDrawing: GameState)

    abstract fun evaluateGameState(gameState: GameState): Float

    protected fun generateTurnTransitionalStates(currentGameState: GameState, actionsSoFar: MutableList<Action>, actionsPerEndState: MutableMap<GameState, List<Action>>) {

        with(currentGameState) {

            activePlayer.getAvailableActions(getOpponent(activePlayer)).forEach {

                actionsSoFar.add(it)

                if (it is EndTurn) {
                    it.resolve(currentGameState)
                    val actionsToEnd = mutableListOf<Action>()
                    actionsSoFar.forEach {
                        actionsToEnd.add(it)
                    }
                    actionsPerEndState.put(currentGameState.deepCopy().apply { activePlayer = getOpponent(activePlayer) }, actionsToEnd)
                    it.rollback(currentGameState)
                } else {
                    it.resolve(currentGameState)
                    generateTurnTransitionalStates(currentGameState, actionsSoFar, actionsPerEndState)
                    it.rollback(currentGameState)
                }

                actionsSoFar.pop()
            }

        }
    }
}