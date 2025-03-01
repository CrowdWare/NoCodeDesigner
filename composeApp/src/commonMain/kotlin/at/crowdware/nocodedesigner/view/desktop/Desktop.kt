/*
 * Copyright (C) 2025 CrowdWare
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

package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import at.crowdware.nocodelib.viewmodel.GlobalProjectState

@Composable
fun desktop() {
    val currentProject = GlobalProjectState.projectState
    var textFieldValue by remember { mutableStateOf(currentProject?.currentFileContent ?: "") }

    LaunchedEffect(currentProject?.currentFileContent) {
        textFieldValue = currentProject?.currentFileContent ?: ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = MaterialTheme.colors.primary)
    ) {
        // Initially empty or with minimal components
        // Will be expanded as NoCodeDesigner functionality is developed
        Text(
            text = "NoCodeDesigner - Coming Soon",
            color = MaterialTheme.colors.onPrimary,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}
