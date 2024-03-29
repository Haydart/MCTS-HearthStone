import actions.EndTurn
import gametree.CardDrawingNode
import gametree.GameTree
import gametree.Node
import greedy_agents.Agent
import greedy_agents.ControllingGreedyAgent
import mcts_agent.ProbabilisticAgent
import models.*
import java.util.*
import kotlin.collections.HashSet

const val PUNISHMENT_VALUE = 2

class Game(gameState: GameState) {

    private val gameTree = GameTree(Node(gameState, listOf(), null))

    private val randomAgent = ControllingGreedyAgent()
    private val greedyAgent = ProbabilisticAgent(gameTree)

    private var player1Controller: Agent? = null
    private var player2Controller: Agent? = null

    init {
        (0 until 3).forEach { gameState.player1.takeCardFromDeck() }
        (0 until 4).forEach { gameState.player2.takeCardFromDeck() }

        gameTree.rootNode.childNodes = generateCardDrawPossibleStates(gameTree.rootNode, gameState)
    }

    fun getActivePlayerController(gameState: GameState): Agent? {
        return if (gameState.activePlayer === gameState.player1) player1Controller else player2Controller
    }

    fun setPlayerController(player: Player, controller: Agent?) {
        if (player === gameTree.rootNode.gameState.player1) {
            player1Controller = controller
        } else {
            player2Controller = controller
        }
    }

    fun getCurrentState(): GameState = gameTree.rootNode.gameState

