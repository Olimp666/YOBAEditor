package com.yobasoft.yobaeditor

import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.ScrollPane
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.VBox
import kotlin.math.exp

class ZoomableScrollPane(private val target: Node) : ScrollPane() {
    private var scaleValue = 0.7
    private val zoomIntensity = 0.02
    private val zoomNode: Node

    var X:Double =0.0
    var Y:Double =0.0


    init {
        zoomNode = Group(target)
        content = outerNode(zoomNode)
        isPannable = true
        hbarPolicy = ScrollBarPolicy.NEVER
        vbarPolicy = ScrollBarPolicy.NEVER
        isFitToHeight = true
        isFitToWidth = true
        updateScale()
    }

    private fun outerNode(node: Node): Node {
        val outerNode = centeredNode(node)
        outerNode.onScroll = EventHandler { e: ScrollEvent ->
            e.consume()
            onScroll(e.textDeltaY, Point2D(e.x, e.y))
        }
        return outerNode
    }

    private fun centeredNode(node: Node): Node {
        val vBox = VBox(node)
        vBox.alignment = Pos.CENTER
        return vBox
    }

    private fun updateScale() {
        target.scaleX = scaleValue
        target.scaleY = scaleValue
    }

    private fun onScroll(wheelDelta: Double, mousePoint: Point2D) {
        val zoomFactor = exp(wheelDelta * zoomIntensity)
        val innerBounds = zoomNode.layoutBounds
        val viewportBounds = viewportBounds

        val valX = hvalue * (innerBounds.width - viewportBounds.width)
        val valY = vvalue * (innerBounds.height - viewportBounds.height)
        scaleValue *= zoomFactor
        updateScale()
        layout()

        val posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint))

        val adjustment = target.localToParentTransform.deltaTransform(posInZoomTarget.multiply(zoomFactor - 1))

        val updatedInnerBounds = zoomNode.boundsInLocal
        hvalue = (valX + adjustment.x) / (updatedInnerBounds.width - viewportBounds.width)
        vvalue = (valY + adjustment.y) / (updatedInnerBounds.height - viewportBounds.height)
    }
}