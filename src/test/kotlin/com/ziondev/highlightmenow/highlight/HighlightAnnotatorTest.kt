package com.ziondev.highlightmenow.highlight

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.ziondev.highlightmenow.settings.HighlightPattern
import com.ziondev.highlightmenow.settings.HighlightSettingsState

class HighlightAnnotatorTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        HighlightSettingsState.getInstance().patterns = mutableListOf(
            HighlightPattern("FIX", "#FFFFFF", "#FF0000"),
            HighlightPattern("TODO", "#000000", "#FFFF00")
        )
    }

    fun testSettingsState() {
        val state = HighlightSettingsState.getInstance()
        assertEquals(2, state.patterns.size)
        assertEquals("FIX", state.patterns[0].pattern)
        assertEquals("TODO", state.patterns[1].pattern)
    }

    fun testPatternMatching() {
        val text = "// TODO: fix this issue"
        val pattern = HighlightSettingsState.getInstance().patterns[1]
        val regex = Regex(pattern.pattern, RegexOption.IGNORE_CASE)

        assertTrue("Pattern should match TODO", regex.containsMatchIn(text))

        val match = regex.find(text)
        assertNotNull(match)
        assertEquals("TODO", match!!.value)
    }

    fun testHighlightEntireLineDefault() {
        val state = HighlightSettingsState.getInstance()
        assertTrue("Default should be highlight entire line", state.highlightEntireLine)
    }

    fun testPatternColors() {
        val fixPattern = HighlightSettingsState.getInstance().patterns[0]
        assertEquals("#FFFFFF", fixPattern.color)
        assertEquals("#FF0000", fixPattern.background)
    }
}
