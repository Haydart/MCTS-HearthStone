package view

import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import models.AdherentCard
import models.Card

const val CARD_SIZE = 80.0
const val CARD_TEXT_SIZE = 10.0

class CardVis(var cardModel: Card) : StackPane(){

    var isCardActive: Boolean = false
    var isCardSelected: Boolean = false
    lateinit var textLabel: Label
    lateinit var background: Rectangle

    init {
        background = Rectangle(0.0, 0.0, CARD_SIZE, CARD_SIZE)
        background.fill = Color.LIGHTBLUE

        this.children.add(background)

        textLabel = Label(getLabelText())
        textLabel.font = Font.font("Serif", FontWeight.NORMAL, CARD_TEXT_SIZE)

        this.children.add(textLabel)
    }

    fun update() {
        textLabel.text = getLabelText()
        background.fill = if (isCardSelected) Color.GREEN else if (isCardActive) Color.LIGHTGREEN else Color.LIGHTBLUE
    }

    fun getLabelText(): String {
        var newText: String = "${cardModel.name}\nCost: ${cardModel.manaCost}"
        if (cardModel is AdherentCard) {
            newText += "\nHealth:${(cardModel as AdherentCard).currentHealthPoints}/${(cardModel as AdherentCard).maxHealthPoints}"
            newText += "\nAttack:${(cardModel as AdherentCard).attackStrength}"
        }
        return newText
    }

    fun setActive(isActive: Boolean) {
        isCardActive = isActive
        update()
    }

    fun setSelected(isSelected: Boolean) {
        isCardSelected = isSelected
        update()
    }
}