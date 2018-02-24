package models

import java.util.*

/**
 * Created by r.makowiecki on 23/02/2018.
 */
data class Player(
        val handCards: MutableList<Card>,
        val deckCards: MutableList<Card>,
        val tableCards: MutableList<AdherentCard>,
        var healthPoints: Int,
        var mana: Int
) {

    var turnsWithDeckCardsDepleted = 0

    fun takeCardFromDeck() = handCards.add(deckCards.takeRandomElement())



    override fun toString() = "deckCards: ${deckCards.size}, handCards: ${handCards.size}, tableCards: ${tableCards.size}"
}

fun <E> MutableList<E>.takeRandomElement() = this.removeAt(Random().nextInt(this.size))

