package gametree

import GameState

/**
 * Created by r.makowiecki on 28/02/2018.
 */
class GameTree(initialRootNode: Node) {
    var rootNode: Node = initialRootNode
}

class Node(
        val gameState: GameState,
        var childNodes: List<Node>,
        val parentNode: Node? = null,
        val gamesPlayed: Int = 0,
        val gamesWon: Int = 0
)
