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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun settingsDialog(
    theme: String,
    onThemeChanged: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onCreateRequest: () -> Unit
) {
    CustomDialog(
        title = "Settings",
        onDismissRequest = onDismissRequest,
        onConfirmRequest = onCreateRequest,
        confirmButtonText = "Apply",
        cancelButtonText = "Cancel",
        height = 400
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Theme:",
                    color = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
                )
                RadioButtonItem(modifier = Modifier.weight(1f),
                    label = "Light",
                    selected = theme == "Light",
                    color = MaterialTheme.colors.onPrimary,
                    onClick = { onThemeChanged("Light") }
                )
                RadioButtonItem(modifier = Modifier.weight(1f),
                    label = "Dark",
                    selected = theme == "Dark",
                    color = MaterialTheme.colors.onPrimary,
                    onClick = { onThemeChanged("Dark") }
                )
            }
        }
    }
}
