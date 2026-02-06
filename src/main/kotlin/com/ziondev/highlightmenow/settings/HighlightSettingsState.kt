package com.ziondev.highlightmenow.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil

data class HighlightPattern(
    var pattern: String = "",
    var color: String = "",
    var background: String = ""
)

@State(
    name = "com.ziondev.highlightmenow.settings.HighlightSettingsState",
    storages = [Storage("HighlightMeNowSettings.xml")]
)
class HighlightSettingsState : PersistentStateComponent<HighlightSettingsState> {
    var patterns: MutableList<HighlightPattern> = mutableListOf(
        HighlightPattern("FIX", "#FFFFFF", "#FF0000"),
        HighlightPattern("TODO", "#000000", "#FFFF00"),
        HighlightPattern("WARN", "#000000", "#FFA500"),
        HighlightPattern("OBS", "#FFFFFF", "#0000FF"),
        HighlightPattern("QUESTION", "#FFFFFF", "#800080")
    )

    override fun getState(): HighlightSettingsState = this

    override fun loadState(state: HighlightSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): HighlightSettingsState = service()
    }
}
