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

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.theme.ExtendedTheme

@Composable
fun partDialog(
    name: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    onDismissRequest: () -> Unit,
    onCreateRequest: () -> Unit
) {

    CustomDialog(
        onDismissRequest = onDismissRequest,
        onConfirmRequest = onCreateRequest,
        title = "Create Part",
        confirmButtonText = "Create",
        cancelButtonText = "Cancel",
        height = 250
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Name:",
                    color= MaterialTheme.colors.onPrimary,
                    modifier = Modifier.align(Alignment.CenterVertically).weight(1F))
                Spacer(modifier = Modifier.width(16.dp))
                TextInput(name, onNameChange, modifier = Modifier.weight(3F))
            }
        }
    }
}