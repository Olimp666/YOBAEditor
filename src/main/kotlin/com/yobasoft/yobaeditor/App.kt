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
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.hildan.fxgson.FxGson
import java.io.File
import java.io.IOException


class Window {
    private var vBox = VBox()
    private val width_ = 1920.0
    private val height_ = 1080.0
    private var aPaneInSPane = AnchorPane().apply {
        prefWidth = 19200.0
        prefHeight = 19200.0
    }
    private val scrollPane = ZoomableScrollPane(aPaneInSPane)
    private var lastX = 20.0
    private var lastY = 50.0

    fun start(): Scene {
        val hBox = HBox().apply {
            layoutX += 10
            spacing = 5.0
        }

        fun createButton(str: String) {
            val button = Button(str)
            button.style = "-fx-border-color: black; -fx-border-image-width: 8;"
            button.font = Font.font("Verdana", 20.0)
            button.onAction = EventHandler {
                lastX = aPaneInSPane.children.last().layoutX
                lastY = aPaneInSPane.children.last().layoutY
                val node = getNode(str)
                node.layoutX += lastX + 50.0
                node.layoutY += lastY + 100.0
                node.onMouseEntered = EventHandler {
                    scrollPane.isPannable = false
                }
                node.onMouseExited = EventHandler {
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

        val buttonHBox = HBox().apply {
            spacing = 5.0
        }
        val loadButton = Button().apply {
            style = "-fx-border-color: black; -fx-border-image-width: 8; -fx-text-fill: blue;"
            font = Font.font("Verdana", 20.0)
            text = "Load"
            isVisible = false
            onAction = EventHandler {
                val fileChooser = FileChooser()
                fileChooser.title = "Open Nodes"
                fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Node Files", "*.ns"))
                val dir = fileChooser.showOpenDialog(scene.window)
                if (dir != null) {
                    try {
                        val file = File(dir.toURI())
                        if (!file.exists()) return@EventHandler

                        val gson = FxGson.coreBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                        val data = gson.fromJson(file.readText(), Serializer.Saved::class.java)
                        if (data.links == null || data.nodes == null) return@EventHandler

                        //println(data)

                        aPaneInSPane.children.removeIf { it is DragNode || it is NodeLink }

                        data.nodes.forEach {
                            val node = getNode(it.name)
                            node.fromData(it)
                            aPaneInSPane.children.add(node)
                        }

                        data.links.forEach {
                            //println(it)

                            val inNode = aPaneInSPane.lookup("#${it.inputNode}") as DragNode
                            val outNode = aPaneInSPane.lookup("#${it.outputNode}") as DragNode
                            val inAnchor = aPaneInSPane.lookup("#${it.inputAnchor}") as AnchorPane
                            val outAnchor = aPaneInSPane.lookup("#${it.outputAnchor}") as AnchorPane

                            inAnchor.layoutX = it.inputAnchorSize.first
                            inAnchor.layoutY = it.inputAnchorSize.second

                            outAnchor.layoutX = it.outputAnchorSize.first
                            outAnchor.layoutY = it.outputAnchorSize.second

//                            inNode.linkNodes(outNode, inNode, outAnchor, inAnchor, it.inputAnchor!!).id = it.id
                        }

                    } catch (e: IOException) {
                        println(e)
                    }
                }
            }
        }
        val saveButton = Button().apply {
            style = "-fx-border-color: black; -fx-border-image-width: 8; -fx-text-fill: blue;"
            font = Font.font("Verdana", 20.0)
            text = "Save"
            onAction = EventHandler {
                val ser = Serializer()
                ser.Save(aPaneInSPane)
            }
        }
        buttonHBox.children.add(loadButton)
        buttonHBox.children.add(saveButton)
        hBox.children.add(buttonHBox)


        val start = InputNode().apply {
            layoutX += 10
            layoutY += 50
            lookup("#deleteButton").isVisible = false
            onMouseEntered = EventHandler { e: MouseEvent ->
                scrollPane.isPannable = false
            }
            onMouseExited = EventHandler { e: MouseEvent ->
                scrollPane.isPannable = true
            }
        }
        val end = OutputNode().apply {
            layoutX = width_ - 100
            layoutY = height_ / 2
            onMouseEntered = EventHandler { e: MouseEvent ->
                scrollPane.isPannable = false
            }
            onMouseExited = EventHandler { e: MouseEvent ->
                scrollPane.isPannable = true
            }
        }

        aPaneInSPane.children.add(end)
        aPaneInSPane.children.add(start)



        vBox.children.add(hBox)
        vBox.children.add(scrollPane)

        return Scene(vBox, width_, height_)
    }

    fun getNode(str: String): DragNode {
        return when (str) {
            "Float" -> FloatNode()
            "Int" -> IntNode()
            "String" -> StringNode()
            "Start" -> InputNode()
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