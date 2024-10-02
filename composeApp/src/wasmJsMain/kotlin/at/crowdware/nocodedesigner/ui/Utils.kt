package at.crowdware.nocodedesigner.ui

import androidx.compose.ui.Modifier
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


actual fun getMatchRange(matchGroup: MatchGroup?): IntRange {
    return if (matchGroup != null) {
        val start = matchGroup.range.start
        val end = matchGroup.range.endInclusive
        start..end
    } else {
        IntRange.EMPTY
    }
}

actual fun createCodeBlockRegex(): Regex {
    return Regex("(?s)```(.*?)```")
}

actual fun ioDispatcher(): CoroutineDispatcher {
    return Dispatchers.Default
}



/*
actual fun Modifier.pointerMoveHandler(
    onEnter: () -> Boolean,
    onExit: () -> Boolean
): Modifier {
    return this.then(Modifier.onPointerEnter { onEnter() })
        .then(Modifier.onPointerLeave { onExit() })
}*/