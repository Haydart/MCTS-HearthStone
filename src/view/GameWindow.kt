package view

import gGameInstance
import GameState
import actions.*
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.event.EventHandler;
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*
import javafx.event.ActionEvent
import javafx.scene.shape.Circle
import models.AdherentCard
import models.Player

const val PLAYER_TABLE_HEIGHT = 150.0
const val PLAYER_HAND_HEIGHT = 100.0
const val SCENE_WIDTH = 900.0
const val SCENE_HEIGHT = 600.0
const val HAND_SPACING = 5.0
const val TABLE_SPACING = 25.0
const val ACTION_MARKER_RADIUS = 20.0

class GameWindow: Application() {

    lateinit var boardRoot: BorderPane
    lateinit var handPlayer1: HBox
    lateinit var handPlayer2: HBox
    lateinit var tablePlayer1: HBox
    lateinit var tablePlayer2: HBox
    lateinit var player1Vis: PlayerVis
    lateinit var player2Vis: PlayerVis

    var selectedCard: CardVis? = null
    var availableActionsVis: MutableList<Pair<Action, Circle>> = mutableListOf()

    override fun start(stage: Stage) {
        initUI(stage)
    }

    private fun createPlayerHand(playerModel: Player): HBox {
        val playerVis = HBox(HAND_SPACING)
        playerVis.alignment = Pos.CENTER
        playerVis.minHeight = PLAYER_HAND_HEIGHT

        val windowHandle = this
        playerModel.handCards.forEach {
            val card = CardVis(it)

            card.onMouseClicked = EventHandler<MouseEvent> {
                if (card.isCardActive) {
                    windowHandle.onCardClicked(card)
                }
            }

            playerVis.children.add(card)
        }

        return playerVis
    }

    private fun initPlayerHandUI() {

        gGameInstance?.let {
            handPlayer2 = createPlayerHand(it.gameState.player2)
            boardRoot.top = handPlayer2
            handPlayer1 = createPlayerHand(it.gameState.player1)
            boardRoot.bottom = handPlayer1
        }
    }

    private fun initNextTurnBtnUI() {
        val nextTurnBtn: Button = Button("Next Turn!")
        nextTurnBtn.setOnAction({ event: ActionEvent ->
            onNextBtnCalled()
        })
        boardRoot.right = nextTurnBtn
    }

    private fun initTableUI() {
        val table = VBox(5.0)
        boardRoot.center = table
        table.alignment = Pos.CENTER

        tablePlayer2 = HBox(TABLE_SPACING)
        tablePlayer2.minHeight = PLAYER_TABLE_HEIGHT
        tablePlayer2.alignment = Pos.CENTER
        table.children.add(tablePlayer2)

        tablePlayer1 = HBox(TABLE_SPACING)
        tablePlayer1.minHeight = PLAYER_TABLE_HEIGHT
        tablePlayer1.alignment = Pos.CENTER
        table.children.add(tablePlayer1)
    }

    private fun initLeftPanelUI() {
        gGameInstance?.let {
            val leftPanel = BorderPane()
            boardRoot.left = leftPanel
            player1Vis = PlayerVis(it.gameState.player1)
            leftPanel.bottom = player1Vis
            player2Vis = PlayerVis(it.gameState.player2)
            leftPanel.top = player2Vis
        }
    }

    private fun initUI(stage: Stage) {

        boardRoot = BorderPane()

        initPlayerHandUI()
        initLeftPanelUI()
        initNextTurnBtnUI()
        initTableUI()

        val scene = Scene(boardRoot, SCENE_WIDTH, SCENE_HEIGHT)

        gGameInstance?.let{

            updateBoard(it.gameState)
        }

        stage.title = "HerfStoun"
        stage.scene = scene
        stage.show()
    }

