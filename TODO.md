# TODO

## Next release

### App Part
- App description is overwritten when creating a website.
- License check via web service
- Support license cancel from user
- Download app after getting a license, send license file and download link
- Navigation should respect language, also in desktop app
- One of the worst bugs. Load file...press cmd-z and editor is empty
- Progressbar für build.
- Still the editor bug, that cursor jumps at the end of the file
- Create APK on Windows failed, we need error handling
- Watermark on non commercial version for app.
- Error when Youtube video is not found.
- Save progress of reading and load that page next time
- decision path save and reset
- Build APK has to change appId into something like at.crowdware.nocodebrowser.preCached
- Browser App does not display the right colors, when pre cached
- Theme, FontSize, FontFamily
- FontSize changeable from end user in book app

## Nice to have
- Page title, should be displayed in navbar of the app, at least for the browser app
- Glossar for Dein Leben
- Puzzle Solve Counter, Rewards
- Reader kan join in to create chapters...
- chapter rating like / dislike
- meeting with god after finish a chapter, changes the story and also the available chapters
- Plugins for website generation, umami analytics for example
- MD syntax highlighting more detailed
- SML syntax highlighting more detailed
- Hyperlinks like buttons
- TopAppBar in app.sml
- Page: barTitle
- Credit all open source developers.
- generate PDF
- pager
- Button { link: toggleSound:on|off }
- Button { link: godot:start|stop|back }
- IconButton {}
- List { data: }
- md referenz in text
- Video Einführung
- Feature Voting via canny.io
- Syntax-edit Grammar
  - Comments are not grayed out
  - No tabs anymore
  - Cursor management when tabbed
- TestCases
  - Syntax highligt..errors
  - Grammar tests
- Import multiple assets
- New Project (Theme chooser)
- New page (Template chooser)
- Paging as navigation maybe visual paging for book content
- App.xml -> open splash screen in preview
- App.xml is delivering a list of files including timestamp (caching)
- Expressions in SML
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
### EPUB
- part name "toc" and "pdfOnly" in docs

## Docs
- https://draft2digital.com/book/epubcheck/upload in docs

## Known bugs
Open a file and press undo, text field gets empty.
https://youtrack.jetbrains.com/issue/CMP-2958/Expose-Undo-Redo-state-in-TextField


## Pricing
FREE - for non commercial usage
STARTER - ebooks (epub2) only
PRO - source code generation and .APK

30,- three months Starter
48,- six months Starter 20 % off
84,- one year Starter 30% off

60,- three months Pro, min. 3 months
96,- six months pro 20% off
210,- one year Pro 30% off


# HTML for the epub version, license for free version
 <p xmlns:cc="http://creativecommons.org/ns#" xmlns:dct="http://purl.org/dc/terms/"><a property="dct:title" rel="cc:attributionURL" href="http://[link to work]">[Title of Work]</a> by <a rel="cc:attributionURL dct:creator" property="cc:attributionName" href="http://[link to creator profile]">[Creator]</a> is licensed under <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1" target="_blank" rel="license noopener noreferrer" style="display:inline-block;">Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International<img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/cc.svg?ref=chooser-v1" alt=""><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/by.svg?ref=chooser-v1" alt=""><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/nc.svg?ref=chooser-v1" alt=""><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/sa.svg?ref=chooser-v1" alt=""></a></p> 
