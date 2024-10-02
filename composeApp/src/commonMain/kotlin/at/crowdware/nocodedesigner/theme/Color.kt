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

package at.crowdware.nocodedesigner.theme

import androidx.compose.ui.graphics.Color

// Light Theme Colors
val PrimaryColor = Color(0xFFDADADA)//Color(0xFF6200EA)
val SecondaryColor = Color(0xFF03DAC5)
val BackgroundColor = Color(0xFFFFFFFF)
val SurfaceColor = Color(0xFFFFFFFF)
val OnPrimaryColor = Color(0xFF514F50)
val OnSecondaryColor = Color(0xFF000000)
val OnSurfaceColor = Color(0xFF000000)

// Dark Theme Colors
val PrimaryColorDark = Color(0xFF353739)
val SecondaryColorDark = Color(0xFF03DAC5)
val BackgroundColorDark = Color(0xFF121212)
val SurfaceColorDark = Color(0xFF1F1F1F)
val OnPrimaryColorDark = Color(0xFFB0B0B0)
val OnSecondaryColorDark = Color(0xFFFFFFFF)
val OnSurfaceColorDark = Color(0xFFFFFFFF)

// Extended Colors (hover states, accents, etc.)
data class ExtendedColors(
    val surfaceHoverColor: Color,
    val secondaryHoverColor: Color,
    val customAccentColor: Color,
    val syntaxColor: Color,
    val attributeNameColor: Color,
    val attributeValueColor: Color,
    val captionColor: Color,
    val defaultTextColor: Color,
    val directoryColor: Color,
    val imageColor: Color,
    val videoColor: Color,
    val soundColor: Color,
    val xmlColor: Color,
    val mdHeader: Color,
    val linkColor: Color
)

// Light Extended Colors
val LightExtendedColors = ExtendedColors(
    surfaceHoverColor = Color(0xFFE0E0E0),      // Slightly darker for hover in light theme
    secondaryHoverColor = Color(0xFFD0D0D0),
    customAccentColor = Color(0xFF6200EA),
    syntaxColor = Color(0xFFB97A57),            // Warm orange
    attributeNameColor = Color(0xFF6A5ACD),     // Slate blue
    attributeValueColor = Color(0xFF008000),    // Dark green
    captionColor = Color(0xFFFCF7F3),
    defaultTextColor = Color(0xFF000000),
    directoryColor = Color(0xFF4CAF50),         // Green for directories
    imageColor = Color(0xFFFFC107),             // Amber for images
    videoColor = Color(0xFF2196F3),             // Blue for videos
    soundColor = Color(0xFFF44336),             // Red for sounds
    xmlColor = Color(0xFF9C27B0),                // Purple for XML files
    mdHeader = Color(0xFFB774B1),
    linkColor = Color(0xFF5E90E0)
)

// Dark Extended Colors
val DarkExtendedColors = ExtendedColors(
    surfaceHoverColor = Color(0xFF333333),      // Slightly lighter for hover in dark theme
    secondaryHoverColor = Color(0xFF555555),
    customAccentColor = Color(0xFFBB86FC),
    syntaxColor = Color(0xFFDBB965),            // Gold
    attributeNameColor = Color(0xFFA5A5A3),     // Grayish
    attributeValueColor = Color(0xFF60774E),    // Olive green
    captionColor = Color(0xFF37302F),
    defaultTextColor = Color(0xFFB0B0B0),
    directoryColor = Color(0xFFB0B0B0),
    imageColor = Color(0xFF64B5F6),
    videoColor = Color(0xFF94BCFD),
    soundColor = Color(0xFFF0766E),
    xmlColor = Color(0xFFA9704C),
    mdHeader = Color(0xFFB774B1),
    linkColor = Color(0xFF5E90E0)
)
