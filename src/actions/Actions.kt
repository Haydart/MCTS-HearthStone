package actions

import GameState
import models.AdherentCard
import models.Card
import models.Player

/**
 * Created by r.makowiecki on 24/02/2018.
 */

typealias Index = Int

abstract class Action {
    abstract fun resolve(gameState: GameState)
    abstract fun rollback(gameState: GameState)
}

abstract class CardAction : Action() {
    abstract val triggeringCard: Card
}

class EndTurn : Action() {

    val usedCardsList = mutableListOf<AdherentCard>()

    override fun resolve(gameState: GameState) {
        gameState.activePlayer.tableCards.forEach {
            usedCardsList.add(it)
            it.hasBeenUsedInCurrentTurn = false
        }
    }

    override fun rollback(gameState: GameState) {
        usedCardsList.forEach { it.hasBeenUsedInCurrentTurn = true }
    }
}

abstract class AdherentCardAction : CardAction()

abstract class SpellCardAction : CardAction() {

    override fun resolve(gameState: GameState) {
        gameState.activePlayer.mana -= triggeringCard.manaCost
        gameState.activePlayer.handCards.remove(triggeringCard)
        gameState.activePlayer.discardedCount++
    }

    override fun rollback(gameState: GameState) {
        gameState.activePlayer.mana += triggeringCard.manaCost
        gameState.activePlayer.handCards.add(triggeringCard)
        gameState.activePlayer.discardedCount--
    }
}

class PlaceAdherentCard(override val triggeringCard: AdherentCard) : AdherentCardAction() {

    init {
        if (triggeringCard !is AdherentCard) throw IllegalAccessException("PlaceAdherentCard can only be the action of an adherent card.")
    }

    override fun resolve(gameState: GameState) {
        with(gameState.activePlayer) {
            mana -= triggeringCard.manaCost
            tableCards.add(triggeringCard)
            handCards.remove(triggeringCard)
            triggeringCard.hasBeenUsedInCurrentTurn = true
        }
    }

    override fun rollback(gameState: GameState) {
        with(gameState.activePlayer) {
            mana += triggeringCard.manaCost
            tableCards.remove(triggeringCard)
            handCards.add(triggeringCard)
            triggeringCard.hasBeenUsedInCurrentTurn = false
        }
    }
}

class FightAnotherAdherent(override val triggeringCard: AdherentCard, val targetCard: AdherentCard) : AdherentCardAction() {

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
        triggeringCard.hasBeenUsedInCurrentTurn = true
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
        triggeringCard.hasBeenUsedInCurrentTurn = false
    }
}

class FightEnemyHero(override val triggeringCard: AdherentCard) : AdherentCardAction() {

    override fun resolve(gameState: GameState) {
        with(gameState) {
            getOpponent(activePlayer).healthPoints -= triggeringCard.attackStrength
        }
        triggeringCard.hasBeenUsedInCurrentTurn = true
    }

    override fun rollback(gameState: GameState) {
        with(gameState) {
            getOpponent(activePlayer).healthPoints += triggeringCard.attackStrength
        }
        triggeringCard.hasBeenUsedInCurrentTurn = false
    }
}

class HitOne(override val triggeringCard: Card, val targetCard: AdherentCard, val damage: Int) : SpellCardAction() {

    var removedAtIndex = -1
    lateinit var killedAdherent: AdherentCard

    override fun resolve(gameState: GameState) {
        super.resolve(gameState)
        with(gameState) {
            targetCard.currentHealthPoints -= damage
            if (targetCard.currentHealthPoints <= 0) {
                removedAtIndex = getOpponent(activePlayer).tableCards.indexOf(targetCard)
                killedAdherent = getOpponent(activePlayer).tableCards.removeAt(removedAtIndex)
            }
        }
    }

    override fun rollback(gameState: GameState) {
        super.rollback(gameState)
        with(gameState) {
            if (removedAtIndex != -1) {
                getOpponent(activePlayer).tableCards.add(removedAtIndex, killedAdherent)
            }
            targetCard.currentHealthPoints += damage
        }
    }
}

class HitAllEnemies(override val triggeringCard: Card, val damage: Int) : SpellCardAction() {

    val removedIndices = mutableListOf<Int>()
    val killedAdherents = mutableListOf<AdherentCard>()

    override fun resolve(gameState: GameState) {
        super.resolve(gameState)
        with(gameState) {
            getOpponent(activePlayer).tableCards.forEach {
                it.currentHealthPoints -= damage

                if (it.currentHealthPoints <= 0) {
                    removedIndices.add(getOpponent(activePlayer).tableCards.indexOf(it))
                    val killedAdherent = getOpponent(activePlayer).tableCards.removeAt(removedIndices.last())
                    killedAdherents += killedAdherent
                }
            }
        }
    }

    override fun rollback(gameState: GameState) {
        super.rollback(gameState)
        with(gameState) {
            killedAdherents.forEachIndexed { loopIndex, adherentCard ->
                getOpponent(activePlayer).tableCards.add(removedIndices[loopIndex], adherentCard)
            }
            getOpponent(activePlayer).tableCards.forEach {
                it.currentHealthPoints += damage
            }
        }
    }
}

class HealOne(override val triggeringCard: Card, val targetCard: AdherentCard, val healAmount: Int) : SpellCardAction() {

    private var effectivelyHealedAmount = 0

    override fun resolve(gameState: GameState) {
        super.resolve(gameState)
        effectivelyHealedAmount = minOf(targetCard.maxHealthPoints - targetCard.currentHealthPoints, healAmount)
        targetCard.currentHealthPoints += effectivelyHealedAmount
    }

    override fun rollback(gameState: GameState) {
        super.rollback(gameState)
        targetCard.currentHealthPoints = targetCard.currentHealthPoints - effectivelyHealedAmount
    }
}

class HealAll(override val triggeringCard: Card, val healAmount: Int) : SpellCardAction() {

    private var effectivelyHealedAmounts = mutableListOf<Int>()
    private var allTableCards = mutableListOf<AdherentCard>()

    override fun resolve(gameState: GameState) {
        super.resolve(gameState)

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
        super.rollback(gameState)

        allTableCards.forEachIndexed { index, adherentCard ->
            adherentCard.currentHealthPoints -= effectivelyHealedAmounts[index]
        }
    }
}

class HealAllFriendly(override val triggeringCard: Card) : SpellCardAction() {

    override fun resolve(gameState: GameState) {
        super.resolve(gameState)
    }

    override fun rollback(gameState: GameState) {
        super.rollback(gameState)
    }
}

