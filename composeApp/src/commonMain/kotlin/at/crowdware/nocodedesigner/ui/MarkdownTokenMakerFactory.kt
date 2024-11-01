package at.crowdware.nocodedesigner.ui

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory
import org.fife.ui.rsyntaxtextarea.TokenMaker

class MarkdownTokenMakerFactory : AbstractTokenMakerFactory() {
    companion object {
        const val SYNTAX_STYLE_MARKDOWN = "text/markdown"
    }

    override fun getTokenMakerImpl(key: String): TokenMaker? {
        return when (key) {
            SYNTAX_STYLE_MARKDOWN -> MarkdownTokenMaker()
            else -> null
        }
    }

    override fun initTokenMakerMap() {
        putMapping(SYNTAX_STYLE_MARKDOWN, "at.crowdware.nocodedesigner.ui.MarkdownTokenMaker")
    }
}
