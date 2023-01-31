package com.yobasoft.yobaeditor

import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import org.hildan.fxgson.FxGson
import java.io.File
import java.io.IOException


open class Serializer {
    data class PairD<A, B>(val first: A, val second: B)
    data class NodeData(val id: String, val name: String, val x: Double?, val y: Double?, var data: String?)

    open fun toData(node: DragNode): NodeData {
        return NodeData(node.id, node::class.simpleName!!, node.rootPane?.layoutX, node.rootPane?.layoutY, null)
    }
    data class Saved(val nodes: MutableList<NodeData>?, val links: MutableList<LinkData>?)


    data class LinkData(
        val id: String?,
        val inputNode: String?,
        val inputNodeClass: String?,
        val outputNode: String?,
        val outputNodeClass: String?,
        val inputAnchor: String?,
        val inputAnchorSize: PairD<Double, Double>,
        val outputAnchor: String?,
        val outputAnchorSize: PairD<Double, Double>
    )

    open fun toData(link: NodeLink): LinkData {
        return LinkData(
            link.id,
            link.inputNode?.id,
            link.inputNode!!::class.simpleName,
            link.outputNode?.id,
            link.outputNode!!::class.simpleName,
            link.inputAnchor?.id,
            PairD(
                link.inputAnchor!!.layoutX + link.inputAnchor!!.width / 2,
                link.inputAnchor!!.layoutY + link.inputAnchor!!.height / 2
            ),
            link.outputAnchor?.id,
            PairD(
                link.outputAnchor!!.layoutX + link.outputAnchor!!.width / 2,
                link.outputAnchor!!.layoutY + link.outputAnchor!!.height / 2
            ),
        )
    }

    fun Save(node: AnchorPane) {
        val gson = FxGson.coreBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        val nodes = node.children.filterIsInstance<DragNode>()
        val listNodes = MutableList(nodes.size) { toData(nodes[it]) }
        val links = node.children.filterIsInstance<NodeLink>()
        val listLinks = MutableList(links.size) { toData(links[it]) }

        println(gson.toJson(Saved(listNodes, listLinks)))

        val fileChooser = FileChooser()
        fileChooser.title = "Save Nodes"
        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("JSON", "*.json"))
        val dir = fileChooser.showSaveDialog(node.scene.window)
        if (dir != null) {
            try {
                val file = File(dir.toURI())
                file.writeText(gson.toJson(Saved(listNodes, listLinks)))
            } catch (e: IOException) {
                println(e)
            }
        }
    }

}