    fun onNextBtnCalled() {
        gGameInstance?.let {

            // force EndTurn action
            val endAction = EndTurn()
            endAction.resolve(it.gameState)

            // force change active player (possibly may take place in EndTurn action)
            it.gameState.activePlayer = it.gameState.getOpponent(it.gameState.activePlayer)

            // force increment game turn (possibly may take place in EndTurn action)
            it.gameState.turnNumber++

            // force active player mana update (possibly may take place in EndTurn action)
            it.gameState.activePlayer.mana = (it.gameState.turnNumber - 1) / 2 + 1

            // update board
            selectedCard?.setSelected(false)
            selectedCard = null
            updateBoard(it.gameState)
        }
    }

    private fun createActionMarker(card: CardVis, cardAction: CardAction, state: GameState): Pair<Action, Circle> {
        val windowHandle = this
        lateinit var actionMarker: Pair<Action, Circle>
        when (cardAction) {
            is PlaceAdherentCard -> {
                val markerVis = Circle(boardRoot.width / 2, boardRoot.height / 2, ACTION_MARKER_RADIUS)
                actionMarker = Pair<Action, Circle>(cardAction, markerVis)
                markerVis.onMouseClicked = EventHandler<MouseEvent> {
                    cardAction.resolve(state)
                    windowHandle.deployCard(card)
                    windowHandle.onActionPlayed()
                }
            }
            is HitOne -> {
                val otherAdherent = cardAction.targetCard
                lateinit var otherVis: CardVis
                getEnemyTableVis(state).children.forEach {
                    if ((it as CardVis).cardModel == otherAdherent) {
                        otherVis = it
                    }
                }
                val boundsInScene = otherVis.localToScene(otherVis.getBoundsInLocal())
                val markerVis = Circle(boundsInScene.minX + boundsInScene.width / 2, boundsInScene.minY + boundsInScene.height / 2, ACTION_MARKER_RADIUS)
                actionMarker = Pair<Action, Circle>(cardAction, markerVis)

                markerVis.onMouseClicked = EventHandler<MouseEvent> {
                    cardAction.resolve(state)
                    windowHandle.discardCard(card)
                    otherVis.update()
                    if (otherAdherent.currentHealthPoints <= 0) {
                        getEnemyTableVis(state).children.remove(otherVis)
                    }
                    windowHandle.onActionPlayed()
                }
            }
            is HitAllEnemies -> {
                val enemyTable = getEnemyTableVis(state)
                val boundsInScene = enemyTable.localToScene(enemyTable.getBoundsInLocal())
                val markerVis = Circle(boundsInScene.minX + boundsInScene.width / 2, boundsInScene.minY + boundsInScene.height / 2, ACTION_MARKER_RADIUS)
                actionMarker = Pair<Action, Circle>(cardAction, markerVis)

                markerVis.onMouseClicked = EventHandler<MouseEvent> {
                    cardAction.resolve(state)
                    windowHandle.discardCard(card)

                    enemyTable.children.forEach {
                        (it as CardVis).update()
                    }

                    enemyTable.children.removeIf {
                        ((it as CardVis).cardModel as AdherentCard).currentHealthPoints <= 0
                    }

                    windowHandle.onActionPlayed()
                }
            }
            is HealAll -> {

                val markerVis = Circle(boardRoot.width / 2, boardRoot.height / 2, ACTION_MARKER_RADIUS)
                actionMarker = Pair<Action, Circle>(cardAction, markerVis)

                markerVis.onMouseClicked = EventHandler<MouseEvent> {
                    cardAction.resolve(state)
                    windowHandle.discardCard(card)

                    handPlayer1.children.forEach {
                        (it as CardVis).update()
                    }

                    handPlayer2.children.forEach {
                        (it as CardVis).update()
                    }

                    windowHandle.onActionPlayed()
                }
            }
            is FightAnotherAdherent -> {
                val otherAdherent = cardAction.targetCard
                lateinit var otherVis: CardVis
                getEnemyTableVis(state).children.forEach {
                    if ((it as CardVis).cardModel == otherAdherent) {
                        otherVis = it
                    }
                }
                val boundsInScene = otherVis.localToScene(otherVis.getBoundsInLocal())
                val markerVis = Circle(boundsInScene.minX + boundsInScene.width / 2, boundsInScene.minY + boundsInScene.height / 2, ACTION_MARKER_RADIUS)
                actionMarker = Pair<Action, Circle>(cardAction, markerVis)

                markerVis.onMouseClicked = EventHandler<MouseEvent> {
                    cardAction.resolve(state)

                    otherVis.update()
                    card.update()

                    if (otherAdherent.currentHealthPoints <= 0) {
                        getEnemyTableVis(state).children.remove(otherVis)
                    }

                    if ((card.cardModel as AdherentCard).currentHealthPoints <= 0) {
                        getActiveTableVis(state).children.remove(card)
                    }

                    windowHandle.onActionPlayed()
                }
            }
            else -> {
                throw IllegalStateException("Action: $cardAction  triggered by Card: ${card.cardModel}  is not handled in visualisation")
            }
        }

        return actionMarker
    }

