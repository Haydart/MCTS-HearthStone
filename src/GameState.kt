import models.Player

data class GameState(val player1: Player, val player2: Player, var activePlayer: Player, var turnNumber: Int) {

    fun getOpponent(activePlayer: Player) = if (activePlayer == player1) player2 else player1

    fun deepCopy(): GameState {
        val player1Copy = player1.deepCopy()
        val player2Copy = player2.deepCopy()

        val activePlayerCopy = if (activePlayer == player1) player1Copy else player2Copy

        return GameState(player1Copy, player2Copy, activePlayerCopy, turnNumber)
    }

    override fun toString() = "$player1\n$player2\nactive player = ${activePlayer.name}"
}