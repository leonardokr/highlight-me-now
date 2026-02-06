package com.ziondev.highlightmenow.highlight

import com.ziondev.highlightmenow.settings.HighlightSettingsState
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement
import com.intellij.ui.ColorUtil
import com.intellij.psi.PsiFile
import com.intellij.openapi.util.TextRange

import com.intellij.openapi.util.Key
import java.awt.Color

class HighlightAnnotator : Annotator {
    companion object {
        private val HIGHLIGHTED_LINES_KEY = Key.create<MutableSet<Int>>("com.ziondev.highlightmenow.HIGHLIGHTED_LINES")
        private val SOFT_WHITE = Color(240, 240, 240)
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Ignoramos o PsiFile para evitar re-processar o arquivo inteiro múltiplas vezes.
        // O IntelliJ chamará o anotador para cada elemento PSI.
        // Processamos apenas elementos folha (sem filhos) que contêm o texto.
        if (element is PsiFile || element.firstChild != null) return

        val text = element.text ?: return
        if (text.isEmpty()) return
        
        val patterns = HighlightSettingsState.getInstance().patterns
        if (patterns.isEmpty()) return

        val document = element.containingFile.viewProvider.document ?: return
        val session = holder.currentAnnotationSession
        var highlightedLines = session.getUserData(HIGHLIGHTED_LINES_KEY)
        if (highlightedLines == null) {
            highlightedLines = mutableSetOf()
            session.putUserData(HIGHLIGHTED_LINES_KEY, highlightedLines)
        }
        
        val elementRange = element.textRange
        
        for (item in patterns) {
            if (item.pattern.isEmpty()) continue
            
            val regex = try {
                Regex(item.pattern, RegexOption.IGNORE_CASE)
            } catch (e: Exception) {
                null
            } ?: continue

            val results = regex.findAll(text)
            for (match in results) {
                val matchStartInFile = elementRange.startOffset + match.range.first
                val matchEndInFile = elementRange.startOffset + match.range.last + 1
                
                if (matchStartInFile >= document.textLength) continue

                val matchRange = TextRange(matchStartInFile, matchEndInFile)
                val lineNumber = document.getLineNumber(matchStartInFile)

                val bgColor = parseColor(item.background)
                val fgColor = parseColor(item.color)

                if (bgColor == null && fgColor == null) continue

                // Destaque da linha inteira
                if (bgColor != null && !highlightedLines.contains(lineNumber)) {
                    val lineStart = document.getLineStartOffset(lineNumber)
                    val lineEnd = document.getLineEndOffset(lineNumber)
                    val lineRange = TextRange(lineStart, lineEnd)
                    
                    val lineAttributes = TextAttributes().apply {
                        backgroundColor = bgColor
                    }

                    holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                        .range(lineRange)
                        .enforcedTextAttributes(lineAttributes)
                        .afterEndOfLine()
                        .create()
                    
                    highlightedLines.add(lineNumber)
                }

                // Destaque do texto específico
                val textAttributes = TextAttributes().apply {
                    backgroundColor = bgColor
                    foregroundColor = fgColor ?: if (bgColor != null) getContrastTextColor(bgColor) else null
                }

                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                    .range(matchRange)
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
