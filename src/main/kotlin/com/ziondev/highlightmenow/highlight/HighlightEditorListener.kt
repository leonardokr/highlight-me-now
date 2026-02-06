package com.ziondev.highlightmenow.highlight

import com.ziondev.highlightmenow.settings.HighlightSettingsState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.ColorUtil
import java.awt.Color

class HighlightEditorListener : EditorFactoryListener {
    companion object {
        private val SOFT_WHITE = Color(240, 240, 240)
    }

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        applyHighlights(editor)

        editor.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                applyHighlights(editor)
            }
        })
    }

    private fun applyHighlights(editor: Editor) {
        val markupModel = editor.markupModel
        val document = editor.document
        val text = document.text

        // Remove old highlights from this plugin
        markupModel.allHighlighters
            .filter { it.getUserData(HIGHLIGHT_KEY) == true }
            .forEach { markupModel.removeHighlighter(it) }

        val state = HighlightSettingsState.getInstance()
        val patterns = state.patterns
        val highlightEntireLine = state.highlightEntireLine

        if (patterns.isEmpty() || text.isEmpty()) return

        val highlightedLines = mutableSetOf<Int>()

        for (item in patterns) {
            if (item.pattern.isEmpty()) continue

            val regex = try {
                Regex(item.pattern, RegexOption.IGNORE_CASE)
            } catch (e: Exception) {
                continue
            }

            for (match in regex.findAll(text)) {
                val matchStart = match.range.first
                val matchEnd = match.range.last + 1

                if (matchStart >= text.length) continue

                val lineNumber = document.getLineNumber(matchStart)
                val bgColor = parseColor(item.background)
                val fgColor = parseColor(item.color)

                if (bgColor == null && fgColor == null) continue

                // Highlight line background (entire line or just text content)
                if (bgColor != null && lineNumber !in highlightedLines) {
                    val lineStart = document.getLineStartOffset(lineNumber)
                    val lineEnd = document.getLineEndOffset(lineNumber)
                    val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))

                    val (bgStart, bgEnd) = if (highlightEntireLine) {
                        lineStart to lineEnd
                    } else {
                        // Find first and last non-whitespace characters
                        val firstNonWhitespace = lineText.indexOfFirst { !it.isWhitespace() }
                        val lastNonWhitespace = lineText.indexOfLast { !it.isWhitespace() }
                        if (firstNonWhitespace >= 0 && lastNonWhitespace >= 0) {
                            (lineStart + firstNonWhitespace) to (lineStart + lastNonWhitespace + 1)
                        } else {
                            lineStart to lineEnd
                        }
                    }

                    val lineAttrs = TextAttributes().apply {
                        backgroundColor = bgColor
                    }

                    val targetArea = if (highlightEntireLine) {
                        HighlighterTargetArea.LINES_IN_RANGE
                    } else {
                        HighlighterTargetArea.EXACT_RANGE
                    }

                    val highlighter = markupModel.addRangeHighlighter(
                        bgStart, bgEnd,
                        HighlighterLayer.SELECTION - 1,
                        lineAttrs,
                        targetArea
                    )
                    highlighter.putUserData(HIGHLIGHT_KEY, true)
                    highlightedLines.add(lineNumber)
                }

                // Highlight matched text with foreground color
                val textAttrs = TextAttributes().apply {
                    backgroundColor = bgColor
                    foregroundColor = fgColor ?: if (bgColor != null) getContrastTextColor(bgColor) else null
                }

                val highlighter = markupModel.addRangeHighlighter(
                    matchStart, matchEnd,
                    HighlighterLayer.SELECTION,
                    textAttrs,
                    HighlighterTargetArea.EXACT_RANGE
                )
                highlighter.putUserData(HIGHLIGHT_KEY, true)
            }
        }
    }

    private fun parseColor(colorStr: String): Color? {
        val trimmed = colorStr.trim()
        if (trimmed.isEmpty()) return null
        return try {
            ColorUtil.fromHex(if (trimmed.startsWith("#")) trimmed else "#$trimmed")
        } catch (e: Exception) {
            null
        }
    }

    private fun getContrastTextColor(bg: Color): Color {
        val r = bg.red / 255.0
        val g = bg.green / 255.0
        val b = bg.blue / 255.0
        val rL = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
        val gL = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
        val bL = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)
        val luminance = 0.2126 * rL + 0.7152 * gL + 0.0722 * bL
        return if (luminance > 0.5) Color.BLACK else SOFT_WHITE
    }
}

private val HIGHLIGHT_KEY = com.intellij.openapi.util.Key.create<Boolean>("HighlightMeNow")
