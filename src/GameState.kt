import models.Player

data class GameState(val player1: Player, val player2: Player, val turnNumber: Int)