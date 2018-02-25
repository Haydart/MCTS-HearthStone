package view

import gGameInstance
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Stage


import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle;


class GameWindow: Application() {

    override fun start(stage: Stage) {
        initUI(stage)
    }

    private fun initUI(stage: Stage) {

        //val root = StackPane()
        val root = VBox(5.0)

        val scene = Scene(root, 900.0, 600.0)

        val lbl = Label("Simple JavaFX application.")
        lbl.font = Font.font("Serif", FontWeight.NORMAL, 20.0)
        root.children.add(lbl)

        val horBox = HBox(25.0)
        root.children.add(horBox)



        gGameInstance?.let {

            val isActive = it.gameState.activePlayer == it.gameState.player1
            it.gameState.player1.handCards.forEach {

                val l = Label(it.name)
                l.font = Font.font("Serif", if (isActive) FontWeight.NORMAL else FontWeight.EXTRA_BOLD, 10.0)

                val stack = StackPane()

                val rect = Rectangle(0.0, 0.0, 80.0, 80.0)
                rect.fill = Color.LIGHTBLUE

                rect.setOnMouseClicked(object : EventHandler<MouseEvent> {
                    override fun handle(event: MouseEvent?) {
                        println("aaa")
                        rect.y += 50.0
                        rect.width += 50
                        rect.height += 50
                    }
                })

                stack.children.add(rect)
                stack.children.add(l)

                horBox.children.addAll(stack)

            }
        }


        val hor2Box = HBox(25.0)
        root.children.add(hor2Box)


        gGameInstance?.let {

            val isActive = it.gameState.activePlayer == it.gameState.player2
            it.gameState.player2.handCards.forEach {
                val l = Label(it.name)
                l.font = Font.font("Serif", if (isActive) FontWeight.NORMAL else FontWeight.EXTRA_BOLD, 10.0)
                hor2Box.children.add(l)

            }
        }

        stage.title = "Simple application"
        stage.scene = scene
        stage.show()
    }

    fun launchWindow(args: Array<String>) {
        Application.launch(*args)
    }
}
