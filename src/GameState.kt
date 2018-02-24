import models.Player

data class GameState(val player1: Player, val player2: Player, val turnNumber: Int, var activePlayer: Player){

    fun getOpponent(activePlayer: Player) = if (activePlayer == player1) player2 else player1
}