    fun runMCTSPerformanceTest() {
        val mctsAgent = ProbabilisticAgent(gameTree)
        val initialGameState = gameTree.rootNode.gameState

        // simulate first draw
        drawCardOrGetPunished(initialGameState.activePlayer)

        // move root to node after first draw
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
            println("!!! New root node found after draw !!!")
            gameTree.updateRoot(newRootNode)

            printMCTSStatistics(newRootNode)
            // perform turn
            mctsAgent.performTurn(gameTree.rootNode.gameState)

            printMCTSStatistics(newRootNode)



        } else {
            println("!!! Not found new node after draw !!!")
        }

    }

    fun printMCTSStatistics(rootNode: Node) {
        val leafsDepthList = mutableListOf<Int>()
        val simChildsPart = mutableListOf<Float>()
        val childNodesCount = collectNodeStatsRecursive(rootNode, 1, leafsDepthList, simChildsPart)
        println("Child nodes count:" + childNodesCount)
        val leafsCount = leafsDepthList.size
        println("Leafs count: " + leafsCount)
        println(leafsDepthList)
        val leafsDepthAvg = leafsDepthList.average()
        println("Leafs average depth: " + leafsDepthAvg)
        leafsDepthList.sort()
        println(leafsDepthList)
        val leafsDepthMedian = leafsDepthList.get(leafsDepthList.size / 2)
        println("Leafs depth median: " + leafsDepthMedian)
        val leafsMinDepth = leafsDepthList.min()
        println("Leafs min depth: " + leafsMinDepth)
        val leafsMaxDepth = leafsDepthList.max()
        println("Leafs max depth: " + leafsMaxDepth)
        val nodesWithSimChildsCount = simChildsPart.count { it > 0 }
        println("Count of nodes with simulated childs (at least one): " + nodesWithSimChildsCount)
        val averagePartOfSim = simChildsPart.average()
        println("Average % of simulations per child: " + averagePartOfSim)
        val minPartOfSim = simChildsPart.min()
        println("Min % of simulations per childs: " + minPartOfSim)
        val maxPartOfSim = simChildsPart.max()
        println("Max % of simulations per childs: " + maxPartOfSim)
        println(simChildsPart.sort())
        println(simChildsPart)
        val medianPartOfSim = simChildsPart.get(simChildsPart.size / 2)
        println("Median % of simulations per childs: " + medianPartOfSim)
        val playoutsCount = rootNode.gamesPlayed
        println("Total simulations count: " + playoutsCount)

        // print in csv format
        println("tot_sim,tot_nodes,leaf_count,leaf_depth_avg,leaf_depth_med,leaf_depth_min,leaf_depth_max,has_sim_child,sim_child_pct_avg,sim_child_pct_med,sim_child_pct_min,sim_child_pct_max")
        println("$playoutsCount,$childNodesCount,$leafsCount,$leafsDepthAvg,$leafsDepthMedian,$leafsMinDepth,$leafsMaxDepth,$nodesWithSimChildsCount,$averagePartOfSim,$medianPartOfSim,$minPartOfSim,$maxPartOfSim")
    }

    fun collectNodeStatsRecursive(currNode: Node, depth: Int, leafsDepthList: MutableList<Int>, simChildsPart: MutableList<Float>): Int {
        var childNodesCount = 0
        if (currNode.childNodes.isEmpty()) {
            leafsDepthList.add(depth / 2) // Draw Node + Turn Node are calculated as once
        } else {
            var simulatedChildsCount = 0
            currNode.childNodes.forEach {
                childNodesCount += collectNodeStatsRecursive(it, depth + 1, leafsDepthList, simChildsPart)
                if (currNode is CardDrawingNode && it.gamesPlayed > 0) {
                    simulatedChildsCount += 1
                }
            }
            simChildsPart.add(simulatedChildsCount.toFloat() / currNode.childNodes.size)
            childNodesCount += currNode.childNodes.size
        }
        return childNodesCount
    }

    fun run() {
        player1Controller = randomAgent
        player2Controller = greedyAgent

        //val initialTreeRoot = gameTree.rootNode

        while (!gameEndConditionsMet(getCurrentState())) {
            println("Turn of ${getCurrentState().activePlayer.name}")
            println("Game state before ${getCurrentState().activePlayer.name} turn: ")
            println(getCurrentState())
            println("...")
            println("Game state after ${getCurrentState().activePlayer.name} turn: ")
            performTurn(getCurrentState().activePlayer)
            println(getCurrentState())

            //println(initialTreeRoot.printTree(0))

            println("______________________________")
        }

        val winningPlayer = if (getCurrentState().player1.healthPoints < getCurrentState().player2.healthPoints) getCurrentState().player2 else getCurrentState().player1
        println("Game end, the winning player is \n$winningPlayer")

        println(getCurrentState())
    }

    private fun performTurn(currentPlayer: Player) {

        if (getActivePlayerController(getCurrentState()) is ProbabilisticAgent) {
            if (gameTree.rootNode.childNodes.size <= 0) {
                println("!!! Force tree generation !!!")
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
            println("!!! New root node found after draw !!!")
            gameTree.updateRoot(newRootNode)
            println(gameTree.rootNode.getNodeInfo())
        } else {
            println("!!! Not found new node after draw !!!")
        }

        //println(gameTree.rootNode.getNodeInfo())

        val notMCTS: Boolean = (getActivePlayerController(getCurrentState()) !is ProbabilisticAgent)

        getActivePlayerController(getCurrentState())?.performTurn(getCurrentState())

        if (notMCTS) {
            // resolve tree
            val newRootNode: Node? = gameTree.rootNode.childNodes.find {
                it.gameState == gameTree.rootNode.gameState
            }

            if (newRootNode != null) {
                println("!!! New root node found after turn !!!")
                gameTree.updateRoot(newRootNode)
            } else {
                println("!!! Not found new node after turn !!!")
            }
        }
        println(gameTree.rootNode.getNodeInfo())
    }

    fun drawCardOrGetPunished(currentPlayer: Player) {
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

    private fun revertPlayerPunish(player: Player) {
        player.healthPoints += player.turnsWithDeckCardsDepleted * PUNISHMENT_VALUE
        player.turnsWithDeckCardsDepleted--
    }

    private fun Player.useRandomCard() {
        val drawnCard = handCards.popRandomElement()

        if (drawnCard is AdherentCard) {
            tableCards.add(drawnCard)
        } else if (drawnCard is SpellCard) {
//            drawnCard.applyEffect(activePlayer, getOpponent(activePlayer))
        } else throw IllegalStateException("The drawn card is neither Adherent nor Spell.")
    }
}

fun <E> MutableList<E>.push(element: E) {
    add(element)
}

fun <E> MutableList<E>.pop(): E {
    val lastItem = last()
    remove(lastItem)
    return lastItem
}

fun <E> MutableList<E>.removeExact(element: E): Boolean {
    return removeIf {
        it === element
    }
}

fun <E> MutableList<E>.indexOfExact(element: E): Int {
    return indexOfFirst {
        it === element
    }
}

fun <E> MutableList<E>.containsExact(element: E): Boolean {
    return any {
        it === element
    }
}

fun drawCardOrGetPunished(currentPlayer: Player) {
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

fun gameEndConditionsMet(gameState: GameState) =
        gameState.player1.healthPoints <= 0 || gameState.player2.healthPoints <= 0


fun generateCardDrawPossibleStates(parentNode: Node? = null, gameState: GameState): List<Node> {
    val possibleEndStateNodes = mutableListOf<Node>()
    val cardToDrawProbability = mutableMapOf<Card, Float>()

    gameState.activePlayer.deckCards.forEach {
        val cardDrawProbability = gameState.activePlayer.deckCards.count { iterCard ->
            iterCard.name == it.name
        } / gameState.activePlayer.deckCards.size.toFloat()
        cardToDrawProbability[it] = cardDrawProbability
    }

    cardToDrawProbability.forEach { card, probability ->
        val index = gameState.activePlayer.takeCardFromDeck(card)
        val gameStateAfterDraw = gameState.deepCopy()
        gameState.activePlayer.returnCardToDeck(card, index)

        val drawNode = CardDrawingNode(probability, gameStateAfterDraw, listOf(), parentNode)
        val drawNodeChildren = generatePossibleEndTurnGameStates(drawNode, gameStateAfterDraw)
        drawNode.childNodes = drawNodeChildren

        possibleEndStateNodes.add(drawNode)
    }

    if (cardToDrawProbability.isEmpty()) {
        val punishmentNode = CardDrawingNode(1f, gameState.deepCopy(), listOf(), parentNode)
        punishPlayerWithEmptyDeck(punishmentNode.gameState.activePlayer)
        val punishmentNodeChildren = generatePossibleEndTurnGameStates(punishmentNode, punishmentNode.gameState)
        punishmentNode.childNodes = punishmentNodeChildren
        possibleEndStateNodes.add(punishmentNode)
    }

    return possibleEndStateNodes
}

private fun generatePossibleEndTurnGameStates(parentNode: Node? = null, stateAfterCardDraw: GameState): MutableList<Node> {
    val endStatesSet = HashSet<GameState>()
    generateTurnTransitionalStates(endStatesSet, stateAfterCardDraw)

    return endStatesSet.map {
        Node(it, LinkedList(), parentNode)
    }.toMutableList()
}

private fun generateTurnTransitionalStates(leafStatesSet: MutableSet<GameState>, currentGameState: GameState) {
    with(currentGameState) {
        activePlayer.getAvailableActions(getOpponent(activePlayer)).forEach {
            if (it is EndTurn) {
                it.resolve(currentGameState)
                if (!leafStatesSet.contains(currentGameState)){
                    leafStatesSet.add(currentGameState.deepCopy())
                }
                it.rollback(currentGameState)
            } else {
                it.resolve(currentGameState)
                generateTurnTransitionalStates(leafStatesSet, currentGameState)
                it.rollback(currentGameState)
            }
        }
    }
}