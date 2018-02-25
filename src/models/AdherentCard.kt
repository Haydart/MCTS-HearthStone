package models

import actions.CardAction
import actions.FightAnotherAdherent
import actions.FightEnemyHero
import actions.PlaceAdherentCard

val defaultAdherentActionsFun: (Card, Player, Player) -> List<CardAction> = { triggeringCard, (handCards, _, tableCards), enemyPlayer ->
    triggeringCard as AdherentCard // smart casting
    val availableActions = mutableListOf<CardAction>()

    if (triggeringCard in handCards) {
        availableActions += PlaceAdherentCard(triggeringCard)
    } else if (triggeringCard in tableCards && !triggeringCard.hasBeenUsedInCurrentTurn) {
        enemyPlayer.tableCards.forEach { enemyCard ->
            availableActions += FightAnotherAdherent(triggeringCard, enemyCard)
        }
        availableActions += FightEnemyHero(triggeringCard)
    }

    availableActions
}

class AdherentCard(
        val maxHealthPoints: Int,
        var attackStrength: Int,
        var hasBeenUsedInCurrentTurn: Boolean = false,
        name: String,
        manaCost: Int
) : Card(name, manaCost, defaultAdherentActionsFun) {

    var currentHealthPoints: Int = maxHealthPoints

    override fun toString() = "name($name), manaCost($manaCost), wasUsedInThisTurn($hasBeenUsedInCurrentTurn)"
}