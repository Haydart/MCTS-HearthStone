package greedy_agents

import GameState
import actions.Action

/**
 * Created by r.makowiecki on 03/03/2018.
 */

private const val OWN_HP_WEIGHT = 0.1f
private const val ENEMY_HP_WEIGHT = -10
private const val ADHERENT_COUNT_WEIGHT = 2
private const val ATTACK_SUM_WEIGHT = 2
private const val HAND_SIZE_WEIGHT = 1
private const val ADHERENT_HP_SUM_WEIGHT = 1

class AggressiveGreedyAgent : Agent() {

    override fun performTurn(globalGameStateAfterCardDrawing: GameState): List<Action> {
        val gameStateActionsListMap = mutableMapOf<GameState, List<Action>>()
        generateTurnTransitionalStates(globalGameStateAfterCardDrawing, gameStateActionsListMap)
        var bestEvaluationSoFar = -Float.MAX_VALUE //Float min val is positive
        lateinit var movesToPerform: List<Action>

        gameStateActionsListMap.forEach { gameState, actionsList ->
            val evaluation = evaluateGameState(gameState)

            if (evaluation > bestEvaluationSoFar) {
                bestEvaluationSoFar = evaluation
                movesToPerform = actionsList
            }
        }

        println("Moves to perform: $movesToPerform")

        movesToPerform.forEach { it.resolve(globalGameStateAfterCardDrawing) }
        return movesToPerform
    }

    private fun evaluateGameState(gameState: GameState): Float {
        return with(gameState) {
            val enemyPlayer = getOpponent(activePlayer)
            activePlayer.healthPoints * OWN_HP_WEIGHT + enemyPlayer.healthPoints * ENEMY_HP_WEIGHT +
                    (activePlayer.tableCards.size - enemyPlayer.tableCards.size) * ADHERENT_COUNT_WEIGHT +
                    (activePlayer.tableCards.sumBy { it.attackStrength } - enemyPlayer.tableCards.sumBy { it.attackStrength }) * ATTACK_SUM_WEIGHT +
                    (activePlayer.handCards.size - enemyPlayer.handCards.size) * HAND_SIZE_WEIGHT +
                    (activePlayer.tableCards.sumBy { it.currentHealthPoints } - enemyPlayer.tableCards.sumBy { it.currentHealthPoints }) * ADHERENT_HP_SUM_WEIGHT
        }
    }
}