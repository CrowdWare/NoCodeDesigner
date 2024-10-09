# TODO

## Next release
- Syntaxedit
  - Comments are not grayed out
  - No tabs anymore
  - Cursor management when tabbed
- Icon exchange with custom svg
- TestCases
  - Syntax highligt..errors
  - Grammar tests
    
## Bugs
- Cursor movement is bad...goes not up if at the last bracket
```qml
Page {
    backgroundColor: "#00000ff"
	color: "#FFFFFF"
	Column {
	Button { label: "Click me" link: "page:home" }
	Spacer { height: 8 }
	Button { label: "Click me" link: "page:home" }
	}

	
}
```

## Nice to have
- Import multiple assets
- New Project (Theme chooser)
- New page (Template chooser)
- Paging as navigation maybe visual paging for book content
- App.xml -> open splash screen in preview
- App.xml is delivering a list of files including timestamp (caching)
- Expressions in QML
- Settings page
    - Option to choose between local directory and local webserver via REST
    - Todo
- versioning of log file for later support
- put python install into dmg for local webserver
- Git support
- Save only when xml is valid or after a period od time
- Drag and drop tree node to editor (button) 
- WebService in KTOR also for Desktop
- Code Generator