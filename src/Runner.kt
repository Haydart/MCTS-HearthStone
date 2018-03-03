import actions.CardAction
import actions.HealAll
import actions.HitAllEnemies
import actions.HitOne
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
    GUI
}

fun main(args: Array<String>) {

    val player1 = Player(
            mutableListOf(),
            createInitialDeck(),
            mutableListOf(),
            healthPoints = 20,
            name = "Random agent",
            turnsWithDeckCardsDepleted = 0,
            discardedCount = 0
    )

    val player2 = Player(
            mutableListOf(),
            createInitialDeck(),
            mutableListOf(),
            healthPoints = 20,
            name = "Aggressive agent",
            turnsWithDeckCardsDepleted = 0,
            discardedCount = 0
    )

    val gameInstance = Game(GameState(player1, player2, turnNumber = 0, activePlayer = player1))
    gGameInstance = gameInstance

    val gameMode = GameMode.TEXT
    when (gameMode) {
        GameMode.TEXT -> {
            gameInstance.run()
        }
        GameMode.GUI -> {
            val gameWindow = GameWindow()
            gameWindow.launchWindow(args)
        }
    }
}

fun createInitialDeck(): MutableList<Card> = mutableListOf<Card>().apply {
    (0..1).forEach {
        add(AdherentCard(maxHealthPoints = 2, attackStrength = 5, cardManaCost = 2, cardName = "Hidden Gnome"))
        add(AdherentCard(maxHealthPoints = 3, attackStrength = 2, cardManaCost = 1, cardName = "Kobold"))
        add(AdherentCard(maxHealthPoints = 5, attackStrength = 7, cardManaCost = 6, cardName = "Mad Bomber"))
        add(AdherentCard(maxHealthPoints = 6, attackStrength = 1, cardManaCost = 4, cardName = "Micro Machine"))
        add(AdherentCard(maxHealthPoints = 7, attackStrength = 3, cardManaCost = 5, cardName = "Panther"))
        add(AdherentCard(maxHealthPoints = 5, attackStrength = 2, cardManaCost = 3, cardName = "Public Defender"))
        add(AdherentCard(maxHealthPoints = 2, attackStrength = 3, cardManaCost = 1, cardName = "Murloc"))
        add(SpellCard(cardName = "Flame Lance", cardManaCost = 4, cardGetActionsFun = flameLanceEffect))
        add(SpellCard(cardName = "Dragon's Breath", cardManaCost = 5, cardGetActionsFun = dragonBreathEffect))
        add(SpellCard(cardName = "Circle of Healing", cardManaCost = 0, cardGetActionsFun = circleOfHealingEffect))
    }
}

val dragonBreathEffect: (Card, Player, Player) -> List<CardAction> = { triggeringCard, _, enemyPlayer ->
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



