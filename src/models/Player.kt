package models

import models.Card

/**
 * Created by r.makowiecki on 23/02/2018.
 */
data class Player(
        val handCards: MutableList<Card>,
        val deckCards: MutableList<Card>,
        val tableCards: MutableList<Card>,
        var healthPoints: Int,
        var mana: Int
) {
    init {
        println("models.Player initialized")
    }
}