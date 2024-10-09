import AppKit



@_cdecl("selectFolder")
public func selectFolder(darkMode: Bool) -> UnsafeMutablePointer<CChar>? {
    // Initialize and activate the NSApplication to ensure it inherits system settings
    let app = NSApplication.shared
    app.setActivationPolicy(.regular)
    app.activate(ignoringOtherApps: true)

    // Create a placeholder for the result
    var selectedFolderPath: UnsafeMutablePointer<CChar>? = nil

    // Ensure the NSOpenPanel is called on the main thread
    DispatchQueue.main.sync {
        let openPanel = NSOpenPanel()
        openPanel.message = "Please select the folder, with the project data."
        openPanel.showsResizeIndicator = true
        openPanel.showsHiddenFiles = false
        openPanel.canChooseDirectories = true
        openPanel.canChooseFiles = false
        openPanel.allowsMultipleSelection = false

        // Set the appearance based on the darkMode parameter
        if #available(macOS 10.14, *) {
            if darkMode {
                openPanel.appearance = NSAppearance(named: .darkAqua) // Dark mode
            } else {
                openPanel.appearance = NSAppearance(named: .aqua) // Light mode
            }
        }

        let response = openPanel.runModal()

        if response == .OK, let url = openPanel.url {
            let path = url.path
            selectedFolderPath = strdup(path) // Return the folder path as a C-style string
        }
    }

    return selectedFolderPath
}

@_cdecl("selectFile")
public func selectFile(darkMode: Bool) -> UnsafeMutablePointer<CChar>? {
    // Initialize and activate the NSApplication to ensure it inherits system settings
    let app = NSApplication.shared
    app.setActivationPolicy(.regular)
    app.activate(ignoringOtherApps: true)

    // Create a placeholder for the result
    var selectedFilePath: UnsafeMutablePointer<CChar>? = nil

    // Ensure the NSOpenPanel is called on the main thread
    DispatchQueue.main.sync {
        let openPanel = NSOpenPanel()
        openPanel.message = "Please select a file to import."
        openPanel.showsResizeIndicator = true
        openPanel.showsHiddenFiles = false
        openPanel.canChooseDirectories = false
        openPanel.canChooseFiles = true
        openPanel.allowsMultipleSelection = false

        // Set the file types filter
        openPanel.allowedFileTypes = ["png", "jpg", "gif", "mp3", "mp4"]

        // Set the appearance based on the darkMode parameter
        if #available(macOS 10.14, *) {
            if darkMode {
                openPanel.appearance = NSAppearance(named: .darkAqua) // Dark mode
            } else {
                openPanel.appearance = NSAppearance(named: .aqua) // Light mode
            }
        }

        let response = openPanel.runModal()

        if response == .OK, let url = openPanel.url {
            let path = url.path
            selectedFilePath = strdup(path) // Return the file path as a C-style string
        }
    }

    return selectedFilePath
}