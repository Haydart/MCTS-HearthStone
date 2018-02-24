package models

import GameState

/**
 * Created by r.makowiecki on 24/02/2018.
 */

typealias Index = Int

sealed class Action {

    abstract val triggeringCard: Card
    abstract fun resolve(gameState: GameState)
    abstract fun rollback(gameState: GameState)

    class DrawCard(override val triggeringCard: Card) : Action() {

        init {
            if (triggeringCard !is AdherentCard) throw IllegalAccessException("DrawCard can only be the action of an adherent card.")
        }

        override fun resolve(gameState: GameState) {
            with(gameState.activePlayer) {
                tableCards.add(triggeringCard as AdherentCard)
                handCards.remove(triggeringCard)
            }
        }

        override fun rollback(gameState: GameState) {
            with(gameState.activePlayer) {
                tableCards.remove(triggeringCard)
                handCards.add(triggeringCard)
            }
        }
    }

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

    class HealAll(override val triggeringCard: Card, val healAmount: Int) : Action() {

        private var effectivelyHealedAmounts = mutableListOf<Int>()
        private var allTableCards = mutableListOf<AdherentCard>()

        override fun resolve(gameState: GameState) {

            gameState.activePlayer.handCards.remove(triggeringCard)

            gameState.player1.tableCards.forEach {
                allTableCards.add(it)
            }
            gameState.player2.tableCards.forEach {
                allTableCards.add(it)
            }

            allTableCards.forEach {
                val effectivelyHealedAmount = minOf(it.maxHealthPoints - it.currentHealthPoints, healAmount)
                effectivelyHealedAmounts.add(effectivelyHealedAmount)
                it.currentHealthPoints += effectivelyHealedAmount
            }
        }

        override fun rollback(gameState: GameState) {
            gameState.activePlayer.handCards.add(triggeringCard)
            allTableCards.forEachIndexed { index, adherentCard ->
                adherentCard.currentHealthPoints -= effectivelyHealedAmounts[index]
            }
        }
    }

    class HealAllFriendly(override val triggeringCard: Card) : Action() {
        override fun resolve(gameState: GameState) = Unit

        override fun rollback(gameState: GameState) = Unit
    }

    class HitOne(override val triggeringCard: Card, val targetCard: AdherentCard, val damage: Int) : Action() {

        var removedAtIndex = -1
        lateinit var killedAdherent: AdherentCard

        override fun resolve(gameState: GameState) {
            with(gameState) {
                targetCard.currentHealthPoints -= damage
                activePlayer.handCards.remove(triggeringCard)

                if (targetCard.currentHealthPoints <= 0) {
                    removedAtIndex = getOpponent(activePlayer).tableCards.indexOf(targetCard)
                    killedAdherent = getOpponent(activePlayer).tableCards.removeAt(removedAtIndex)
                }
            }
        }

        override fun rollback(gameState: GameState) {
            with(gameState) {
                if (removedAtIndex != -1) {
                    getOpponent(activePlayer).tableCards.add(removedAtIndex, killedAdherent)
                }
                targetCard.currentHealthPoints += damage
                activePlayer.handCards.add(triggeringCard)
            }
        }
    }

    class FightAnotherAdherent(override val triggeringCard: AdherentCard, val targetCard: AdherentCard) : Action() {

        var activePlayerKilledAdherent: Pair<Index, AdherentCard>? = null
        var enemyPlayerKilledAdherent: Pair<Index, AdherentCard>? = null

        override fun resolve(gameState: GameState) = with(gameState) {
            targetCard.currentHealthPoints -= triggeringCard.attackStrength
            triggeringCard.currentHealthPoints -= targetCard.attackStrength

            if (targetCard.currentHealthPoints <= 0) {
                val enemyPlayer = getOpponent(activePlayer)
                val removedAt = enemyPlayer.tableCards.indexOf(targetCard)
                val removedAdherent = enemyPlayer.tableCards.removeAt(removedAt)
                enemyPlayerKilledAdherent = Pair(removedAt, removedAdherent)
            }

            if (triggeringCard.currentHealthPoints <= 0) {
                val removedAt = activePlayer.tableCards.indexOf(targetCard)
                val removedAdherent = activePlayer.tableCards.removeAt(removedAt)
                activePlayerKilledAdherent = Pair(removedAt, removedAdherent)
            }
        }

        override fun rollback(gameState: GameState) = with(gameState) {

            activePlayerKilledAdherent?.let { (index, adherentCard) ->
                activePlayer.tableCards.add(index, adherentCard)
            }

            enemyPlayerKilledAdherent?.let { (index, adherentCard) ->
                getOpponent(activePlayer).tableCards.add(index, adherentCard)
            }

            targetCard.currentHealthPoints += triggeringCard.attackStrength
            triggeringCard.currentHealthPoints += targetCard.attackStrength
        }
    }

    class HitAllEnemies(override val triggeringCard: Card, val damage: Int) : Action() {

        val removedIndices = mutableListOf<Int>()
        val killedAdherents = mutableListOf<AdherentCard>()

        override fun resolve(gameState: GameState) = with(gameState) {

            activePlayer.handCards.remove(triggeringCard)

            getOpponent(activePlayer).tableCards.forEach {
                it.currentHealthPoints -= damage

                if (it.currentHealthPoints <= 0) {
                    removedIndices.add(getOpponent(activePlayer).tableCards.indexOf(it))
                    val killedAdherent = getOpponent(activePlayer).tableCards.removeAt(removedIndices.last())
                    killedAdherents += killedAdherent
                }
            }
        }

        override fun rollback(gameState: GameState) = with(gameState) {
            killedAdherents.forEachIndexed { loopIndex, adherentCard ->
                getOpponent(activePlayer).tableCards.add(removedIndices[loopIndex], adherentCard)
            }

            getOpponent(activePlayer).tableCards.forEach {
                it.currentHealthPoints += damage
            }
        }
    }
}
