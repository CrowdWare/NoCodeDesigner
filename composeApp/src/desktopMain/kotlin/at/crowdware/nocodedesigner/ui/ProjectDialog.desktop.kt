
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

import at.crowdware.nocodedesigner.MacLib
import at.crowdware.nocodedesigner.getLibraryPath
import at.crowdware.nocodedesigner.viewmodel.GlobalProjectState
import com.sun.jna.Native


val macLib = Native.load(getLibraryPath(), MacLib::class.java) as MacLib

actual fun openFolder() :String {
    try {
        // Load the native library
        val currentProject = GlobalProjectState.projectState
        val folderPathPointer = macLib.selectFolder(currentProject?.darkMode != false)

        // Check if the pointer is null
        if (folderPathPointer == null) {
            println("No folder selected or an error occurred in the Swift function.")
        } else {
            // Convert the result from a Pointer to a String
            return folderPathPointer.getString(0)
        }
    } catch (e: Exception) {
        // Catch any exceptions that happen during the JNA call
        println("Error calling native function: ${e.message}")
        e.printStackTrace()
    }
    return ""
}