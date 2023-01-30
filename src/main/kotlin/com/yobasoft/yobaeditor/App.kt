package com.yobasoft.yobaeditor

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage

class Window {
    private var vBox = VBox()
    private val width = 1920.0
    private val height = 1080.0
    private val aPaneInSPane = AnchorPane()
    private val scrollPane = ZoomableScrollPane(aPaneInSPane)

    fun start(): Scene {
        val hBox = HBox()
        fun createButton(str: String) {
            val button = Button(str)
            button.style = "-fx-border-color: black; -fx-border-image-width: 8;"
            button.font = Font.font("Verdana", 20.0)
            button.onAction = EventHandler {
                val node = getNode(str)
                node.layoutX += 1000.0
                node.layoutY += 500.0
                node.onMouseEntered = EventHandler { e: MouseEvent ->
                    scrollPane.isPannable = false
                }
                node.onMouseExited = EventHandler { e: MouseEvent ->
                    scrollPane.isPannable = true
                }
                aPaneInSPane.children.add(node)
            }
            hBox.children.add(button)
        }

        createButton("Float")
        createButton("Int")
        createButton("String")
        createButton("Start")
        createButton("Add text")
        createButton("Add image")
        createButton("Gray")
        createButton("Brightness")
        createButton("Sepia")
        createButton("Invert")
        createButton("Blur")
        createButton("Move")
        createButton("Scale")
        createButton("Rotate")


        val end = EndNode()
        val start = StartNode()
        start.layoutX += 10
        start.layoutY += 50
        start.lookup("#deleteButton").isVisible = false
        end.layoutX = width - end.rootPane!!.prefWidth - 10
        end.layoutY = height / 2
        start.onMouseEntered = EventHandler { e: MouseEvent ->
            scrollPane.isPannable = false
        }
        start.onMouseExited = EventHandler { e: MouseEvent ->
            scrollPane.isPannable = true
        }
        end.onMouseEntered = EventHandler { e: MouseEvent ->
            scrollPane.isPannable = false
        }
        end.onMouseExited = EventHandler { e: MouseEvent ->
            scrollPane.isPannable = true
        }
        aPaneInSPane.children.add(end)
        aPaneInSPane.children.add(start)
        aPaneInSPane.prefWidth = 19200.0
        aPaneInSPane.prefHeight = 19200.0

        hBox.layoutX += 10
        hBox.spacing = 5.0
        vBox.children.add(hBox)
        vBox.children.add(scrollPane)

        return Scene(vBox, width, height)
    }

    private fun getNode(str: String): DragNode {
        return when (str) {
            "Float" -> FloatNode()
            "Int" -> IntNode()
            "String" -> StringNode()
            "Start" -> StartNode()
            "Add text" -> AddTextNode()
            "Add image" -> AddImgNode()
            "Gray" -> GrayNode()
            "Brightness" -> BrightnessNode()
            "Sepia" -> SepiaNode()
            "Invert" -> InvertNode()
            "Blur" -> BlurNode()
            "Move" -> MoveNode()
            "Scale" -> ScaleNode()
            "Rotate" -> RotateNode()
            else -> IntNode()
        }
    }


}


class App : Application() {
    override fun start(primaryStage: Stage) {
        nu.pattern.OpenCV.loadLocally()
        primaryStage.isMaximized = true
        primaryStage.scene = Window().start()
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(App::class.java)
        }
    }
}