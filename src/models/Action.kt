package models

import GameState

/**
 * Created by r.makowiecki on 24/02/2018.
 */

sealed class Action {

    abstract val triggeringCard: Card
    abstract fun resolve(gameState: GameState)
    abstract fun rollback(gameState: GameState)

    class HealOne(override val triggeringCard: Card, val targetCard: AdherentCard, val healAmount: Int) : Action() {

        private var effectivelyHealedAmount = 0

        override fun resolve(gameState: GameState) {
            effectivelyHealedAmount = minOf(targetCard.maxHealthPoints - targetCard.currentHealthPoints, healAmount)
            targetCard.currentHealthPoints += effectivelyHealedAmount
        }

        override fun rollback(gameState: GameState) {
            targetCard.currentHealthPoints = targetCard.currentHealthPoints - effectivelyHealedAmount
        }
    }

    class HealAll(override val triggeringCard: Card, healAmount: Int) : Action() {
        override fun resolve(gameState: GameState) = Unit

        override fun rollback(gameState: GameState) = Unit
    }

    class HealAllFriendly(override val triggeringCard: Card) : Action() {
        override fun resolve(gameState: GameState) = Unit

        override fun rollback(gameState: GameState) = Unit
    }

    class HitOne(override val triggeringCard: Card, val targetCard: AdherentCard, val damage: Int) : Action() {
        override fun resolve(gameState: GameState) {
            targetCard.currentHealthPoints -= damage
        }

        override fun rollback(gameState: GameState) {
            targetCard.currentHealthPoints += damage
        }
    }

    class FightAnotherAdherent(override val triggeringCard: AdherentCard, val targetCard: AdherentCard) : Action() {
        override fun resolve(gameState: GameState) {
            targetCard.currentHealthPoints -= triggeringCard.attackStrength
            triggeringCard.currentHealthPoints -= targetCard.attackStrength
        }

        override fun rollback(gameState: GameState) {
            targetCard.currentHealthPoints += triggeringCard.attackStrength
            triggeringCard.currentHealthPoints += targetCard.attackStrength
        }
    }

    class HitAllEnemies(override val triggeringCard: Card, val damage: Int) : Action() {
        override fun rollback(gameState: GameState) = Unit

        override fun resolve(gameState: GameState) = Unit
    }
}
