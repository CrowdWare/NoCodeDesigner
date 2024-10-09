package at.crowdware.nocodedesigner.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

enum class NodeType {
    DIRECTORY, IMAGE, VIDEO, SOUND, XML, MD, OTHER, QML
}

val extensionToNodeType = mapOf(
    "png" to NodeType.IMAGE,
    "jpg" to NodeType.IMAGE,
    "jpeg" to NodeType.IMAGE,
    "gif" to NodeType.IMAGE,
    "mp4" to NodeType.VIDEO,
    "avi" to NodeType.VIDEO,
    "mkv" to NodeType.VIDEO,
    "mov" to NodeType.VIDEO,
    "mp3" to NodeType.SOUND,
    "wav" to NodeType.SOUND,
    "flac" to NodeType.SOUND,
    "xml" to NodeType.XML,
    "md" to NodeType.MD,
    "qml" to NodeType.QML
)

// Data model for a tree node
data class TreeNode(
    val title: String,
    val path: String,
    val type: NodeType,
    var children: SnapshotStateList<TreeNode> = mutableStateListOf(),
    var expanded: MutableState<Boolean> = mutableStateOf(false)
)