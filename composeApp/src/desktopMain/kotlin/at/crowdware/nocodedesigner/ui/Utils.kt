package at.crowdware.nocodedesigner.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerMoveFilter

actual fun getMatchRange(matchGroup: MatchGroup?): IntRange {
    return matchGroup?.range ?: IntRange.EMPTY
}

actual fun createCodeBlockRegex(): Regex {
    return Regex("```(.*?)```", RegexOption.DOT_MATCHES_ALL)
}

actual fun ioDispatcher(): CoroutineDispatcher {
    return Dispatchers.IO
}

/*
@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.pointerMoveHandler(
    onEnter: () -> Boolean,
    onExit: () -> Boolean
): Modifier {
    return this.pointerMoveFilter(
        onEnter = { onEnter() },
        onExit = { onExit() }
    )
}

 */