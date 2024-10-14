package at.crowdware.nocodedesigner.utils

import com.sun.jna.Library
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

// Define the interface for the Swift library
interface MacLib : Library {
    fun selectFolder(darkMode: Boolean): Pointer?
    fun selectFile(darkMode: Boolean): Pointer?
}

// JNA interface to access Objective-C runtime
interface ObjCRuntime : Library {
    fun objc_getClass(className: String): Pointer
    fun sel_registerName(selectorName: String): Pointer
    fun objc_msgSend(receiver: Pointer, selector: Pointer): Pointer
}

fun isRunningInProdMode(): Boolean {
    val bp = getBundlePath()
    // in dev you get like: /Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin
    // in prod you get like: /Users/art/SourceCode/NoCodeDesigner/build/compose/binaries/main/app/NoCodeDesigner.app
    return bp.contains(".app")
}

fun getBundlePath(): String {
    val objc = Native.load("objc", ObjCRuntime::class.java) as ObjCRuntime
    val nsBundleClass = objc.objc_getClass("NSBundle")
    val mainBundleSelector = objc.sel_registerName("mainBundle")
    val mainBundle = objc.objc_msgSend(nsBundleClass, mainBundleSelector)
    val bundlePathSelector = objc.sel_registerName("bundlePath")
    val bundlePathNSString = objc.objc_msgSend(mainBundle, bundlePathSelector)
    val utf8StringSelector = objc.sel_registerName("UTF8String")
    val bundlePathPointer = objc.objc_msgSend(bundlePathNSString, utf8StringSelector)

    return bundlePathPointer.getString(0)
}

fun getLibraryPath(): String {
    val home = System.getProperty("user.dir")

    return if (isRunningInProdMode()) {
        val objc = Native.load("objc", ObjCRuntime::class.java) as ObjCRuntime
        val nsBundleClass = objc.objc_getClass("NSBundle")
        val mainBundleSelector = objc.sel_registerName("mainBundle")
        val mainBundle = objc.objc_msgSend(nsBundleClass, mainBundleSelector)
        val bundlePathSelector = objc.sel_registerName("bundlePath")
        val bundlePathNSString = objc.objc_msgSend(mainBundle, bundlePathSelector)
        val utf8StringSelector = objc.sel_registerName("UTF8String")
        val bundlePathPointer = objc.objc_msgSend(bundlePathNSString, utf8StringSelector)
        val bundlePath = bundlePathPointer.getString(0)
        val prodPath = "$bundlePath/Contents/Resources/libmac.dylib"
        prodPath
    } else {
        val devPath = "$home/mac/.build/release/libmac.dylib"
        devPath
    }
}

// Load the native library
val macLib = Native.load(getLibraryPath(), MacLib::class.java) as MacLib
