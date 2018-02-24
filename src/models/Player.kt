package models

import actions.Action
import java.util.*

/**
 * Created by r.makowiecki on 23/02/2018.
 */
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

    fun getAvailableActions(enemyPlayer: Player): List<Action> {
        val availableActionsList = mutableListOf<Action>()

        handCards.forEach {
            availableActionsList += it.getActionsFun(it, this, enemyPlayer)
        }
        tableCards.forEach {
            availableActionsList += it.getActionsFun(it, this, enemyPlayer)
        }

        return availableActionsList
    }

    fun takeCardFromDeck() = handCards.add(deckCards.takeRandomElement())

    override fun toString() = "$name deckCards(${deckCards.size}), handCards(${handCards.size}), tableCards(${tableCards.size}), discardedCards($discardedCount)"
}

fun <E> MutableList<E>.takeRandomElement() = this.removeAt(Random().nextInt(this.size))