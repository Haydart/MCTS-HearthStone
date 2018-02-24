import models.Player

data class GameState(val player1: Player, val player2: Player, var activePlayer: Player, var turnNumber: Int) {

    fun getOpponent(activePlayer: Player) = if (activePlayer == player1) player2 else player1
}