package models

class SpellCard(
        val applyEffectFun: (Player, Player) -> Unit,
        name: String,
        manaCost: Int
) : Card(name, manaCost) {

    fun applyEffect(currentPlayer: Player, enemyPlayer: Player) {
        applyEffectFun(currentPlayer, enemyPlayer)
    }
}