    private fun onCardClicked(card: CardVis) {
        selectedCard?.setSelected(false)
        selectedCard = card
        card.setSelected(true)

        gGameInstance?.let {
            val actions = card.cardModel.getActionsFun(card.cardModel, it.gameState.activePlayer, it.gameState.getOpponent(it.gameState.activePlayer))
            val state = it.gameState
            actions.forEach {
                val actionMarker = createActionMarker(card, it, state)
                availableActionsVis.add(actionMarker)
                boardRoot.children.add(actionMarker.second)
            }
        }
    }

    fun onActionPlayed() {
        gGameInstance?.let {
            clearActionMarkers()
            selectedCard?.setSelected(false)
            updateBoard(it.gameState)
        }
    }

    fun clearActionMarkers() {
        availableActionsVis.forEach {
            boardRoot.children.remove(it.second)
        }
        availableActionsVis.clear()
    }

    fun updateBoard(state: GameState) {

        player1Vis.update()
        player2Vis.update()

        markActivePlayer(state)
        markActiveCards(state)
    }

    fun deployCard(card: CardVis) {
        gGameInstance?.let {
            getActiveHandVis(it.gameState).children.remove(card)
            getActiveTableVis(it.gameState).children.add(card)
        }
    }

    fun discardCard(card: CardVis) {
        gGameInstance?.let {
            getActiveHandVis(it.gameState).children.remove(card)
        }
    }

    private fun markActiveCards(state: GameState) {
        getEnemyHandVis(state).children.forEach {
            val vis: CardVis = (it as CardVis)
            vis.setActive(false)
        }

        getEnemyTableVis(state).children.forEach {
            val vis: CardVis = (it as CardVis)
            vis.setActive(false)
        }

        getActiveHandVis(state).children.forEach {
            val vis: CardVis = (it as CardVis)

            vis.setActive(vis.cardModel.manaCost <= state.activePlayer.mana &&
                    vis.cardModel.getActionsFun(vis.cardModel, state.activePlayer, state.getOpponent(state.activePlayer)).isNotEmpty())
        }

        getActiveTableVis(state).children.forEach {
            val vis: CardVis = (it as CardVis)

            vis.setActive(vis.cardModel.getActionsFun(vis.cardModel, state.activePlayer, state.getOpponent(state.activePlayer)).isNotEmpty())
        }
    }

    private fun markActivePlayer(state: GameState) {
        player1Vis.setActive(state.activePlayer == state.player1)
        player2Vis.setActive(state.activePlayer == state.player2)
    }

    private fun getActiveHandVis(state: GameState): HBox {
        return if (state.activePlayer == state.player1) handPlayer1 else handPlayer2
    }

    private fun getEnemyHandVis(state: GameState): HBox {
        return if (state.activePlayer != state.player1) handPlayer1 else handPlayer2
    }

    private fun getActiveTableVis(state: GameState): HBox {
        return if (state.activePlayer == state.player1) tablePlayer1 else tablePlayer2
    }

    private fun getEnemyTableVis(state: GameState): HBox {
        return if (state.activePlayer != state.player1) tablePlayer1 else tablePlayer2
    }

    fun launchWindow(args: Array<String>) {
        Application.launch(*args)
    }
}
