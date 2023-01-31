package com.yobasoft.yobaeditor

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.*
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import org.opencv.core.Mat
import java.io.IOException
import java.util.*


lateinit var nodeDragStart: DragNode
lateinit var anchorDragStart: AnchorPane

var stateAddLink = DataFormat("linkAdd")
var stateAddNode = DataFormat("nodeAdd")

abstract class DragNode : AnchorPane() {
    @FXML
    var rootPane: AnchorPane? = null

    @FXML
    var outputLinkHandle: AnchorPane? = null

    @FXML
    var titleBar: Label? = null

    @FXML
    var deleteButton: Button? = null

    private lateinit var contextDragOver: EventHandler<DragEvent>
    private lateinit var contextDragDropped: EventHandler<DragEvent>
    private lateinit var linkDragDetected: EventHandler<MouseEvent>
    private lateinit var linkDragDropped: EventHandler<DragEvent>
    private lateinit var contextLinkDragOver: EventHandler<DragEvent>
    private lateinit var contextLinkDagDropped: EventHandler<DragEvent>

    private var myLink = NodeLink()
    private var offset = Point2D(0.0, 0.0)

    var connectedLinks = mutableListOf<NodeLink>()
    var outputLink: NodeLink? = null

    var nodes = mutableMapOf<String, Triple<AnchorPane, DragNode?, NodeTypes>>()
    open val nodeType: NodeTypes = NodeTypes.NONE

    private var superParent: AnchorPane? = null

    abstract fun getValue(): Any?

    abstract fun updateNode()

    open fun addInit() {}

    @FXML
    private fun initialize() {
        nodeHandlers()
        linkHandlers()

        initHandles()
        addInit()

        myLink.isVisible = false

        deleteButton!!.onAction = EventHandler {
            superParent!!.children.remove(this)
            superParent!!.children.removeIf { ch -> connectedLinks.count { it.id == ch.id } > 0 }
        }

        nodes.forEach { it.value.first.onDragDropped = linkDragDropped }

        parentProperty().addListener { _, _, _ -> if (parent != null) superParent = parent as AnchorPane }
    }

    open fun initHandles() {
        if (outputLinkHandle != null)
            outputLinkHandle!!.onDragDetected = linkDragDetected
    }


    private fun updatePoint(p: Point2D) {
        val local = parent.sceneToLocal(p)
        relocate(
            (local.x - offset.x),
            (local.y - offset.y)
        )
    }

    private fun nodeHandlers() {

        contextDragOver = EventHandler { event ->
            updatePoint(Point2D(event.sceneX, event.sceneY))
            event.consume()
        }

        contextDragDropped = EventHandler { event ->
            parent.onDragDropped = null
            parent.onDragOver = null
            event.isDropCompleted = true
            event.consume()
        }


        rootPane!!.onDragDetected = EventHandler { event ->
            parent.onDragOver = contextDragOver
            parent.onDragDropped = contextDragDropped

            offset = Point2D(event.x, event.y)
            updatePoint(Point2D(event.sceneX, event.sceneY))

            val content = ClipboardContent()
            content[stateAddNode] = "node"
            startDragAndDrop(*TransferMode.ANY).setContent(content)
            event.consume()
        }
    }

    private fun linkHandlers() {

        linkDragDetected = EventHandler { event ->

            if (outputLink != null) {
                superParent!!.children.remove(outputLink)
            }

            parent.onDragOver = null
            parent.onDragDropped = null

            parent.onDragOver = contextLinkDragOver
            parent.onDragDropped = contextLinkDagDropped

            superParent!!.children.add(0, myLink)
            myLink.isVisible = true

            val src = event.source as AnchorPane

            val p = Point2D(layoutX + src.layoutX + src.width / 2, layoutY + src.layoutY + src.height / 2)
            myLink.setStart(p)

            nodeDragStart = this
            anchorDragStart = event.source as AnchorPane

            val content = ClipboardContent()
            content[stateAddLink] = "link"
            startDragAndDrop(*TransferMode.ANY).setContent(content)
            event.consume()
        }

        linkDragDropped = EventHandler { event ->

            val src = event.source as AnchorPane
            if (nodeDragStart == this || !nodes.containsKey(src.id) || nodeDragStart.nodeType != nodes[src.id]!!.third || nodes[src.id]!!.second != null)
                return@EventHandler

            parent.onDragOver = null
            parent.onDragDropped = null

            myLink.isVisible = false
            superParent!!.children.removeAt(0)

            val link = NodeLink()

            superParent!!.children.add(0, link)
            nodeDragStart.outputLink = link

            nodes[src.id] = nodes[src.id]!!.copy(second = nodeDragStart)
            link.bindStartEnd(nodeDragStart, this, anchorDragStart, event.source as AnchorPane)
            event.isDropCompleted = true
            event.consume()
        }


        contextLinkDragOver = EventHandler { event ->
            event.acceptTransferModes(*TransferMode.ANY)
            if (!myLink.isVisible) myLink.isVisible = true
            myLink.setEnd(Point2D(event.x, event.y))

            event.consume()
        }

        contextLinkDagDropped = EventHandler { event ->
            parent.onDragDropped = null
            parent.onDragOver = null

            myLink.isVisible = false
            superParent!!.children.removeAt(0)

            event.isDropCompleted = true
            event.consume()
        }
    }

    fun init(str: String) {
        val fxmlLoader = FXMLLoader(
            javaClass.getResource(str)
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

    fun errorNode(str: String): Mat? {
        if (nodes.containsKey(str)) {
            (nodes[str]!!.first.children.find { it is Circle } as Circle).fill = Paint.valueOf(Colors.RED)
        }
        return null
    }

    fun goodNodes() {
        nodes.forEach {
            (it.value.first.children.find { it2 -> it2 is Circle } as Circle).fill = Paint.valueOf(Colors.GREEN)
        }
    }
    open fun fromData(nodeData: Serializer.NodeData) {
        id = nodeData.id
        layoutX = nodeData.x ?: 100.0
        layoutY = nodeData.y ?: 100.0
    }

}