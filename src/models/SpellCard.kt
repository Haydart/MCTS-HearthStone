package models

class SpellCard(
        val applyEffectFun: (List<Card>) -> Unit,
        name: String,
        manaCost: Int
) : Card(name, manaCost) {

    fun applyEffect(tableCardsList: List<Card>) {
        applyEffectFun(tableCardsList)
    }
}