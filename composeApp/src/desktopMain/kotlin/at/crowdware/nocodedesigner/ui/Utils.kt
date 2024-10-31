/*
 * Copyright (C) 2024 CrowdWare
 *
 * This file is part of NoCodeDesigner.
 *
 *  NoCodeDesigner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeDesigner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeDesigner.  If not, see <http://www.gnu.org/licenses/>.
 */

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