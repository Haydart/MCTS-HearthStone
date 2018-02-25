package models

import actions.Action
import actions.DrawCard
import actions.FightAnotherAdherent
import actions.FightEnemyHero

val defaultAdherentActionsFun: (Card, Player, Player) -> List<Action> = { triggeringCard, (handCards, _, tableCards), enemyPlayer ->
    triggeringCard as AdherentCard // smart casting
    val availableActions = mutableListOf<Action>()

    if (triggeringCard in handCards) {
        availableActions += DrawCard(triggeringCard)
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
        var hasBeenUsedInCurrentTurn: Boolean,
        name: String,
        manaCost: Int
) : Card(name, manaCost, defaultAdherentActionsFun) {

    var currentHealthPoints: Int = maxHealthPoints
}