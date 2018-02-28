package models

import actions.CardAction

data class SpellCard(
        private val cardName: String,
        private val cardManaCost: Int,
        private val cardGetActionsFun: (Card, Player, Player) -> List<CardAction>
) : Card(cardName, cardManaCost, cardGetActionsFun) {

    override fun deepCopy() = copy()

    override fun toString() = "name($name), manaCost($manaCost)"
}