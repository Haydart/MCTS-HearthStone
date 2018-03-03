package greedy_agents

import GameState

/**
 * Created by r.makowiecki on 03/03/2018.
 */
class RandomGreedyAgent : GreedyAgent() {

    override fun performTurn(gameStateAfterCardDrawing: GameState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun evaluateGameState(gameState: GameState): Float = Float.MIN_VALUE
}