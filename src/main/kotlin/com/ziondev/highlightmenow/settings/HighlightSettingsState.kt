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
        HighlightPattern("FIX", "#5C4033", "#F8B4B4"),
        HighlightPattern("TODO", "#5C4033", "#FEF3C7"),
        HighlightPattern("WARN", "#5C4033", "#FED7AA"),
        HighlightPattern("OBS", "#1E3A5F", "#BFDBFE"),
        HighlightPattern("QUESTION", "#4C1D95", "#DDD6FE")
    )
    var highlightEntireLine: Boolean = true

    override fun getState(): HighlightSettingsState = this

    override fun loadState(state: HighlightSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): HighlightSettingsState = service()
    }
}
