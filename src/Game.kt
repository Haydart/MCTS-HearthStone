import actions.EndTurn
import gametree.CardDrawingNode
import gametree.GameTree
import gametree.Node
import greedy_agents.ControllingGreedyAgent
import greedy_agents.RandomGreedyAgent
import models.*
import java.util.*

const val PUNISHMENT_VALUE = 2

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

    fun run() {
        with(gameState) {
            while (!gameEndConditionsMet(gameState)) {
                performTurn(activePlayer)

                println("Game state after turn: ")
                println(gameState)
                println("______________________________")
            }

            val winningPlayer = if (player1.healthPoints < player2.healthPoints) player2 else player1
            println("Game end, the winning player is \n$winningPlayer")
        }
        println(gameTree.rootNode)
    }

    private fun performTurn(currentPlayer: Player) {
        drawCardOrGetPunished(currentPlayer)

        if (currentPlayer == gameState.player1) {
            randomAgent.performTurn(gameState)
        } else {
            greedyAgent.performTurn(gameState)
        }
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

fun gameEndConditionsMet(gameState: GameState) =
        gameState.player1.healthPoints <= 0 || gameState.player2.healthPoints <= 0