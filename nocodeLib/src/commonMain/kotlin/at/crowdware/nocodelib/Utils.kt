/*
 * Copyright (C) 2024 CrowdWare
 *
 * This file is part of NoCodeLib.
 *
 *  NoCodeLib is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeLib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.nocodelib

import androidx.compose.ui.graphics.Color


fun parsePadding(padding: String): Padding {
    val paddingValues = padding.split(" ").mapNotNull { it.toIntOrNull() }

    return when (paddingValues.size) {
        1 -> Padding(paddingValues[0], paddingValues[0], paddingValues[0], paddingValues[0]) // Alle Seiten gleich
        2 -> Padding(paddingValues[0], paddingValues[1], paddingValues[0], paddingValues[1]) // Vertikal und Horizontal gleich
        4 -> Padding(paddingValues[0], paddingValues[1], paddingValues[2], paddingValues[3]) // Oben, Rechts, Unten, Links
        else -> Padding(0, 0, 0, 0)
    }
}

fun hexToColor(hex: String): Color {
    val color = hex.trimStart('#')
    return when (color.length) {
        6 -> {
            // Hex without alpha (e.g., "RRGGBB")
            val r = color.substring(0, 2).toIntOrNull(16) ?: return Color.Black
            val g = color.substring(2, 4).toIntOrNull(16) ?: return Color.Black
            val b = color.substring(4, 6).toIntOrNull(16) ?: return Color.Black
            Color(r, g, b)
        }
        8 -> {
            // Hex with alpha (e.g., "AARRGGBB")
            val a = color.substring(0, 2).toIntOrNull(16) ?: return Color.Black
            val r = color.substring(2, 4).toIntOrNull(16) ?: return Color.Black
            val g = color.substring(4, 6).toIntOrNull(16) ?: return Color.Black
            val b = color.substring(6, 8).toIntOrNull(16) ?: return Color.Black
            Color(r, g, b, a)
        }
        else -> Color.Black
    }
}
