import actions.CardAction
import actions.HealAll
import actions.HitAllEnemies
import actions.HitOne
import greedyagents.AggressiveGreedyAgent
import greedyagents.ControllingGreedyAgent
import greedyagents.RandomGreedyAgent
import models.AdherentCard
import models.Card
import models.Player
import models.SpellCard

import view.GameWindow

/**
 * Created by r.makowiecki on 23/02/2018.
 */

var gGameInstance: Game? = null

enum class GameMode {
    TEXT,
    GUI,
    PERFORMANCE_TEST,
    MCTS_QUALITY_TEST
}

fun main(args: Array<String>) {

    val player1 = Player(
            mutableListOf(),
            createInitialDeck(),
            mutableListOf(),
            healthPoints = 20,
            name = "player1",
            turnsWithDeckCardsDepleted = 0,
            discardedCount = 0
    )

    val player2 = Player(
            mutableListOf(),
            createInitialDeck(),
            mutableListOf(),
            healthPoints = 20,
            name = "player2",
            turnsWithDeckCardsDepleted = 0,
            discardedCount = 0
    )

    val gameInstance = Game(GameState(player1, player2, turnNumber = 1, activePlayer = player1))
    gGameInstance = gameInstance

    val gameMode = GameMode.MCTS_QUALITY_TEST
    when (gameMode) {
        GameMode.TEXT -> {
            gameInstance.run()
        }
        GameMode.GUI -> {
            val gameWindow = GameWindow()
            gameWindow.launchWindow(args)
        }
        GameMode.PERFORMANCE_TEST -> {
            gameInstance.runMCTSPerformanceTest()
        }
        GameMode.MCTS_QUALITY_TEST -> {
            (0..25).forEach {
                val startTime = System.currentTimeMillis()
                MctsTestGame(GameState(player1, player2, turnNumber = 1, activePlayer = player1), RandomGreedyAgent(), false).run()
                print("Game took ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            }
            (0..25).forEach {
                MctsTestGame(GameState(player1, player2, turnNumber = 1, activePlayer = player1), RandomGreedyAgent(), true).run()
            }
            (0..25).forEach {
                MctsTestGame(GameState(player1, player2, turnNumber = 1, activePlayer = player1), ControllingGreedyAgent(), false).run()
            }
            (0..25).forEach {
                MctsTestGame(GameState(player1, player2, turnNumber = 1, activePlayer = player1), ControllingGreedyAgent(), true).run()
            }
            (0..25).forEach {
                MctsTestGame(GameState(player1, player2, turnNumber = 1, activePlayer = player1), AggressiveGreedyAgent(), false).run()
            }
            (0..25).forEach {
                MctsTestGame(GameState(player1, player2, turnNumber = 1, activePlayer = player1), AggressiveGreedyAgent(), true).run()
            }
        }
    }
}

fun createInitialDeck(): MutableList<Card> = mutableListOf<Card>().apply {
    (0..1).forEach {
        add(AdherentCard(maxHealthPoints = 2, attackStrength = 5, cardManaCost = 2, cardName = "Hidden Gnome"))
        add(AdherentCard(maxHealthPoints = 3, attackStrength = 2, cardManaCost = 1, cardName = "Kobold"))
        add(AdherentCard(maxHealthPoints = 5, attackStrength = 7, cardManaCost = 6, cardName = "Mad Bomber"))
        add(AdherentCard(maxHealthPoints = 6, attackStrength = 1, cardManaCost = 4, cardName = "Micro Machine"))
        add(AdherentCard(maxHealthPoints = 7, attackStrength = 3, cardManaCost = 5, cardName = "Panther", hasChargeAbility = true))
        add(AdherentCard(maxHealthPoints = 5, attackStrength = 2, cardManaCost = 3, cardName = "Public Defender", hasProvocationAbility = true))
        add(AdherentCard(maxHealthPoints = 2, attackStrength = 3, cardManaCost = 1, cardName = "Murloc"))
        add(SpellCard(cardName = "Flame Lance", cardManaCost = 4, cardGetActionsFun = flameLanceEffect))
        add(SpellCard(cardName = "Dragon's Breath", cardManaCost = 5, cardGetActionsFun = dragonBreathEffect))
        add(SpellCard(cardName = "Circle of Healing", cardManaCost = 0, cardGetActionsFun = circleOfHealingEffect))
    }
}

val dragonBreathEffect: (Card, Player, Player) -> List<CardAction> = { triggeringCard, _, _ ->
    listOf(HitAllEnemies(triggeringCard, damage = 2))
}

val circleOfHealingEffect: (Card, Player, Player) -> List<CardAction> = { triggeringCard, _, _ ->
    listOf(HealAll(triggeringCard, healAmount = 3))
}

val flameLanceEffect: (Card, Player, Player) -> List<CardAction> = { triggeringCard, _, enemyPlayer ->
    val actionList = mutableListOf<CardAction>()
    enemyPlayer.tableCards.forEach {
        actionList.add(HitOne(triggeringCard, it, damage = 4))
    }
    actionList
}