package models

import actions.Action

open class Card(
        val name: String,
        val manaCost: Int,
        val getActionsFun: (Card, Player, Player) -> List<Action>
)