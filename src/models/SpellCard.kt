package models

import actions.Action

class SpellCard(
        name: String,
        manaCost: Int,
        getActionsFun: (Card, Player, Player) -> List<Action>
) : Card(name, manaCost, getActionsFun) {

    fun getActions(currentPlayer: Player, enemyPlayer: Player) = getActionsFun(this, currentPlayer, enemyPlayer)
}