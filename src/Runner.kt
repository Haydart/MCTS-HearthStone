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
            name = "Player1"
    )

    val player2 = Player(
            mutableListOf(),
            createInitialDeck(),
            mutableListOf(),
            healthPoints = 20,
            name = "Player2"
    )

    val gameInstance = Game(GameState(player1, player2, turnNumber = 0, activePlayer = player1))
    gGameInstance = gameInstance

    val gameMode = GameMode.GUI
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
        add(AdherentCard(maxHealthPoints = 2, attackStrength = 5, manaCost = 2, name = "Hidden Gnome"))
        add(AdherentCard(maxHealthPoints = 3, attackStrength = 2, manaCost = 1, name = "Kobold"))
        add(AdherentCard(maxHealthPoints = 5, attackStrength = 7, manaCost = 6, name = "Mad Bomber"))
        add(AdherentCard(maxHealthPoints = 6, attackStrength = 1, manaCost = 4, name = "Micro Machine"))
        add(AdherentCard(maxHealthPoints = 7, attackStrength = 3, manaCost = 5, name = "Panther"))
        add(AdherentCard(maxHealthPoints = 5, attackStrength = 2, manaCost = 3, name = "Public Defender"))
        add(AdherentCard(maxHealthPoints = 2, attackStrength = 3, manaCost = 1, name = "Murloc"))

        add(SpellCard(name = "Flame Lance", manaCost = 4, getActionsFun = flameLanceEffect))
        add(SpellCard(name = "Dragon's Breath", manaCost = 5, getActionsFun = dragonBreathEffect))
        add(SpellCard(name = "Circle of Healing", manaCost = 0, getActionsFun = circleOfHealingEffect))
    }
}

val dragonBreathEffect: (Card, Player, Player) -> List<CardAction> = { triggeringCard, _, enemyPlayer ->
    listOf(HitAllEnemies(triggeringCard, damage = 2))
}

val circleOfHealingEffect: (Card, Player, Player) -> List<CardAction> = { triggeringCard, currentPlayer, enemyPlayer ->
    val allTableCards = mutableListOf<AdherentCard>()
    currentPlayer.tableCards.forEach { allTableCards.add(it) }
    enemyPlayer.tableCards.forEach { allTableCards.add(it) }

    listOf(HealAll(triggeringCard, healAmount = 3))
}

val flameLanceEffect: (Card, Player, Player) -> List<CardAction> = { triggeringCard, _, enemyPlayer ->
    val actionList = mutableListOf<CardAction>()
    enemyPlayer.tableCards.forEach {
        actionList.add(HitOne(triggeringCard, it, damage = 4))
    }
    actionList
}



