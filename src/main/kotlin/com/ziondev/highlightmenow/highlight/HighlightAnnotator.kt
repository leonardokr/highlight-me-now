package com.ziondev.highlightmenow.highlight

import com.ziondev.highlightmenow.settings.HighlightSettingsState
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.ColorUtil
import java.awt.Color

class HighlightAnnotator : Annotator {
    companion object {
        private val HIGHLIGHTED_LINES_KEY = Key.create<MutableSet<Int>>("com.ziondev.highlightmenow.HIGHLIGHTED_LINES")
        private val SOFT_WHITE = Color(240, 240, 240)
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Process only the PsiFile to scan the entire document once
        if (element !is PsiFile) return

        val document = element.viewProvider.document ?: return
        val text = document.text
        if (text.isEmpty()) return

        val patterns = HighlightSettingsState.getInstance().patterns
        if (patterns.isEmpty()) return

        val highlightedLines = mutableSetOf<Int>()

        for (item in patterns) {
            if (item.pattern.isEmpty()) continue

            val regex = try {
                Regex(item.pattern, RegexOption.IGNORE_CASE)
            } catch (e: Exception) {
                continue
            }

            val results = regex.findAll(text)
            for (match in results) {
                val matchStart = match.range.first
                val matchEnd = match.range.last + 1

                if (matchStart >= text.length) continue

                val lineNumber = document.getLineNumber(matchStart)
                val bgColor = parseColor(item.background)
                val fgColor = parseColor(item.color)

                if (bgColor == null && fgColor == null) continue

                // Highlight the entire line (only once per line)
                if (bgColor != null && lineNumber !in highlightedLines) {
                    val lineStart = document.getLineStartOffset(lineNumber)
                    val lineEnd = document.getLineEndOffset(lineNumber)

                    val lineAttributes = TextAttributes().apply {
                        backgroundColor = bgColor
                    }

                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(TextRange(lineStart, lineEnd))
                        .enforcedTextAttributes(lineAttributes)
                        .create()

                    highlightedLines.add(lineNumber)
                }

                // Highlight the matched text
                val textAttributes = TextAttributes().apply {
                    backgroundColor = bgColor
                    foregroundColor = fgColor ?: if (bgColor != null) getContrastTextColor(bgColor) else null
                }

                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(TextRange(matchStart, matchEnd))
                    .enforcedTextAttributes(textAttributes)
                    .create()
            }
        }
    }

    private fun parseColor(colorStr: String): Color? {
        val trimmed = colorStr.trim()
        if (trimmed.isEmpty()) return null
        return try {
            val hex = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
            ColorUtil.fromHex(hex)
        } catch (e: Exception) {
            null
        }
    }

    private fun getContrastTextColor(backgroundColor: Color): Color {
        val r = backgroundColor.red / 255.0
        val g = backgroundColor.green / 255.0
        val b = backgroundColor.blue / 255.0

        val rLinear = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
        val gLinear = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
        val bLinear = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)

        val luminance = 0.2126 * rLinear + 0.7152 * gLinear + 0.0722 * bLinear
        return if (luminance > 0.5) Color.BLACK else SOFT_WHITE
    }
}
