package mcts_agent

import GameState
import actions.Action
import drawCardOrGetPunished
import gameEndConditionsMet
import gametree.GameTree
import gametree.Node
import generateCardDrawPossibleStates
import greedy_agents.Agent
import models.Player
import models.getRandomElement
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Created by r.makowiecki on 07/03/2018.
 */

const val TURN_TIME_MILLIS = 3000L
const val MAGIC_C = 1

class ProbabilisticAgent(private val gameTree: GameTree) : Agent() {

    override fun performTurn(globalGameStateAfterCardDrawing: GameState): List<Action> {
        val currentNode = gameTree.rootNode
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() < startTime + TURN_TIME_MILLIS) {
            val promisingChild = selectPromisingChild(gameTree.rootNode)
            val simulationResult = simulate(promisingChild)
            backPropagate(promisingChild, simulationResult)
        }

        gameTree.rootNode = findBestChild(currentNode)

        return emptyList()
    }

    private fun selectPromisingChild(parentNode: Node): Node {
        var promisingChild = parentNode

        while (!gameEndConditionsMet(promisingChild.gameState)) {
            if (isNodeFullyExpanded(promisingChild)) {
                promisingChild = findBestChild(promisingChild)
            } else {
                return expand(promisingChild)
            }
        }

        return promisingChild
    }

    private fun isNodeFullyExpanded(parentNode: Node): Boolean {
        parentNode.childNodes.forEach {
            if (it.gamesPlayed <= 0) {
                return false
            }
        }
        return true
    }

    private fun findBestChild(parentNode: Node): Node {
        return parentNode.childNodes.maxBy {
            getWinsForActivePlayer(parentNode) / it.gamesPlayed + MAGIC_C * sqrt(2 * ln(parentNode.gamesPlayed.toFloat()) / it.gamesPlayed)
        }!!
    }

    private fun getWinsForActivePlayer(parentNode: Node) =
            if (gameTree.rootNode.gameState.activePlayer == gameTree.rootNode.gameState.player1)
                parentNode.gamesWon.first
            else parentNode.gamesWon.second

    private fun expand(parentNode: Node): Node {
        if (parentNode.childNodes.isEmpty()) {
            parentNode.childNodes = generateCardDrawPossibleStates(parentNode, parentNode.gameState)
        }
        val randomUntriedNode = parentNode.childNodes.filter {
            it.gamesPlayed <= 0
        }.getRandomElement()

        return randomUntriedNode
    }

    private fun backPropagate(finalNode: Node, gameResult: GameResult) {
        finalNode.updateGamesWon(gameResult.winningPlayer == finalNode.gameState.player1)
    }

    private fun simulate(node: Node): GameResult {
        with(node.gameState.deepCopy()) {
            while (!gameEndConditionsMet(this)) {
                drawCardOrGetPunished(activePlayer)
                if (!gameEndConditionsMet(this)) {
                    simulateTurn(this)
                } else break
            }

            val winningPlayer = if (player1.healthPoints < player2.healthPoints) player2 else player1
            return GameResult(winningPlayer)
        }
    }

    private fun simulateTurn(gameState: GameState) {
        val enemyPlayer = gameState.getOpponent(gameState.activePlayer)
        gameState.activePlayer.getAvailableActions(enemyPlayer).getRandomElement().resolve(gameState)
    }
}

class GameResult(val winningPlayer: Player)