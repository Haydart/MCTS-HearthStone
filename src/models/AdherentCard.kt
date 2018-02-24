package models

import actions.Action

val defaultAdherentActionsFun: (Card, Player, Player) -> List<Action> = { triggeringCard, (handCards), enemyPlayer ->
    val availableActions = mutableListOf<Action>()
    val isInHand = triggeringCard in handCards

    enemyPlayer.tableCards.forEach {
        availableActions += Action.HitOne(triggeringCard, it, (triggeringCard as AdherentCard).attackStrength)
    }

    if(isInHand) availableActions += Action.DrawCard(triggeringCard)
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