package models

import actions.CardAction

open class Card(
        val name: String,
        val manaCost: Int,
        val getActionsFun: (Card, Player, Player) -> List<CardAction>
)