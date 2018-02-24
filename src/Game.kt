import models.Card
import models.Player

const val MAX_ADHERENT_CARDS_COUNT = 7

class Game(var gameState: GameState) {

    init {
        (0 until 3).forEach { gameState.player1.takeCardFromDeck() }
        (0 until 4).forEach { gameState.player2.takeCardFromDeck() }
    }

    fun run() {
        with(gameState) {
            var activePlayer = player1

            while (!gameEndConditionsMet()) {
                performTurnIfConditionsMet(player1, player2)
                activePlayer = if (activePlayer == player1) player2 else player1
            }

            val winningPlayer = if (player1.healthPoints < player2.healthPoints) player2 else player1
            println("Game end, the winning player is ${if(winningPlayer == player1) "player1" else "player2"}")
        }
    }

    private fun gameEndConditionsMet() = gameState.player1.healthPoints <= 0 || gameState.player2.healthPoints <= 0

    private fun performTurnIfConditionsMet(currentPlayer: Player, enemyPlayer: Player) {
        if(currentPlayer.deckCards.size > 0) {
            performTurn(currentPlayer, enemyPlayer)
        } else {
            currentPlayer.turnsWithDeckCardsDepleted++
            punishPlayer(currentPlayer)
        }
    }

    private fun performTurn(currentPlayer: Player, enemyPlayer: Player) {
        currentPlayer.takeCardFromDeck()

        if(currentPlayer.tableCards.size < MAX_ADHERENT_CARDS_COUNT) {
            currentPlayer.deployRandomAdherentCard()
        } else {

        }
    }

    private fun punishPlayer(player: Player) {
        player.healthPoints -= player.turnsWithDeckCardsDepleted * 2
    }
}

data class GameState(val player1: Player, val player2: Player, val turnNumber: Int)