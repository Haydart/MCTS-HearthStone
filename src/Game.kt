import models.Player

class Game(var gameState: GameState) {

    init {
        (0 until 4).forEach { gameState.player1.takeCardFromDeck() }
        (0 until 3).forEach { gameState.player2.takeCardFromDeck() }
    }

    fun run() {
        with(gameState) {
            var activePlayer = player1

            while (!gameEndConditionsMet()) {
                performTurn(player1, player2)
                activePlayer = if (activePlayer == player1) player2 else player1
            }

            val winningPlayer = if (player1.healthPoints < player2.healthPoints) player2 else player1
        }
    }

    private fun performTurn(currentPlayer: Player, enemyPlayer: Player) {
        currentPlayer.takeCardFromDeck()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun gameEndConditionsMet() = gameState.player1.healthPoints <= 0 || gameState.player2.healthPoints <= 0
}

data class GameState(val player1: Player, val player2: Player, val turnNumber: Int)