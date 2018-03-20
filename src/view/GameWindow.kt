package view

import GameState
import actions.*
import gGameInstance
import greedyagents.Agent
import greedyagents.AggressiveGreedyAgent
import greedyagents.ControllingGreedyAgent
import greedyagents.RandomGreedyAgent
import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.RadioButton
import javafx.scene.control.ToggleGroup
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.shape.Circle
import javafx.scene.text.Text
import javafx.stage.Stage
import mctsagent.ProbabilisticAgent
import models.Card
import models.Player
import pop
import push
import kotlin.reflect.full.createInstance


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
    lateinit var nextTurnBtn: Button
    lateinit var startGameBtn: Button

    var selectedCard: CardVis? = null
    var availableActionsVis: MutableList<Pair<Action, Circle>> = mutableListOf()

    private val historyActions: MutableList<Action> = mutableListOf()
    private val undoActionsHistory: MutableList<Action> = mutableListOf()

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

    private fun createAgentSelection(playerVis: PlayerVis): VBox {
        val agentSelectionBox = VBox()
        agentSelectionBox.children.add(Text(playerVis.player.name + " Model"))
        val toggleGroup = ToggleGroup()

        val option1 = RadioButton("Player")
        option1.toggleGroup = toggleGroup
        option1.isSelected = true
        agentSelectionBox.children.add(option1)

        val option2 = RadioButton(RandomGreedyAgent::class.simpleName)
        option2.toggleGroup = toggleGroup
        option2.isSelected = false
        agentSelectionBox.children.add(option2)

        val option3 = RadioButton(ControllingGreedyAgent::class.simpleName)
        option3.toggleGroup = toggleGroup
        option3.isSelected = false
        agentSelectionBox.children.add(option3)

        val option4 = RadioButton(AggressiveGreedyAgent::class.simpleName)
        option4.toggleGroup = toggleGroup
        option4.isSelected = false
        agentSelectionBox.children.add(option4)

        val option5 = RadioButton(ProbabilisticAgent::class.simpleName)
        option5.toggleGroup = toggleGroup
        option5.isSelected = false
        agentSelectionBox.children.add(option5)

        toggleGroup.selectedToggleProperty().addListener { _, _, new_toggle ->
            if (toggleGroup.selectedToggle != null) {
                gGameInstance?.let {
                    val controllerClassName = (new_toggle as RadioButton).text

                    var newController: Agent? = null
                    when (controllerClassName) {
                        "Player" -> {
                            // do nothing
                        }
                        ProbabilisticAgent::class.simpleName -> {
                            val kClass = Class.forName("mctsagent." + controllerClassName).kotlin
                            println(kClass)
                            newController = (kClass.createInstance() as Agent)
                        }
                        else -> {
                            val kClass = Class.forName("greedyagents." + controllerClassName).kotlin
                            println(kClass)
                            newController = (kClass.createInstance() as Agent)
                        }
                    }

                    it.setPlayerController(playerVis.player, newController)
                }
            }
        }

        return agentSelectionBox
    }

    private fun initPlayerHandUI() {

        gGameInstance?.let {
            handPlayer2 = createPlayerHand(it.getCurrentState().player2)
            boardRoot.top = handPlayer2
            handPlayer1 = createPlayerHand(it.getCurrentState().player1)
            boardRoot.bottom = handPlayer1
        }
    }

    private fun initNextTurnBtnUI(rightPanel: VBox) {
        nextTurnBtn = Button("Next Turn!")
        nextTurnBtn.setOnAction({
            onNextBtnCalled()
        })
        nextTurnBtn.isDisable = true
        rightPanel.children.add(nextTurnBtn)
    }

    private fun initStartGameBtnUI(rightPanel: VBox) {
        startGameBtn = Button("Start!")
        startGameBtn.setOnAction({
            onStartGameCalled()
        })
        rightPanel.children.add(startGameBtn)
    }

    private fun initUndoRedoActionBtnUI(rightPanel: VBox) {
        val undoActionBtn = Button("Undo action")
        undoActionBtn.setOnAction({
            onUndoActionCalled()
        })
        rightPanel.children.add(undoActionBtn)

        val redoActionBtn = Button("Redo action")
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
            player1Vis = PlayerVis(it.getCurrentState().player1)
            leftPanel.bottom = player1Vis
            player2Vis = PlayerVis(it.getCurrentState().player2)
            leftPanel.top = player2Vis

            val playerControllerSelection = VBox(25.0)
            playerControllerSelection.children.add(createAgentSelection(player2Vis))
            playerControllerSelection.children.add(createAgentSelection(player1Vis))
            leftPanel.center = playerControllerSelection
        }
    }

    private fun initRightPanelUI() {
        val rightPanel = VBox(5.0)

        initStartGameBtnUI(rightPanel)
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

            it.setPlayerController(it.getCurrentState().player1, null)
            it.setPlayerController(it.getCurrentState().player2, null)
            updateBoard(it.getCurrentState())
        }

        stage.title = "HerfStoun"
        stage.scene = scene
        stage.show()
    }

    private fun simulateCardDraw() {
        //it might be handled in other way if we choose to force update everything instead of just selected board items
        gGameInstance?.let {
            val state = it.getCurrentState()
            val inHandBeforeDraw = state.activePlayer.handCards.size
            it.drawCardOrGetPunished(state.activePlayer)
            val inHandAfterDraw = state.activePlayer.handCards.size
            if (inHandAfterDraw > inHandBeforeDraw) {
                getActiveHandVis(state).children.add(createCardVis(state.activePlayer.handCards.last()))
            }
        }
    }

    fun onStartGameCalled() {
        gGameInstance?.let {
            startGameBtn.isDisable = true
            nextTurnBtn.isDisable = false
            val state = it.getCurrentState()
            simulateCardDraw()

            it.getActivePlayerController(state)?.performTurn(state)?.forEach { performedAction ->
                historyActions.push(performedAction)
            }

            // update board
            selectedCard?.setSelected(false)
            selectedCard = null
            updateBoard(state)
        }
    }

    fun onNextBtnCalled() {
        gGameInstance?.let {
            val state = it.getCurrentState()

            val activeController = it.getActivePlayerController(it.getCurrentState())
            if (activeController != null) {
                activeController.performTurn(it.getCurrentState()).forEach {performedAction ->
                    historyActions.push(performedAction)
                }
            } else {
                // force EndTurn action
                val endAction = EndTurn()
                // force redo all history actions
                while (undoActionsHistory.isNotEmpty()) {
                    onRedoActionCalled()
                }
                endAction.resolve(state)
                historyActions.push(endAction)
            }

            simulateCardDraw()

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
                lastItem.rollback(it.getCurrentState())
                updateBoard(it.getCurrentState())
            }
            undoActionsHistory.push(lastItem)
        }
    }

    fun onRedoActionCalled() {
        if (undoActionsHistory.isNotEmpty()) {
            val lastItem = undoActionsHistory.pop()
            gGameInstance?.let {
                lastItem.resolve(it.getCurrentState())
                updateBoard(it.getCurrentState())
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
            val actions = card.cardModel.getActionsFun(card.cardModel, it.getCurrentState().activePlayer, it.getCurrentState().getOpponent(it.getCurrentState().activePlayer))
            val state = it.getCurrentState()
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
            updateBoard(it.getCurrentState())
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
            getActiveHandVis(it.getCurrentState()).children.remove(card)
            getActiveTableVis(it.getCurrentState()).children.add(card)
        }
    }

    fun discardCard(card: CardVis) {
        gGameInstance?.let {
            getActiveHandVis(it.getCurrentState()).children.remove(card)
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
