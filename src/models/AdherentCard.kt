package models

import actions.Action
import actions.DrawCard
import actions.FightAnotherAdherent

val defaultAdherentActionsFun: (Card, Player, Player) -> List<Action> = { triggeringCard, (handCards, _, tableCards), enemyPlayer ->
    val availableActions = mutableListOf<Action>()

    if (triggeringCard in handCards) {
        availableActions += DrawCard(triggeringCard)
    } else if (triggeringCard in tableCards) {
        enemyPlayer.tableCards.forEach { enemyCard ->
            availableActions += FightAnotherAdherent(triggeringCard as AdherentCard, enemyCard)
        }
        availableActions += FightEnemyHero()
    }

    availableActions
}

class AdherentCard(
        val maxHealthPoints: Int,
        var attackStrength: Int,
        var tablePlacementTurn: Int = -1,
        name: String,
        manaCost: Int
) : Card(name, manaCost, defaultAdherentActionsFun) {

    var currentHealthPoints: Int = maxHealthPoints
}