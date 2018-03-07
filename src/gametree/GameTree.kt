package gametree

import GameState

/**
 * Created by r.makowiecki on 28/02/2018.
 */
typealias Player1Wins = Float
typealias Player2Wins = Float

class GameTree(initialRootNode: Node) {
    var rootNode: Node = initialRootNode
}

open class Node(
        val gameState: GameState,
        var childNodes: List<Node>,
        val parentNode: Node? = null,
        val gamesPlayed: Int = 0,
        val gamesWon: Pair<Player1Wins, Player2Wins> = Pair(0f, 0f)
) {
    override fun toString() = "gamestate = ${gameState.player1} childNodes = \n\t\t\t${childNodes}"
}

class CardDrawingNode(
        val nodeOccurenceProbability: Float,
        gameState: GameState,
        childNodes: List<Node>,
        parentNode: Node? = null,
        gamesPlayed: Int = 0,
        gamesWon: Pair<Player1Wins, Player2Wins> = Pair(0f, 0f)
) : Node(gameState, childNodes, parentNode, gamesPlayed, gamesWon)
