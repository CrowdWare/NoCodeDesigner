package at.crowdware.nocodedesigner.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import at.crowdware.nocodedesigner.utils.UIElement

enum class NodeType {
    DIRECTORY, OTHER, IMAGE, VIDEO, SOUND, XML, MD, SML
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
    "sml" to NodeType.SML,
    "md" to NodeType.MD,
    "webp" to NodeType.IMAGE,
    "bmp" to NodeType.IMAGE,
    "webm" to NodeType.VIDEO,
    "avi" to NodeType.VIDEO,
    "flv" to NodeType.VIDEO,
    "ts" to NodeType.VIDEO,
    "3gp" to NodeType.VIDEO,
    "m4v" to NodeType.VIDEO
)

open class TreeNode(
    var title: MutableState<String> = mutableStateOf(""),
    val type: Any,
    var path: String = "",
    var children: SnapshotStateList<TreeNode> = mutableStateListOf(),
    var expanded: MutableState<Boolean> = mutableStateOf(false),
)


class ElementTreeNode(
    title: MutableState<String> = mutableStateOf(""),
    type: NodeType,
    path: String,
    children: SnapshotStateList<TreeNode> = mutableStateListOf(),
    expanded: MutableState<Boolean> = mutableStateOf(false),
    element: UIElement = UIElement.Zero
) : TreeNode(title, type, path, children, expanded)