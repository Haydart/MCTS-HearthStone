package models

import actions.CardAction
import actions.PlaceAdherentCard
import java.util.*

/**
 * Created by r.makowiecki on 23/02/2018.
 */

const val MAX_ADHERENT_CARDS_LAID_OUT = 7

data class Player(
        val handCards: MutableList<Card>,
        val deckCards: MutableList<Card>,
        val tableCards: MutableList<AdherentCard>,
        var healthPoints: Int,
        var mana: Int,
        val name: String
) {

    var turnsWithDeckCardsDepleted = 0
    var discardedCount = 0

    fun getAvailableActions(enemyPlayer: Player): List<CardAction> {
        val actionsListBeforeConstraining = mutableListOf<CardAction>()

        handCards.forEach {
            actionsListBeforeConstraining += it.getActionsFun(it, this, enemyPlayer)
        }
        tableCards.forEach {
            actionsListBeforeConstraining += it.getActionsFun(it, this, enemyPlayer)
        }

        return actionsListBeforeConstraining
                .filter { mana >= it.triggeringCard.manaCost }
                .filter { it !is PlaceAdherentCard || tableCards.size < MAX_ADHERENT_CARDS_LAID_OUT }
                .filter { it.triggeringCard !is AdherentCard || !(it.triggeringCard as AdherentCard).hasBeenUsedInCurrentTurn }
    }

    fun takeCardFromDeck() = handCards.add(deckCards.takeRandomElement())

    override fun toString() = "$name deckCards(${deckCards.size}), handCards(${handCards.size}), tableCards(${tableCards.size}), " +
            "discardedCards($discardedCount), HP($healthPoints), mana($mana)"
}

fun <E> MutableList<E>.takeRandomElement() = this.removeAt(Random().nextInt(this.size))