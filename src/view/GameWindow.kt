package view

import GameState
import actions.*
import gGameInstance
import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.shape.Circle
import javafx.stage.Stage
import models.Card
import models.Player
import pop
import push

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

    val historyActions: MutableList<Action> = mutableListOf()
    val undoActionsHistory: MutableList<Action> = mutableListOf()

    override fun start(stage: Stage) {
        initUI(stage)
    }

    private fun createPlayerHand(playerModel: Player): HBox {
        val playerVis = HBox(HAND_SPACING)
        playerVis.alignment = Pos.CENTER
        playerVis.minHeight = PLAYER_HAND_HEIGHT

        playerModel.handCards.forEach {
            playerVis.children.add(createCardVis(it))
        }

        return playerVis
    }

    private fun createCardVis(cardModel: Card): CardVis {
        val windowHandle = this
        val card = CardVis(cardModel)

        card.onMouseClicked = EventHandler<MouseEvent> {
            if (card.isCardActive) {
                windowHandle.onCardClicked(card)
            }
        }

        return card
    }

    private fun initPlayerHandUI() {

        gGameInstance?.let {
            handPlayer2 = createPlayerHand(it.gameState.player2)
            boardRoot.top = handPlayer2
            handPlayer1 = createPlayerHand(it.gameState.player1)
            boardRoot.bottom = handPlayer1
        }
    }

    private fun initNextTurnBtnUI(rightPanel: VBox) {
        val nextTurnBtn: Button = Button("Next Turn!")
        nextTurnBtn.setOnAction({
            onNextBtnCalled()
        })
        rightPanel.children.add(nextTurnBtn)
    }

    private fun initUndoRedoActionBtnUI(rightPanel: VBox) {
        val undoActionBtn: Button = Button("Undo action")
        undoActionBtn.setOnAction({
            onUndoActionCalled()
        })
        rightPanel.children.add(undoActionBtn)

        val redoActionBtn: Button = Button("Redo action")
        redoActionBtn.setOnAction({
            onRedoActionCalled()
        })
        rightPanel.children.add(redoActionBtn)
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

    private fun initRightPanelUI() {
        val rightPanel = VBox(5.0)

        initNextTurnBtnUI(rightPanel)
        initUndoRedoActionBtnUI(rightPanel)

        boardRoot.right = rightPanel
    }

    private fun initUI(stage: Stage) {

        boardRoot = BorderPane()

        initPlayerHandUI()
        initLeftPanelUI()
        initLeftPanelUI()
        initRightPanelUI()
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
            val state = it.gameState

            // force EndTurn action
            val endAction = EndTurn()
            // force redo all history actions
            while (undoActionsHistory.isNotEmpty()) {
                onRedoActionCalled()
            }
            endAction.resolve(state)
            historyActions.push(endAction)

            //it might be handled in other way if we choose to force update everything instead of just selected board items
            val inHandBeforeDraw = state.activePlayer.handCards.size
            it.drawCardOrGetPunished(state.activePlayer)
            val inHandAfterDraw = state.activePlayer.handCards.size
            if (inHandAfterDraw > inHandBeforeDraw) {
                getActiveHandVis(state).children.add(createCardVis(state.activePlayer.handCards.last()))
            }

            // update board
            selectedCard?.setSelected(false)
            selectedCard = null
            updateBoard(state)
        }
    }

    fun onUndoActionCalled() {
        if (historyActions.isNotEmpty()) {
            val lastItem = historyActions.pop()
            gGameInstance?.let {
                lastItem.rollback(it.gameState)
                updateBoard(it.gameState)
            }
            undoActionsHistory.push(lastItem)
        }
    }

    fun onRedoActionCalled() {
        if (undoActionsHistory.isNotEmpty()) {
            val lastItem = undoActionsHistory.pop()
            gGameInstance?.let {
                lastItem.resolve(it.gameState)
                updateBoard(it.gameState)
            }
            historyActions.push(lastItem)
        }
    }

    private fun createActionMarker(card: CardVis, cardAction: CardAction, state: GameState): Pair<Action, Circle> {
        val windowHandle = this
        lateinit var actionMarker: Pair<Action, Circle>
        when (cardAction) {
            is PlaceAdherentCard -> {
                val markerVis = Circle(boardRoot.width / 2, boardRoot.height / 2, ACTION_MARKER_RADIUS)
                actionMarker = Pair<Action, Circle>(cardAction, markerVis)
            }
            is HitOne -> {
                val otherAdherent = cardAction.targetCard
                lateinit var otherVis: CardVis
                getEnemyTableVis(state).children.forEach {
                    if ((it as CardVis).cardModel === otherAdherent) {
                        otherVis = (it as CardVis)
                    }
                }
                val boundsInScene = otherVis.localToScene(otherVis.boundsInLocal)
                val markerVis = Circle(boundsInScene.minX + boundsInScene.width / 2, boundsInScene.minY + boundsInScene.height / 2, ACTION_MARKER_RADIUS)
                actionMarker = Pair<Action, Circle>(cardAction, markerVis)
            }
            is HitAllEnemies -> {
                val enemyTable = getEnemyTableVis(state)
                val boundsInScene = enemyTable.localToScene(enemyTable.boundsInLocal)
                val markerVis = Circle(boundsInScene.minX + boundsInScene.width / 2, boundsInScene.minY + boundsInScene.height / 2, ACTION_MARKER_RADIUS)
                actionMarker = Pair<Action, Circle>(cardAction, markerVis)
            }
            is HealAll -> {

                val markerVis = Circle(boardRoot.width / 2, boardRoot.height / 2, ACTION_MARKER_RADIUS)
                actionMarker = Pair<Action, Circle>(cardAction, markerVis)
            }
            is FightAnotherAdherent -> {
                val otherAdherent = cardAction.targetCard
                lateinit var otherVis: CardVis
                getEnemyTableVis(state).children.forEach {
                    if ((it as CardVis).cardModel === otherAdherent) {
                        otherVis = (it as CardVis)
                    }
                }
                val boundsInScene = otherVis.localToScene(otherVis.boundsInLocal)
                val markerVis = Circle(boundsInScene.minX + boundsInScene.width / 2, boundsInScene.minY + boundsInScene.height / 2, ACTION_MARKER_RADIUS)
                actionMarker = Pair<Action, Circle>(cardAction, markerVis)
            }
            is FightEnemyHero -> {
                val enemyPlayerVis: PlayerVis = getEnemyPlayerVis(state)

                val boundsInScene = enemyPlayerVis.localToScene(enemyPlayerVis.boundsInLocal)
                val markerVis = Circle(boundsInScene.minX + boundsInScene.width / 2, boundsInScene.minY + boundsInScene.height / 2, ACTION_MARKER_RADIUS)
                actionMarker = Pair<Action, Circle>(cardAction, markerVis)
            }
            else -> {
                throw IllegalStateException("Action: $cardAction  triggered by Card: ${card.cardModel}  is not handled in visualisation")
            }
        }

        actionMarker.second.onMouseClicked = EventHandler<MouseEvent> {
            cardAction.resolve(state)
            windowHandle.onActionPlayed(actionMarker.first)
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
            println(actions)
            actions.forEach {
                val actionMarker = createActionMarker(card, it, state)
                availableActionsVis.add(actionMarker)
                boardRoot.children.add(actionMarker.second)
            }
        }
    }

    fun onActionPlayed(actionCalled: Action) {
        gGameInstance?.let {
            clearActionMarkers()
            selectedCard?.setSelected(false)
            historyActions.push(actionCalled)
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

        updatePlayerCards(player1Vis, handPlayer1, tablePlayer1)
        updatePlayerCards(player2Vis, handPlayer2, tablePlayer2)

        markActivePlayer(state)
        markActiveCards(state)
    }

    private fun updatePlayerCards(playerVis: PlayerVis, playerHandVis: HBox, playerTableVis: HBox) {
        val playerHand = playerVis.player.handCards
        val playerTable = playerVis.player.tableCards

        playerHandVis.children.clear()
        playerHand.forEach {
            playerHandVis.children.add(createCardVis(it))
        }

        playerTableVis.children.clear()
        playerTable.forEach {
            playerTableVis.children.add(createCardVis(it))
        }
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
        player1Vis.setActive(state.activePlayer === state.player1)
        player2Vis.setActive(state.activePlayer === state.player2)
    }

    private fun getActivePlayerVis(state: GameState): PlayerVis {
        return if (state.activePlayer === state.player1) player1Vis else player2Vis
    }

    private fun getEnemyPlayerVis(state: GameState): PlayerVis {
        return if (state.activePlayer !== state.player1) player1Vis else player2Vis
    }

    private fun getActiveHandVis(state: GameState): HBox {
        return if (state.activePlayer === state.player1) handPlayer1 else handPlayer2
    }

    private fun getEnemyHandVis(state: GameState): HBox {
        return if (state.activePlayer !== state.player1) handPlayer1 else handPlayer2
    }

    private fun getActiveTableVis(state: GameState): HBox {
        return if (state.activePlayer === state.player1) tablePlayer1 else tablePlayer2
    }

    private fun getEnemyTableVis(state: GameState): HBox {
        return if (state.activePlayer !== state.player1) tablePlayer1 else tablePlayer2
    }

    fun launchWindow(args: Array<String>) {
        Application.launch(*args)
    }
}
