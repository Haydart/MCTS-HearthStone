package models

import actions.Action
import actions.CardAction
import actions.EndTurn
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
        var mana: Int = 1,
        val name: String
) {

    var turnsWithDeckCardsDepleted = 0
    var discardedCount = 0

    fun getAvailableActions(enemyPlayer: Player): List<Action> {
        val actionsListBeforeConstraining = mutableListOf<CardAction>()

        handCards.forEach {
            actionsListBeforeConstraining += it.getActionsFun(it, this, enemyPlayer)
        }
        tableCards.forEach {
            actionsListBeforeConstraining += it.getActionsFun(it, this, enemyPlayer)
        }

        return actionsListBeforeConstraining
                .filter(this::userCanAffordTheCardIfInHand)
                .filter(this::placedAdherentCardsCountIsBelowLimit)
                .filter(this::cardWasNotUsedInCurrentTurn) + EndTurn()
    }

    private fun userCanAffordTheCardIfInHand(action: CardAction) = mana >= action.triggeringCard.manaCost || action.triggeringCard in tableCards

    private fun placedAdherentCardsCountIsBelowLimit(action: CardAction) = action !is PlaceAdherentCard || tableCards.size < MAX_ADHERENT_CARDS_LAID_OUT

    private fun cardWasNotUsedInCurrentTurn(action: CardAction) = action.triggeringCard !is AdherentCard || !(action.triggeringCard as AdherentCard).hasBeenUsedInCurrentTurn

    fun takeCardFromDeck() = handCards.add(deckCards.takeRandomElement())

    fun takeCardFromDeck(card: Card): Int {
        val cardIndex = deckCards.indexOf(card)
        handCards.add(deckCards.removeAt(cardIndex))
        return cardIndex
    }

    fun returnCardToDeck(card: Card, index: Int) {
        handCards.remove(card)
        deckCards.add(index, card)
    }

    fun deepCopy(): Player {
        val handCardsCopy = mutableListOf<Card>().apply {
            this@Player.handCards.forEach { card ->
                add(card.deepCopy())
            }
        }

        val deckCardsCopy = mutableListOf<Card>().apply {
            this@Player.deckCards.forEach { card ->
                add(card.deepCopy())
            }
        }

        val tableCardsCopy = mutableListOf<AdherentCard>().apply {
            this@Player.tableCards.forEach { card ->
                add(card.deepCopy())
            }
        }

        return Player(
                handCardsCopy,
                deckCardsCopy,
                tableCardsCopy,
                healthPoints,
                mana,
                name
        )
    }

    override fun toString() = "$name deckCards(${deckCards.size}), handCards(${handCards.size}), tableCards(${tableCards.size}), " +
            "discardedCards($discardedCount), HP($healthPoints), mana($mana)"
}

fun <E> MutableList<E>.takeRandomElement() = this.removeAt(Random().nextInt(this.size))