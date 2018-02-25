import models.AdherentCard
import models.Player
import models.SpellCard
import models.takeRandomElement
import java.util.*

class Game(var gameState: GameState) {

//    val gameStatesStack = mutableListOf<GameState>()

    init {
        (0 until 3).forEach { gameState.player1.takeCardFromDeck() }
        (0 until 4).forEach { gameState.player2.takeCardFromDeck() }
    }

    fun run() {
        with(gameState) {

            while (!gameEndConditionsMet()) {
                gameState.turnNumber++
                performTurn(activePlayer, getOpponent(activePlayer))
                activePlayer = getOpponent(activePlayer)
            }

            val winningPlayer = if (player1.healthPoints < player2.healthPoints) player2 else player1
            println("Game end, the winning player is ${if (winningPlayer == player1) "player1" else "player2"}")
        }
    }

    private fun gameEndConditionsMet() = gameState.player1.healthPoints <= 0 || gameState.player2.healthPoints <= 0

    private fun performTurn(currentPlayer: Player, enemyPlayer: Player) {
        gameState.activePlayer.mana = (gameState.turnNumber - 1) / 2 + 1
        drawCardOrGetPunished(currentPlayer)
        println(gameState.activePlayer)
        if (gameEndConditionsMet()) return

        val availableActions = currentPlayer.getAvailableActions(enemyPlayer)
        println("My available actions $availableActions")

        if (!availableActions.isEmpty()) {
            val randomAvailableAction = availableActions[Random().nextInt(availableActions.size)]
            println("I'm about to play: ${randomAvailableAction.triggeringCard}")
            randomAvailableAction.resolve(gameState)
        }

        println("I have now ${currentPlayer.handCards.size} cards in hand.")

        println("I have ${gameState.activePlayer.healthPoints} HP")
        println("------ Turn ended ------")
    }

    private fun drawCardOrGetPunished(currentPlayer: Player) {
        if (currentPlayer.deckCards.size > 0) {
            currentPlayer.takeCardFromDeck()
        } else {
            currentPlayer.turnsWithDeckCardsDepleted++
            punishPlayerWithEmptyDeck(currentPlayer)
        }
    }

    private fun punishPlayerWithEmptyDeck(player: Player) {
        player.healthPoints -= player.turnsWithDeckCardsDepleted * 2
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