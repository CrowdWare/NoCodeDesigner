package at.crowdware.nocodedesigner.ui

import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineDispatcher

expect fun ioDispatcher(): CoroutineDispatcher

expect fun getMatchRange(matchGroup: MatchGroup?): IntRange

expect fun createCodeBlockRegex(): Regex

/*
expect fun Modifier.pointerMoveHandler(
    onEnter: () -> Boolean,
    onExit: () -> Boolean
): Modifier
 */