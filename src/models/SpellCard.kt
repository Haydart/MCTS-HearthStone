package models

class SpellCard(
        name: String,
        manaCost: Int,
        getActionsFun: (Card, Player, Player) -> List<Action>
) : Card(name, manaCost, getActionsFun) {

    fun applyEffect(currentPlayer: Player, enemyPlayer: Player) {
        getActionsFun(this, currentPlayer, enemyPlayer)
        println("$name card effect applied.")
    }

    fun getActions(currentPlayer: Player, enemyPlayer: Player) = getActionsFun(this, currentPlayer, enemyPlayer)
}