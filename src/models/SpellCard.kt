package models

class SpellCard(
        val getActionsFun: (SpellCard, Player, Player) -> List<Action>,
        name: String,
        manaCost: Int
) : Card(name, manaCost) {

    fun applyEffect(currentPlayer: Player, enemyPlayer: Player) {
        getActionsFun(this, currentPlayer, enemyPlayer)
        println("$name card effect applied.")
    }

    fun getActions(currentPlayer: Player, enemyPlayer: Player) = getActionsFun(this, currentPlayer, enemyPlayer)
}