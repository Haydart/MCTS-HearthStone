package view

import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import models.Player

class PlayerVis(val player: Player) : StackPane(){

    lateinit var textLabel: Label
    lateinit var background: Rectangle

    init {
        background = Rectangle(0.0, 0.0, 80.0, 80.0)
        background.fill = Color.LIGHTBLUE

        this.children.add(background)

        textLabel = Label(getLabelText())
        textLabel.font = Font.font("Serif", FontWeight.NORMAL, 10.0)

        this.children.add(textLabel)
    }

    fun update() {
        textLabel.text = getLabelText()
    }

    fun getLabelText(): String {
        var newText: String = "${player.name}\nHealth:${player.healthPoints}\nMana:${player.mana}"

        return newText
    }

    fun setActive(isActive: Boolean) {
        background.fill = if (isActive) Color.LIGHTGREEN else Color.LIGHTBLUE
    }
}