package mcts_agent

import GameState
import actions.Action
import actions.EndTurn
import drawCardOrGetPunished
import gameEndConditionsMet
import gametree.GameTree
import gametree.Node
import generateCardDrawPossibleStates
import greedy_agents.Agent
import models.getRandomElement
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Created by r.makowiecki on 07/03/2018.
 */

const val TURN_TIME_MILLIS = 7500L
const val MAGIC_C = 0.5f

private const val HP_WEIGHT = 1f
private const val OWN_ADHERENT_COUNT_WEIGHT = 2
private const val ENEMY_ADHERENT_COUNT_WEIGHT = 20
private const val ATTACK_SUM_WEIGHT = 3
private const val HAND_SIZE_WEIGHT = 1
private const val ADHERENT_HP_SUM_WEIGHT = 2

class ProbabilisticAgent(private val gameTree: GameTree) : Agent() {

    override fun performTurn(globalGameStateAfterCardDrawing: GameState): List<Action> {
        val currentNode = gameTree.rootNode
        val startTime = System.currentTimeMillis()

        val state = currentNode.gameState;
        val avActions = state.activePlayer.getAvailableActions(state.getOpponent(state.activePlayer))
        avActions.forEach {
            println(it)
        }

        val playoutsSoFar = gameTree.rootNode.gamesPlayed
        println("Playouts so far: $playoutsSoFar")
        while (System.currentTimeMillis() < startTime + TURN_TIME_MILLIS) {
            val promisingChild = selectPromisingChild(gameTree.rootNode)
            val simulationResult = simulate(promisingChild)
            backPropagate(promisingChild, simulationResult)
        }

        val playoutsAfterTurn = gameTree.rootNode.gamesPlayed
        println("Playouts after this turn: $playoutsAfterTurn (delta = ${playoutsAfterTurn - playoutsSoFar})")
//        println("Childs:")
//        gameTree.rootNode.childNodes.forEach {
//            println(it.getNodeInfo())
//        }

        val bestChildren = findBestChild(currentNode, 0f)
        println("BestChildren:")
        println(bestChildren.getNodeInfo())
        gameTree.updateRoot(bestChildren)

        return emptyList()
    }

    private fun selectPromisingChild(parentNode: Node): Node {
        var promisingChild = parentNode

        while (!gameEndConditionsMet(promisingChild.gameState)) {
            if (isNodeFullyExpanded(promisingChild)) {
                promisingChild = findBestChild(promisingChild, MAGIC_C)
            } else {
                return expand(promisingChild)
            }
        }

        return promisingChild
    }

    private fun isNodeFullyExpanded(parentNode: Node): Boolean {
        if (parentNode.childNodes.isEmpty()) {
            parentNode.childNodes = generateCardDrawPossibleStates(parentNode, parentNode.gameState)
        }

        parentNode.childNodes.forEach {
            if (it.gamesPlayed <= 0) {
                return false
            }
        }
        return true
    }

    private fun findBestChild(parentNode: Node, c_param: Float): Node {
        return parentNode.childNodes.maxBy {
            getWinsForActivePlayer(it) / it.gamesPlayed + c_param * sqrt(2 * ln(parentNode.gamesPlayed.toFloat()) / it.gamesPlayed)
        }!!
    }

    private fun getWinsForActivePlayer(parentNode: Node) =
            if (gameTree.rootNode.gameState.activePlayer == gameTree.rootNode.gameState.player1)
                parentNode.gamesWon.first
            else parentNode.gamesWon.second

    private fun expand(parentNode: Node): Node {
        val randomUntriedNode = parentNode.childNodes.filter {
            it.gamesPlayed <= 0
        }.getRandomElement()

        return randomUntriedNode
    }

    private fun backPropagate(finalNode: Node, gameResult: GameResult) {
        val player1Won = gameResult.player1Won
        finalNode.updateGamesWon(player1Won)
    }

    private fun simulate(node: Node): GameResult {
        with(node.gameState.deepCopy()) {
            while (!gameEndConditionsMet(this)) {
                drawCardOrGetPunished(activePlayer)
                if (!gameEndConditionsMet(this)) {
                    simulateTurn(this)
                } else break
            }

            return GameResult(player1.healthPoints > player2.healthPoints)
        }
    }

    private fun simulateTurn(gameState: GameState) {
        val enemyPlayer = gameState.getOpponent(gameState.activePlayer)

        var playerEndedTurn = false

        while (!playerEndedTurn) {
            val actions = gameState.activePlayer.getAvailableActions(enemyPlayer)

            val chosenAction = getBestMove(actions, gameState)

            chosenAction.resolve(gameState)

            if (chosenAction is EndTurn) {
                playerEndedTurn = true
            }
        }
    }

    private fun getBestMove(actions: List<Action>, gameState: GameState): Action {
        var bestAction = actions[0]
        var bestEvaluation = -Float.MIN_VALUE

        actions.forEach { currentAction ->
            currentAction.resolve(gameState)
            val currentActionEvaluation = evaluateGameState(gameState)
            currentAction.rollback(gameState)

            if (currentActionEvaluation > bestEvaluation) {
                bestEvaluation = currentActionEvaluation
                bestAction = currentAction
            }
        }

        return bestAction
    }

    private fun evaluateGameState(gameState: GameState): Float {
        return with(gameState) {
            val enemyPlayer = getOpponent(activePlayer)
            (activePlayer.healthPoints - enemyPlayer.healthPoints) * HP_WEIGHT +
                    activePlayer.tableCards.size * OWN_ADHERENT_COUNT_WEIGHT - enemyPlayer.tableCards.size * ENEMY_ADHERENT_COUNT_WEIGHT +
                    (activePlayer.tableCards.sumBy { it.attackStrength } - enemyPlayer.tableCards.sumBy { it.attackStrength }) * ATTACK_SUM_WEIGHT +
                    (activePlayer.handCards.size - enemyPlayer.handCards.size) * HAND_SIZE_WEIGHT +
                    (activePlayer.tableCards.sumBy { it.currentHealthPoints } - enemyPlayer.tableCards.sumBy { it.currentHealthPoints }) * ADHERENT_HP_SUM_WEIGHT
        }
    }
}

class GameResult(val player1Won: Boolean)