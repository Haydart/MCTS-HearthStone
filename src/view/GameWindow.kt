package view

import gGameInstance
import GameState
import actions.Action
import actions.DrawCard
import actions.FightAnotherAdherent
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

    private fun initUI(stage: Stage) {

        boardRoot = BorderPane()

        val battleground = VBox(5.0)
        boardRoot.center = battleground

        handPlayer2 = HBox(5.0)
        handPlayer2.alignment = Pos.CENTER
        boardRoot.top = handPlayer2
        boardRoot.top.minHeight(150.0)

        handPlayer1 = HBox(5.0)
        handPlayer1.alignment = Pos.CENTER
        boardRoot.bottom = handPlayer1
        boardRoot.bottom.minHeight(150.0)

        gGameInstance?.let {
            val leftPanel = BorderPane()
            boardRoot.left = leftPanel
            player1Vis = PlayerVis(it.gameState.player1)
            leftPanel.bottom = player1Vis
            player2Vis = PlayerVis(it.gameState.player2)
            leftPanel.top = player2Vis
        }

        val nextTurnBtn: Button = Button("Next Turn!")
        nextTurnBtn.setOnAction({ event: ActionEvent ->
            onNextBtnCalled()
        })
        boardRoot.right = nextTurnBtn

        val scene = Scene(boardRoot, 900.0, 600.0)

        tablePlayer2 = HBox(25.0)
        battleground.children.add(tablePlayer2)
        battleground.alignment = Pos.CENTER
        tablePlayer2.alignment = Pos.CENTER

        val windowHandle = this
        gGameInstance?.let {
            val gameState = it.gameState
            gameState.player1.handCards.forEach {
                val card = CardVis(it)

                card.setOnMouseClicked(object : EventHandler<MouseEvent> {
                    override fun handle(event: MouseEvent?) {
                        if (card.isCardActive) {
                            windowHandle.onCardClicked(card)
                        }
                    }
                })

                handPlayer1.children.add(card)
            }
        }

        tablePlayer1 = HBox(25.0)
        tablePlayer1.alignment = Pos.CENTER
        battleground.children.add(tablePlayer1)

        gGameInstance?.let {
            val gameState = it.gameState
            gameState.player2.handCards.forEach {
                val card = CardVis(it)

                card.setOnMouseClicked(object : EventHandler<MouseEvent> {
                    override fun handle(event: MouseEvent?) {
                        if (card.isCardActive) {
                            windowHandle.onCardClicked(card)
                        }
                    }
                })

                handPlayer2.children.add(card)
            }
        }

        gGameInstance?.let{

            updateBoard(it.gameState)
        }

        stage.title = "HerfStoun"
        stage.scene = scene
        stage.show()
    }

    fun onNextBtnCalled() {
        gGameInstance?.let {
            it.gameState.activePlayer = it.gameState.getOpponent(it.gameState.activePlayer)
            it.gameState.activePlayer.mana += 1
            selectedCard?.setSelected(false)
            selectedCard = null
            updateBoard(it.gameState)
        }
    }

    fun onCardClicked(card: CardVis) {
        selectedCard?.setSelected(false)
        selectedCard = card
        card.setSelected(true)

        val windowHandle = this

        gGameInstance?.let {
            val actions = card.cardModel.getActionsFun(card.cardModel, it.gameState.activePlayer, it.gameState.getOpponent(it.gameState.activePlayer))
            val state = it.gameState
            actions.forEach {
                if (it is DrawCard) {
                    val markerVis = Circle(boardRoot.width / 2, boardRoot.height / 2, 20.0)
                    val marker = Pair<Action, Circle>(it, markerVis)
                    markerVis.setOnMouseClicked(object : EventHandler<MouseEvent> {
                        override fun handle(event: MouseEvent?) {
                            it.resolve(state)
                            println("ActionTriggered")
                            windowHandle.deployCard(card)
                            windowHandle.clearActionMarkers()
                            windowHandle.updateBoard(state)
                        }
                    })
                    availableActionsVis.add(marker)
                    boardRoot.children.add(marker.second)
                }
                else if (it is FightAnotherAdherent) {
                    val otherAdherent = (it as FightAnotherAdherent).targetCard
                    lateinit var otherVis: CardVis
                    getEnemyTableVis(state).children.forEach {
                        if ((it as CardVis).cardModel == otherAdherent) {
                            otherVis = it
                        }
                    }
                    val boundsInScene = otherVis.localToScene(otherVis.getBoundsInLocal())
                    val markerVis = Circle(boundsInScene.minX + boundsInScene.width / 2, boundsInScene.minY + boundsInScene.height / 2, 20.0)
                    val marker = Pair<Action, Circle>(it, markerVis)

                    markerVis.setOnMouseClicked(object : EventHandler<MouseEvent> {
                        override fun handle(event: MouseEvent?) {
                            it.resolve(state)
                            println("ActionTriggered")
                            windowHandle.clearActionMarkers()
                            windowHandle.updateBoard(state)
                        }
                    })

                    availableActionsVis.add(marker)
                    boardRoot.children.add(marker.second)
                }
            }
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

    private fun markActiveCards(state: GameState) {
        getEnemyHandVis(state).children.forEach {
            val vis: CardVis = (it as CardVis)
            vis.setActive(false)
        }

        getEnemyHandVis(state).children.forEach {
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

            vis.setActive(vis.cardModel.manaCost <= state.activePlayer.mana &&
                    vis.cardModel.getActionsFun(vis.cardModel, state.activePlayer, state.getOpponent(state.activePlayer)).isNotEmpty())
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
