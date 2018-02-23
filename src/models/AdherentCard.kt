package models

class AdherentCard(
        val maxHealthPoints: Int,
        var attackStrength: Int,
        var lastTurnPlaced: Int = -1,
        name: String,
        manaCost: Int
) : Card(name, manaCost) {

    var currentHealthPoints: Int = maxHealthPoints


}