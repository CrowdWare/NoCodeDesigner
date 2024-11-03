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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.theme.ExtendedTheme
import java.io.InputStream

@Composable
fun aboutDialog(appName: String, version: String,
    onDismissRequest: () -> Unit,
) {
    CustomDialog(
        onDismissRequest = onDismissRequest,
        onConfirmRequest = onDismissRequest,
        title = "About NoCodeDesigner",
        confirmButtonText = "OK",
        cancelButtonText = "",
        height = 250
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Column {
                val icnsIcon = loadPngIcon("/icons/icon.png")
                IconDisplay(icnsIcon)
            }
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
            ) {
                Text(
                    text = "$appName $version",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Copyright © 2024 CrowdWare",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "All rights reserved.",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun loadPngIcon(resourcePath: String): ImageBitmap? {
    return try {
        val inputStream: InputStream = object {}.javaClass.getResourceAsStream(resourcePath)
        loadImageBitmap(inputStream)  // Load .png image instead
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}