package models

import actions.CardAction

abstract class Card(
        val name: String,
        val manaCost: Int,
        val getActionsFun: (Card, Player, Player) -> List<CardAction>
) {
    abstract fun deepCopy(): Card
}