# NoCodeDesigner

The idea of this project is to create a desktop app where the user is able to build 
dynamic Android apps with NO CODE. So the user doesn't have to be a developer at all.
The initial aim was be able to create ebooks with new media content like movies, 3D objects, 
buttons for interactions.
We have now splitted the project into NoCodeDesigner only for Android apps and the [BookDesigner](https://github.com/CrowdWare/BookDesigner) only for ebooks. BookDesigner is now a JavaFX application.

# Platforms
This desktop app will be available for Mac, Window, Linux.

# Build
**Windows**
`gradlew packageDistributionForCurrentOS`
Find the MSI file in composeApp/build/compose/binaries/main/msi

**Mac**
`./gradlew packageDistributionForCurrentOS`
Find the DMG file in composeApp/build/compose/binaries/main/dmg


# apktool
The apktool can be downloaded here: https://apktool.org/docs/install
