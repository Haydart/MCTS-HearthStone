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
            val randomAction = gameState.activePlayer.getAvailableActions(enemyPlayer).getRandomElement()
            randomAction.resolve(gameState)

            if (randomAction is EndTurn) {
                playerEndedTurn = true
            }
        }
    }
}

class GameResult(val player1Won: Boolean)