import models.AdherentCard
import models.Player
import models.SpellCard
import models.takeRandomElement

const val MAX_ADHERENT_CARDS_COUNT = 7

class Game(var gameState: GameState) {


    init {
        (0 until 3).forEach { gameState.player1.takeCardFromDeck() }
        (0 until 4).forEach { gameState.player2.takeCardFromDeck() }
    }

    fun run() {
        with(gameState) {

            while (!gameEndConditionsMet()) {
                performTurnIfConditionsMet(activePlayer, getOpponent(activePlayer))
                activePlayer = getOpponent(activePlayer)
            }

            val winningPlayer = if (player1.healthPoints < player2.healthPoints) player2 else player1
            println("Game end, the winning player is ${if (winningPlayer == player1) "player1" else "player2"}")
        }
    }

    private fun gameEndConditionsMet() = gameState.player1.healthPoints <= 0 || gameState.player2.healthPoints <= 0

    private fun performTurnIfConditionsMet(currentPlayer: Player, enemyPlayer: Player) {
        if (currentPlayer.deckCards.size > 0) {
            performTurn(currentPlayer, enemyPlayer)
        } else {
            currentPlayer.turnsWithDeckCardsDepleted++
            punishPlayer(currentPlayer)
        }
    }

    private fun performTurn(currentPlayer: Player, enemyPlayer: Player) {
        currentPlayer.takeCardFromDeck()

        if (currentPlayer.tableCards.size < MAX_ADHERENT_CARDS_COUNT) {
            currentPlayer.useRandomCard()
        }
    }

    private fun punishPlayer(player: Player) {
        player.healthPoints -= player.turnsWithDeckCardsDepleted * 2
    }

    private fun Player.useRandomCard() {
        val drawnCard = handCards.takeRandomElement()

        if (drawnCard is AdherentCard) {
            tableCards.add(drawnCard)
        } else if (drawnCard is SpellCard) {
            drawnCard.applyEffect(activePlayer, getOpponent(activePlayer))
        } else throw IllegalStateException("The drawn card is neither Adherent nor Spell.")
    }


}