import actions.EndTurn
import gametree.CardDrawingNode
import gametree.GameTree
import gametree.Node
import greedy_agents.ControllingGreedyAgent
import greedy_agents.RandomGreedyAgent
import models.*
import java.util.*
import kotlin.math.ln
import kotlin.math.sqrt

const val TURN_TIME_MILLIS = 5000L
const val PUNISHMENT_VALUE = 2
const val MAGIC_C = 1

class Game(var gameState: GameState) {

    private val initialRootNode = Node(
            gameState,
            listOf(),
            null
    )

    private val gameTree = GameTree(initialRootNode)

    private val randomAgent = RandomGreedyAgent()
    private val greedyAgent = ControllingGreedyAgent()

    init {
        (0 until 3).forEach { gameState.player1.takeCardFromDeck() }
        (0 until 4).forEach { gameState.player2.takeCardFromDeck() }

        initialRootNode.childNodes = generateCardDrawPossibleStates(initialRootNode)
    }

    private fun generateCardDrawPossibleStates(parentNode: Node? = null): List<Node> {
        val possibleEndStateNodes = mutableListOf<Node>()
        val cardToDrawProbability = mutableMapOf<Card, Float>()

        gameState.activePlayer.deckCards.forEach {
            val cardDrawProbability = gameState.activePlayer.deckCards.count { iterCard ->
                iterCard.name == it.name
            } / gameState.activePlayer.deckCards.size.toFloat()
            cardToDrawProbability[it] = cardDrawProbability
        }

        cardToDrawProbability.forEach { card, probability ->
            val index = gameState.activePlayer.takeCardFromDeck(card)
            val gameStateAfterDraw = gameState.deepCopy()
            gameState.activePlayer.returnCardToDeck(card, index)

            val drawNode = CardDrawingNode(probability, gameStateAfterDraw, listOf(), parentNode)
            val drawNodeChildren = generatePossibleEndTurnGameStates(drawNode, gameStateAfterDraw)
            drawNode.childNodes = drawNodeChildren

            possibleEndStateNodes.add(drawNode)
        }

        if (cardToDrawProbability.isEmpty()) {
            val punishmentNode = CardDrawingNode(1f, gameState.deepCopy(), listOf(), parentNode)
            punishPlayerWithEmptyDeck(punishmentNode.gameState.activePlayer)
            val punishmentNodeChildren = generatePossibleEndTurnGameStates(punishmentNode, punishmentNode.gameState)
            punishmentNode.childNodes = punishmentNodeChildren
            possibleEndStateNodes.add(punishmentNode)
        }

        return possibleEndStateNodes
    }

    private fun generatePossibleEndTurnGameStates(parentNode: Node? = null, stateAfterCardDraw: GameState): MutableList<Node> {
        val endStatesList = LinkedList<GameState>()
        generateTurnTransitionalStates(endStatesList, stateAfterCardDraw)
        return endStatesList.map {
            Node(it, LinkedList(), parentNode)
        }.toMutableList()
    }

    private fun generateTurnTransitionalStates(leafStatesList: MutableList<GameState>, currentGameState: GameState) {
        with(currentGameState) {
            activePlayer.getAvailableActions(getOpponent(activePlayer)).forEach {
                if (it is EndTurn) {
                    it.resolve(currentGameState)
                    leafStatesList.add(currentGameState.deepCopy())
                    it.rollback(currentGameState)
                } else {
                    it.resolve(currentGameState)
                    generateTurnTransitionalStates(leafStatesList, currentGameState)
                    it.rollback(currentGameState)
                }
            }
        }
    }

    private fun simulate(node: Node): GameResult {
        with(node.gameState) {
            while (!gameEndConditionsMet()) {
                drawCardOrGetPunished(activePlayer)
                if (!gameEndConditionsMet()) {
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

    fun run() {
        with(gameState) {
            while (!gameEndConditionsMet()) {
                performTurn(activePlayer)

                println("Game state after turn: ")
                println(gameState)
                println("______________________________")
            }

            val winningPlayer = if (player1.healthPoints < player2.healthPoints) player2 else player1
            println("Game end, the winning player is \n$winningPlayer")
        }
    }

    private fun gameEndConditionsMet(gameState: GameState = this.gameState): Boolean {
        return gameState.player1.healthPoints <= 0 || gameState.player2.healthPoints <= 0
    }

    private fun performTurn(currentPlayer: Player) {
        drawCardOrGetPunished(currentPlayer)

        if (currentPlayer == gameState.player1) {
            randomAgent.performTurn(gameState)
        } else {
            greedyAgent.performTurn(gameState)
        }
    }

    private fun mctsLoop() {
        val currentNode = gameTree.rootNode
        val startTime = System.currentTimeMillis()


        while (System.currentTimeMillis() < startTime + TURN_TIME_MILLIS) {

            val promisinghild = selectPromisingChild(gameTree.rootNode)

        }
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

    private fun findBestChild(parentNode: Node) = parentNode.childNodes.maxBy {
        getWinsForActivePlayer(parentNode) / it.gamesPlayed + MAGIC_C * sqrt(2 * ln(parentNode.gamesPlayed.toFloat()) / it.gamesPlayed)
    }!!

    private fun getWinsForActivePlayer(parentNode: Node) =
            if (gameTree.rootNode.gameState.activePlayer == gameTree.rootNode.gameState.player1)
                parentNode.gamesWon.first
            else parentNode.gamesWon.second

    private fun expand(parentNode: Node): Node {
        return parentNode
    }

    fun drawCardOrGetPunished(currentPlayer: Player) {
        if (currentPlayer.deckCards.size > 0) {
            currentPlayer.takeCardFromDeck()
        } else {
            punishPlayerWithEmptyDeck(currentPlayer)
        }
    }

    private fun punishPlayerWithEmptyDeck(player: Player) {
        player.turnsWithDeckCardsDepleted++
        player.healthPoints -= player.turnsWithDeckCardsDepleted * PUNISHMENT_VALUE
    }

    private fun revertPlayerPunish(player: Player) {
        player.healthPoints += player.turnsWithDeckCardsDepleted * PUNISHMENT_VALUE
        player.turnsWithDeckCardsDepleted--
    }

    private fun Player.useRandomCard() {
        val drawnCard = handCards.popRandomElement()

        if (drawnCard is AdherentCard) {
            tableCards.add(drawnCard)
        } else if (drawnCard is SpellCard) {
//            drawnCard.applyEffect(activePlayer, getOpponent(activePlayer))
        } else throw IllegalStateException("The drawn card is neither Adherent nor Spell.")
    }
}

class GameResult(val winningPlayer: Player)

fun <E> MutableList<E>.push(element: E) {
    add(element)
}

fun <E> MutableList<E>.pop(): E {
    val lastItem = last()
    remove(lastItem)
    return lastItem
}

fun <E> MutableList<E>.removeExact(element: E): Boolean {
    return removeIf {
        it === element
    }
}

fun <E> MutableList<E>.indexOfExact(element: E): Int {
    return indexOfFirst {
        it === element
    }
}

fun <E> MutableList<E>.containsExact(element: E): Boolean {
    return any {
        it === element
    }
}