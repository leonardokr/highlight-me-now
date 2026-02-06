package com.ziondev.highlightmenow.highlight

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.ziondev.highlightmenow.settings.HighlightPattern
import com.ziondev.highlightmenow.settings.HighlightSettingsState
import com.intellij.lang.annotation.HighlightSeverity

class HighlightAnnotatorTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        HighlightSettingsState.getInstance().patterns = mutableListOf(
            HighlightPattern("FIX", "#FFFFFF", "#FF0000"),
            HighlightPattern("TODO", "#000000", "#FFFF00")
        )
    }

    fun testHighlightTodo() {
        myFixture.configureByText("test.txt", "// TODO: fix this")
        val highlights = myFixture.doHighlighting()
        
        println("Highlights found: ${highlights.size}")
        highlights.forEach { 
            println("Highlight: text='${it.text}', severity=${it.severity}, forcedAttributes=${it.forcedTextAttributes}")
        }

        val hasTodo = highlights.any { it.text?.contains("TODO") == true }
        println("Has TODO highlight: $hasTodo")
        assertTrue("Deveria ter encontrado destaque para TODO", hasTodo)
    }

    fun testHighlightKotlin() {
        val text = "fun main() { // FIX: something }\nsegunda linha"
        myFixture.configureByText("test.kt", text)
        val highlights = myFixture.doHighlighting()
        
        println("Kotlin Highlights found: ${highlights.size}")
        highlights.forEach { 
            println("Kotlin Highlight: text='${it.text}', range=${it.startOffset}-${it.endOffset}, severity=${it.severity}, forcedAttributes=${it.forcedTextAttributes}")
        }

        val hasFix = highlights.any { it.text?.contains("FIX") == true }
        // A primeira linha termina antes do \n
        val firstLineEnd = text.indexOf('\n')
        val hasLineHighlight = highlights.any { it.startOffset == 0 && it.endOffset == firstLineEnd }
        
        println("Has FIX highlight: $hasFix")
        println("Has Line highlight: $hasLineHighlight")
        
        assertTrue("Deveria ter encontrado destaque para FIX em Kotlin", hasFix)
        assertTrue("Deveria ter encontrado destaque para a linha inteira", hasLineHighlight)
    }
}
