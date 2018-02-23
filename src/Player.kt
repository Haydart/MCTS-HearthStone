import models.Card

/**
 * Created by r.makowiecki on 23/02/2018.
 */
class Player(
        val handCards: MutableList<Card>,
        val deckCards: MutableList<Card>,
        val tableCards: MutableList<Card>,
        var healthPoints: Int,
        var mana: Int
) {
    init {
        println("Player initialized")
    }
}