package models

import actions.CardAction

class SpellCard(
        name: String,
        manaCost: Int,
        getActionsFun: (Card, Player, Player) -> List<CardAction>
) : Card(name, manaCost, getActionsFun) {

    fun getActions(currentPlayer: Player, enemyPlayer: Player) = getActionsFun(this, currentPlayer, enemyPlayer)
}