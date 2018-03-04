package models

import actions.CardAction
import actions.FightAnotherAdherent
import actions.FightEnemyHero
import actions.PlaceAdherentCard
import containsExact

val defaultAdherentActionsFun: (Card, Player, Player) -> List<CardAction> = { triggeringCard, (handCards, _, tableCards), enemyPlayer ->
    triggeringCard as AdherentCard // smart casting
    val availableActions = mutableListOf<CardAction>()

    if (handCards.containsExact(triggeringCard)) {
        availableActions += PlaceAdherentCard(triggeringCard)
    } else if (tableCards.containsExact(triggeringCard) && !triggeringCard.hasBeenUsedInCurrentTurn) {
        enemyPlayer.tableCards.forEach { enemyCard ->
            availableActions += FightAnotherAdherent(triggeringCard, enemyCard)
        }
        availableActions += FightEnemyHero(triggeringCard)
    }

    availableActions
}

data class AdherentCard(
        val maxHealthPoints: Int,
        var attackStrength: Int,
        var hasBeenUsedInCurrentTurn: Boolean = false,
        private val cardName: String,
        private val cardManaCost: Int
) : Card(cardName, cardManaCost, defaultAdherentActionsFun) {

    var currentHealthPoints: Int = maxHealthPoints

    override fun deepCopy() = copy()

    override fun toString() = "name($name), manaCost($manaCost), wasUsedInThisTurn($hasBeenUsedInCurrentTurn)"
}