package models

import actions.Action
import actions.DrawCard
import actions.HitOne

val defaultAdherentActionsFun: (Card, Player, Player) -> List<Action> = { triggeringCard, (handCards), enemyPlayer ->
    val availableActions = mutableListOf<Action>()
    val isInHand = triggeringCard in handCards

    enemyPlayer.tableCards.forEach {
        availableActions += HitOne(triggeringCard, it, (triggeringCard as AdherentCard).attackStrength)
    }

    if(isInHand) availableActions += DrawCard(triggeringCard)
    availableActions
}

class AdherentCard(
        val maxHealthPoints: Int,
        var attackStrength: Int,
        var lastTurnPlaced: Int = -1,
        name: String,
        manaCost: Int
) : Card(name, manaCost, defaultAdherentActionsFun) {

    var currentHealthPoints: Int = maxHealthPoints

}