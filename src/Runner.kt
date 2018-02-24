import models.*
import java.util.*

/**
 * Created by r.makowiecki on 23/02/2018.
 */

fun main(args: Array<String>) {

    val player1 = Player(
            mutableListOf(),
            createInitialDeck(),
            mutableListOf(),
            healthPoints = 20,
            mana = 0
    )

    val player2 = Player(
            mutableListOf(),
            createInitialDeck(),
            mutableListOf(),
            healthPoints = 20,
            mana = 0
    )

    Game(GameState(player1, player2, turnNumber = 0, activePlayer = player1)).run()
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

val dragonBreathEffect: (Card, Player, Player) -> List<Action> = { triggeringCard, _, enemyPlayer ->
    enemyPlayer.tableCards.forEach {
        it.currentHealthPoints -= 1
    }
    listOf(Action.HitAllEnemies(triggeringCard, 2))
}

val circleOfHealingEffect: (Card, Player, Player) -> List<Action> = { triggeringCard, currentPlayer, enemyPlayer ->
    val allTableCards = mutableListOf<AdherentCard>()
    currentPlayer.tableCards.forEach { allTableCards.add(it) }
    enemyPlayer.tableCards.forEach { allTableCards.add(it) }

    allTableCards.forEach {
        it.currentHealthPoints += 1
        it.currentHealthPoints = maxOf(it.maxHealthPoints, it.currentHealthPoints)
    }
    listOf(Action.HealAll(triggeringCard, 3))
}

val flameLanceEffect: (Card, Player, Player) -> List<Action> = { triggeringCard, _, enemyPlayer ->
    val actionList = mutableListOf<Action>()
    enemyPlayer.tableCards.forEach {
        actionList.add(Action.HitOne(triggeringCard, it, 4))
    }
    actionList
}



