import actions.EndTurn
import gametree.CardDrawingNode
import gametree.GameTree
import gametree.Node
import models.*
import java.util.*

const val TURN_TIME_MILLIS = 5000L
const val PUNISH_VALUE = 2

class Game(var gameState: GameState) {

    private val initialRootNode = Node(
            gameState,
            listOf(),
            null
    )

    private val gameTree = GameTree(initialRootNode)

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
            val ind = gameState.activePlayer.takeCardFromDeck(card)
            val gameStateAfterDraw = gameState.deepCopy()
            gameState.activePlayer.returnCardToDeck(card, ind)
            val drawNode = CardDrawingNode(probability, gameStateAfterDraw, listOf(), parentNode)
            val drawNodeChildren = generatePossibleEndTurnGameStates(drawNode, gameStateAfterDraw)
            drawNode.childNodes = drawNodeChildren

            println("card draw prob: $probability")
            possibleEndStateNodes.add(drawNode)
        }

//        if (cardToDrawProbability.isEmpty()) {
//
//            val punishmentNode = CardDrawingNode(1f, gameState.deepCopy(), listOf(), parentNode)
//            punishPlayerWithEmptyDeck(punishmentNode.gameState.activePlayer)
//            val punishmentNodeChildren = generatePossibleEndTurnGameStates(punishmentNode, punishmentNode.gameState)
//            punishmentNode.childNodes = punishmentNodeChildren
//            possibleEndStateNodes.add(punishmentNode)
//        }

        return possibleEndStateNodes
    }


    private fun generatePossibleEndTurnGameStates(parentNode: Node? = null, state: GameState): MutableList<Node> {
        val endStatesList = LinkedList<GameState>()
        generateTurnTransitionalStates(endStatesList, state)
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
        println(gameTree.rootNode)

//        with(gameState) {
//
//            while (!gameEndConditionsMet()) {
//                gameState.turnNumber++
//                performTurn(activePlayer, getOpponent(activePlayer))
//                activePlayer = getOpponent(activePlayer)
//            }
//
//            val winningPlayer = if (player1.healthPoints < player2.healthPoints) player2 else player1
//            println("Game end, the winning player is ${if (winningPlayer == player1) "player1" else "player2"}")
//        }
    }

    private fun gameEndConditionsMet() = gameState.player1.healthPoints <= 0 || gameState.player2.healthPoints <= 0

    private fun performTurn(currentPlayer: Player, enemyPlayer: Player) {
//        drawCardOrGetPunished(currentPlayer)
//        println(gameState.activePlayer)
//        if (gameEndConditionsMet()) return
//
//        val availableActions = currentPlayer.getAvailableActions(enemyPlayer)
//        println("My available actions $availableActions")
//
//        if (!availableActions.isEmpty()) {
//            val randomAvailableAction = availableActions[Random().nextInt(availableActions.size)]
//
//            if (randomAvailableAction is CardAction) {
//                println("I'm about to play: ${randomAvailableAction.triggeringCard}")
//            } else {
//                println("I chose to end my turn")
//            }
//
//            randomAvailableAction.resolve(gameState)
//        }
//
//        println("I have now ${currentPlayer.handCards.size} cards in hand.")
//
//        println("I have ${gameState.activePlayer.healthPoints} HP")
//        println("------ Turn ended ------")

        mctsSearch()
    }

    private fun mctsSearch() {
        val currentNode = gameTree.rootNode
        val startTime = System.currentTimeMillis()


        while (System.currentTimeMillis() < startTime + TURN_TIME_MILLIS) {

            findBestChild(currentNode)

        }
    }

    private fun findBestChild(currentNode: Node) {

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
        player.healthPoints -= player.turnsWithDeckCardsDepleted * PUNISH_VALUE
    }

    private fun revertPlayerPunish(player: Player) {
        player.healthPoints += player.turnsWithDeckCardsDepleted * PUNISH_VALUE
        player.turnsWithDeckCardsDepleted--
    }

    private fun Player.useRandomCard() {
        val drawnCard = handCards.takeRandomElement()

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