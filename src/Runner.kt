import models.AdherentCard
import models.Card
import models.SpellCard

/**
 * Created by r.makowiecki on 23/02/2018.
 */


const val ADHERENTS_COUNT = 7

fun main(args: Array<String>) {

    val player1 = Player(
            mutableListOf(),
            createInitialDeck(),
            mutableListOf(),
            20,
            0
    )

    val player2 = Player(
            mutableListOf(),
            createInitialDeck(),
            mutableListOf(),
            20,
            0
    )
}

fun createInitialDeck(): MutableList<Card> = mutableListOf<Card>().apply {
    (0..1).forEach {
        add(AdherentCard(maxHealthPoints = 2, attackStrength = 5, manaCost = 2, name = "Hidden Gnome"))
        add(AdherentCard(maxHealthPoints = 3, attackStrength = 2, manaCost = 1, name = "Kobold"))
        add(AdherentCard(maxHealthPoints = 5, attackStrength = 7, manaCost = 6, name = "Mad Bomber"))
        add(AdherentCard(maxHealthPoints = 6, attackStrength = 1, manaCost = 4, name = "Micro Machine"))
        add(AdherentCard(maxHealthPoints = 7, attackStrength = 3, manaCost = 5, name = "Panther"))
        add(AdherentCard(maxHealthPoints = 5, attackStrength = 2, manaCost = 3, name = "Public Defender"))
        add(AdherentCard(maxHealthPoints = 2, attackStrength = 3, manaCost = 1, name = "Murloc"))

        add(SpellCard(name = "Flame Lance", manaCost = 4))
        add(SpellCard(name = "Dragon's Breath", manaCost = 5))
        add(SpellCard(name = "Circle of Healing", manaCost = 0))
    }
}



