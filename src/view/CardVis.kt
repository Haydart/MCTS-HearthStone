package view

import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import models.Card

class CardVis(var cardModel: Card) : StackPane(){

    init {
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

        this.children.add(rect)

        val l = Label(cardModel.name)
        l.font = Font.font("Serif", FontWeight.NORMAL, 10.0)

        this.children.add(l)
    }
}