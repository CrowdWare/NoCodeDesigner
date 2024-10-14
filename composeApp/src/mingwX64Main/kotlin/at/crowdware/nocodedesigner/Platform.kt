package at.crowdware.nocodedesigner

import com.sun.jna.Pointer

actual class Platform {
    actual companion object {
        actual fun selectFolder(darkMode: Boolean): Pointer? {
            // TODO: implement
            return null
        }

        actual fun selectFile(darkMode: Boolean): Pointer? {
            // TODO: implement
            return null
        }
    }
}