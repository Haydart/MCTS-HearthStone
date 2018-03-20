package models

import actions.Action
import actions.CardAction
import actions.EndTurn
import actions.PlaceAdherentCard
import containsExact
import indexOfExact
import removeExact
import java.util.*


/**
 * Created by r.makowiecki on 23/02/2018.
 */

const val MAX_ADHERENT_CARDS_LAID_OUT = 7
const val MAX_MANA = 10

data class Player(
        val handCards: MutableList<Card>,
        val deckCards: MutableList<Card>,
        val tableCards: MutableList<AdherentCard>,
        var healthPoints: Int,
        var mana: Int = 1,
        val name: String,
        var turnsWithDeckCardsDepleted: Int = 0,
        var discardedCount: Int = 0
) {

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

    private fun userCanAffordTheCardIfInHand(action: CardAction) = mana >= action.triggeringCard.manaCost || (action.triggeringCard is AdherentCard && tableCards.containsExact(action.triggeringCard as AdherentCard))

    private fun placedAdherentCardsCountIsBelowLimit(action: CardAction) = action !is PlaceAdherentCard || tableCards.size < MAX_ADHERENT_CARDS_LAID_OUT

    private fun cardWasNotUsedInCurrentTurn(action: CardAction) = action.triggeringCard !is AdherentCard || !(action.triggeringCard as AdherentCard).hasBeenUsedInCurrentTurn

    fun takeCardFromDeck() = handCards.add(deckCards.popRandomElement())

    fun takeCardFromDeck(card: Card): Int {
        val cardIndex = deckCards.indexOfExact(card)
        handCards.add(deckCards.removeAt(cardIndex))
        return cardIndex
    }

    fun returnCardToDeck(card: Card, index: Int) {
        handCards.removeExact(card)
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
                name,
                turnsWithDeckCardsDepleted = turnsWithDeckCardsDepleted,
                discardedCount = discardedCount
        )
    }

    override fun toString() = "$name deckCards(${deckCards.size}), handCards(${handCards.size}), tableCards(${tableCards.size}), " +
            "discardedCards($discardedCount), HP($healthPoints), mana($mana)"


    override fun hashCode(): Int {
        //return (((((((if (this.handCards != null) this.handCards.hashCode() else 0) * 31 + if (this.deckCards != null) this.deckCards.hashCode() else 0) * 31 + if (this.tableCards != null) this.tableCards.hashCode() else 0) * 31 + this.healthPoints) * 31 + this.mana) * 31 + if (this.name != null) this.name.hashCode() else 0) * 31 + this.turnsWithDeckCardsDepleted) * 31 + this.discardedCount
        return (((((((if (this.handCards != null) this.handCards.sumBy { it.hashCode() } else 0) * 31 + if (this.deckCards != null) this.deckCards.sumBy { it.hashCode() } else 0) * 31 + if (this.tableCards != null) this.tableCards.sumBy { it.hashCode() } else 0) * 31 + this.healthPoints) * 31 + this.mana) * 31 + if (this.name != null) this.name.hashCode() else 0) * 31 + this.turnsWithDeckCardsDepleted) * 31 + this.discardedCount
    }
}

fun <E> MutableList<E>.popRandomElement() = removeAt(Random().nextInt(size))

fun <E> List<E>.getRandomElement() = get(Random().nextInt(size))