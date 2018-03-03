package gametree

import GameState

/**
 * Created by r.makowiecki on 28/02/2018.
 */
class GameTree(initialRootNode: Node) {
    var rootNode: Node = initialRootNode
}

open class Node(
        val gameState: GameState,
        var childNodes: List<Node>,
        val parentNode: Node? = null,
        val gamesPlayed: Int = 0,
        val gamesWon: Float = 0f
) {
    override fun toString() = "gamestate = ${gameState.activePlayer}\n\t\t\tchildNodes = ${childNodes}"
}

class CardDrawingNode(
        val nodeOccurenceProbability: Float,
        gameState: GameState,
        childNodes: List<Node>,
        parentNode: Node? = null,
        gamesPlayed: Int = 0,
        gamesWon: Float = 0f
) : Node(gameState, childNodes, parentNode, gamesPlayed, gamesWon)
