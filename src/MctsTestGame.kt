import gametree.GameTree
import gametree.Node
import greedy_agents.Agent
import mcts_agent.ProbabilisticAgent
import models.Player

private const val PUNISHMENT_VALUE = 2

class MctsTestGame(gameState: GameState, enemyAgent: Agent, mctsFirst: Boolean) {

    private val gameTree = GameTree(Node(gameState, listOf(), null))

    private val agent1 = if (mctsFirst) ProbabilisticAgent(gameTree) else enemyAgent
    private val agent2 = if (mctsFirst) enemyAgent else ProbabilisticAgent(gameTree)

    private var player1Controller: Agent? = null
    private var player2Controller: Agent? = null

    init {
        (0 until 3).forEach { gameState.player1.takeCardFromDeck() }
        (0 until 4).forEach { gameState.player2.takeCardFromDeck() }

        gameTree.rootNode.childNodes = generateCardDrawPossibleStates(gameTree.rootNode, gameState)
    }

    private fun getActivePlayerController(gameState: GameState): Agent? {
        return if (gameState.activePlayer === gameState.player1) player1Controller else player2Controller
    }

    private fun getCurrentState(): GameState = gameTree.rootNode.gameState

    fun run() {
        player1Controller = agent1
        player2Controller = agent2

        while (!gameEndConditionsMet(getCurrentState())) {
            performTurn(getCurrentState().activePlayer)
            println(getCurrentState())

            println("______________________________")
        }

        val winningPlayer = if (getCurrentState().player1.healthPoints < getCurrentState().player2.healthPoints) getCurrentState().player2 else getCurrentState().player1
        println("Game end, the winning player is \n$winningPlayer")

        println(getCurrentState())
    }

    private fun performTurn(currentPlayer: Player) {
        if (getActivePlayerController(getCurrentState()) is ProbabilisticAgent) {
            if (gameTree.rootNode.childNodes.isEmpty()) {
                gameTree.rootNode.childNodes = generateCardDrawPossibleStates(gameTree.rootNode, getCurrentState())
            }
        }

        drawCardOrGetPunished(currentPlayer)
        println(gameTree.rootNode.getNodeInfo())

        // resolve tree
        val newRootNode: Node? = gameTree.rootNode.childNodes.find {
            val hC1 = it.gameState.activePlayer.deckCards.sumBy {
                it.hashCode()
            }
            val hC2 = gameTree.rootNode.gameState.activePlayer.deckCards.sumBy {
                it.hashCode()
            }
            hC1 == hC2
        }

        if (newRootNode != null) {
            gameTree.updateRoot(newRootNode)
        }

        val notMCTS: Boolean = (getActivePlayerController(getCurrentState()) !is ProbabilisticAgent)

        getActivePlayerController(getCurrentState())?.performTurn(getCurrentState())

        if (notMCTS) {
            val newRootNode: Node? = gameTree.rootNode.childNodes.find {
                it.gameState == gameTree.rootNode.gameState
            }

            if (newRootNode != null) {
                gameTree.updateRoot(newRootNode)
            }
        }
    }

    private fun drawCardOrGetPunished(currentPlayer: Player) {
        if (currentPlayer.deckCards.size > 0) {
            currentPlayer.takeCardFromDeck()
        } else {
            punishPlayerWithEmptyDeck(currentPlayer)
        }
    }

    private fun punishPlayerWithEmptyDeck(player: Player) {
        player.turnsWithDeckCardsDepleted++
        player.healthPoints -= player.turnsWithDeckCardsDepleted * PUNISHMENT_VALUE
    }
}

private fun punishPlayerWithEmptyDeck(player: Player) {
    player.turnsWithDeckCardsDepleted++
    player.healthPoints -= player.turnsWithDeckCardsDepleted * PUNISHMENT_VALUE
}