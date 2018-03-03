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

class AggressiveGreedyAgent : GreedyAgent() {

    override fun performTurn(gameStateAfterCardDrawing: GameState) {
        val gameStateActionsListMap = mutableMapOf<GameState, List<Action>>()
        generateTurnTransitionalStates(gameStateAfterCardDrawing, mutableListOf(), gameStateActionsListMap)
        var bestEvaluationSoFar = Float.MIN_VALUE
        lateinit var movesToPerform: Pair<GameState, List<Action>>

        gameStateActionsListMap.forEach { gameState, actionsList ->
            println(gameState)
            println("\t\t$actionsList")

            val evaluation = evaluateGameState(gameState)
            println("this state was evaluated at $evaluation points")
            println("")

            if (evaluation > bestEvaluationSoFar) {
                bestEvaluationSoFar = evaluation
                movesToPerform = gameState to actionsList
            }
        }

        //todo make the moves
    }

    fun evaluateGameState(gameState: GameState): Float {
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