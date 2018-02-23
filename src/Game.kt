import models.Player
import java.util.function.BinaryOperator.maxBy
import java.util.stream.Collectors.maxBy

class Game(val player1: Player, val player2: Player) {

    fun run() {

        var activePlayer = player1

        while (!gameEndConditionsMet()) {
            performTurn(player1, player2)
            activePlayer = if (activePlayer == player1) player2 else player1
        }

        val winningPlayer = if(player1.healthPoints < player2.healthPoints) player2 else player1
    }

    private fun performTurn(currentPlayer: Player, enemyPlayer: Player) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun gameEndConditionsMet() = player1.healthPoints <= 0 || player2.healthPoints <= 0
}