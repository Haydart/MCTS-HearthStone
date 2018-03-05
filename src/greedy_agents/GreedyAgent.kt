package greedy_agents

import GameState
import actions.Action
import actions.EndTurn
import pop

/**
 * Created by r.makowiecki on 03/03/2018.
 */
abstract class GreedyAgent {

    abstract fun performTurn(globalGameStateAfterCardDrawing: GameState): List<Action>

    protected fun generateTurnTransitionalStates(
            currentGameState: GameState,
            actionsPerEndState: MutableMap<GameState, List<Action>>,
            actionsSoFar: MutableList<Action> = mutableListOf()
    ) {

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
                    generateTurnTransitionalStates(currentGameState, actionsPerEndState, actionsSoFar)
                    it.rollback(currentGameState)
                }

                actionsSoFar.pop()
            }
        }
    }
}