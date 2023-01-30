package com.yobasoft.yobaeditor

import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Point2D
import javafx.scene.layout.AnchorPane
import javafx.scene.shape.CubicCurve
import java.io.IOException
import java.util.*

class NodeLink : AnchorPane() {
    @FXML
    var nodeLink: CubicCurve? = null

    private var inputNode: DragNode? = null
    private var inputLinkString: String = ""
    private var outputNode: DragNode? = null


    @FXML
    private fun initialize() {
        nodeLink!!.controlX1Property().bind(Bindings.add(nodeLink!!.startXProperty(), 100))
        nodeLink!!.controlX2Property().bind(Bindings.add(nodeLink!!.endXProperty(), -100))
        nodeLink!!.controlY1Property().bind(Bindings.add(nodeLink!!.startYProperty(), 0))
        nodeLink!!.controlY2Property().bind(Bindings.add(nodeLink!!.endYProperty(), 0))

        parentProperty().addListener { _, _, _ ->
            if (parent == null) {
                if (inputNode != null) {
                    inputNode!!.connectedLinks.remove(this)
                    if (outputNode != null && inputNode!!.nodes.containsKey(inputLinkString)) {
                        inputNode!!.nodes[inputLinkString] =
                            inputNode!!.nodes[inputLinkString]!!.copy(second = null)
                    }
                }
                if (outputNode != null) {
                    outputNode!!.connectedLinks.remove(this)
                    outputNode!!.outputLink = null
                }
            }
        }
    }

    fun setStart(point: Point2D) {
        nodeLink!!.startX = point.x
        nodeLink!!.startY = point.y
    }

    fun setEnd(point: Point2D) {
        nodeLink!!.endX = point.x
        nodeLink!!.endY = point.y
    }

    fun bindStartEnd(source1: DragNode, source2: DragNode, a1: AnchorPane, a2: AnchorPane) {
        nodeLink!!.startXProperty().bind(Bindings.add(source1.layoutXProperty(), a1.layoutX + a1.width / 2.0))
        nodeLink!!.startYProperty().bind(Bindings.add(source1.layoutYProperty(), a1.layoutY + a1.height / 2.0))
        nodeLink!!.endXProperty().bind(Bindings.add(source2.layoutXProperty(), a2.layoutX + a2.width / 2.0))
        nodeLink!!.endYProperty().bind(Bindings.add(source2.layoutYProperty(), a2.layoutY + a2.height / 2.0))

        inputLinkString = a2.id
        links(source1, source2)
    }
    private fun links(source1: DragNode, source2: DragNode) {
        outputNode = source1
        inputNode = source2
        source1.connectedLinks.add(this)
        source2.connectedLinks.add(this)

        if (updateNode(outputNode!!))
            kickAction()
    }

    private fun updateNode(node: DragNode): Boolean {
        if (node.nodes.all { it.value.second != null }) {
            node.updateNode()
            return true
        }
        return false
    }

    fun kickAction() {
        if (inputNode == null)
            return

        if (updateNode(inputNode!!) && inputNode!!.outputLink != null)
            inputNode!!.outputLink!!.kickAction()
    }

    init {
        val fxmlLoader = FXMLLoader(
            javaClass.getResource("NodeLink.fxml")
        )
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        try {
            fxmlLoader.load<Any>()
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        }
        id = UUID.randomUUID().toString()
    }
}