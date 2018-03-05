package actions

import GameState
import indexOfExact
import models.AdherentCard
import models.Card
import removeExact

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

    private val usedCardsList = mutableListOf<AdherentCard>()
    private var manaPointsAtTurnsEnd: Int = 0

    override fun resolve(gameState: GameState) {
        with(gameState) {
            activePlayer.tableCards.forEach {
                if (it.hasBeenUsedInCurrentTurn) {
                    usedCardsList.add(it)
                    it.hasBeenUsedInCurrentTurn = false
                }
            }

            manaPointsAtTurnsEnd = activePlayer.mana
            gameState.turnNumber++
            activePlayer.mana = (gameState.turnNumber - 1) / 2 + 1
            activePlayer = getOpponent(activePlayer)
        }
    }

    override fun rollback(gameState: GameState) {
        with(gameState) {
            usedCardsList.forEach { it.hasBeenUsedInCurrentTurn = true }

            activePlayer = getOpponent(activePlayer)
            activePlayer.mana = manaPointsAtTurnsEnd
            gameState.turnNumber--
        }
    }
}

abstract class AdherentCardAction : CardAction()

abstract class SpellCardAction : CardAction() {
    var spellRemovedAtIndex = -1

    override fun resolve(gameState: GameState) {
        gameState.activePlayer.mana -= triggeringCard.manaCost
        spellRemovedAtIndex = gameState.activePlayer.handCards.indexOf(triggeringCard)
        gameState.activePlayer.handCards.removeExact(triggeringCard)
        gameState.activePlayer.discardedCount++
    }

    override fun rollback(gameState: GameState) {
        gameState.activePlayer.discardedCount--
        gameState.activePlayer.handCards.add(spellRemovedAtIndex, triggeringCard)
        gameState.activePlayer.mana += triggeringCard.manaCost
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
            handCards.removeExact(triggeringCard)
            triggeringCard.hasBeenUsedInCurrentTurn = true
        }
    }

    override fun rollback(gameState: GameState) {
        with(gameState.activePlayer) {
            mana += triggeringCard.manaCost
            tableCards.removeExact(triggeringCard)
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
            val removedAt = enemyPlayer.tableCards.indexOfExact(targetCard)
            val removedAdherent = enemyPlayer.tableCards.removeAt(removedAt)
            enemyPlayerKilledAdherent = Pair(removedAt, removedAdherent)
        }

        if (triggeringCard.currentHealthPoints <= 0) {
            val removedAt = activePlayer.tableCards.indexOfExact(triggeringCard)
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
                removedAtIndex = getOpponent(activePlayer).tableCards.indexOfExact(targetCard)
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
        }
        super.rollback(gameState)
    }
}

class HitAllEnemies(override val triggeringCard: Card, val damage: Int) : SpellCardAction() {

    val removedIndices = mutableListOf<Int>()
    val killedAdherents = mutableListOf<AdherentCard>()

    override fun resolve(gameState: GameState) {
        super.resolve(gameState)
        with(gameState) {
            getOpponent(activePlayer).tableCards.forEachIndexed { loopIndex, adherentCard ->
                adherentCard.currentHealthPoints -= damage

                if (adherentCard.currentHealthPoints <= 0) {
                    removedIndices.add(loopIndex)
                }
            }
            removedIndices.forEachIndexed {loopIndex, cardIndex ->
                val removeAtIndex = cardIndex - loopIndex // positions are moved left after removing each element
                val killedAdherent = getOpponent(activePlayer).tableCards.removeAt(removeAtIndex)
                killedAdherents += killedAdherent
            }
        }
    }

    override fun rollback(gameState: GameState) {
        with(gameState) {
            killedAdherents.forEachIndexed { loopIndex, adherentCard ->
                getOpponent(activePlayer).tableCards.add(removedIndices[loopIndex], adherentCard)
            }
            getOpponent(activePlayer).tableCards.forEach {
                it.currentHealthPoints += damage
            }
        }
        removedIndices.clear()
        killedAdherents.clear()
        super.rollback(gameState)
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

