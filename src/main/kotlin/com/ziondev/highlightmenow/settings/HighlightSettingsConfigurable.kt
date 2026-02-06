package com.ziondev.highlightmenow.settings

import com.ziondev.highlightmenow.HighlightMeNowBundle
import com.intellij.openapi.options.Configurable
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.table.AbstractTableModel

class HighlightSettingsConfigurable : Configurable {
    private var settingsPanel: JPanel? = null
    private val patterns = mutableListOf<HighlightPattern>()
    private val tableModel = HighlightTableModel(patterns)
    private val table = JBTable(tableModel)

    override fun getDisplayName(): String = HighlightMeNowBundle.message("settings.display.name")

    override fun createComponent(): JComponent {
        val panel = JPanel(BorderLayout())
        
        val decorator = ToolbarDecorator.createDecorator(table)
            .setAddAction {
                patterns.add(HighlightPattern(HighlightMeNowBundle.message("settings.new.pattern.name"), "#000000", "#FFFFFF"))
                tableModel.fireTableRowsInserted(patterns.size - 1, patterns.size - 1)
            }
            .setRemoveAction {
                val selectedRow = table.selectedRow
                if (selectedRow != -1) {
                    patterns.removeAt(selectedRow)
                    tableModel.fireTableRowsDeleted(selectedRow, selectedRow)
                }
            }
            .disableUpDownActions()

        panel.add(decorator.createPanel(), BorderLayout.CENTER)
        settingsPanel = panel
        return panel
    }

    override fun isModified(): Boolean {
        val savedPatterns = HighlightSettingsState.getInstance().patterns
        return patterns != savedPatterns
    }

    override fun apply() {
        HighlightSettingsState.getInstance().patterns = patterns.map { it.copy() }.toMutableList()
    }

    override fun reset() {
        patterns.clear()
        patterns.addAll(HighlightSettingsState.getInstance().patterns.map { it.copy() })
        tableModel.fireTableDataChanged()
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }

    private class HighlightTableModel(val patterns: List<HighlightPattern>) : AbstractTableModel() {
        private val columnNames = arrayOf(
            HighlightMeNowBundle.message("settings.column.pattern"),
            HighlightMeNowBundle.message("settings.column.color"),
            HighlightMeNowBundle.message("settings.column.background")
        )

        override fun getRowCount(): Int = patterns.size
        override fun getColumnCount(): Int = columnNames.size
        override fun getColumnName(column: Int): String = columnNames[column]
        
        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val pattern = patterns[rowIndex]
            return when (columnIndex) {
                0 -> pattern.pattern
                1 -> pattern.color
                2 -> pattern.background
                else -> ""
            }
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = true

        override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
            val pattern = patterns[rowIndex]
            val value = aValue?.toString() ?: ""
            when (columnIndex) {
                0 -> pattern.pattern = value
                1 -> pattern.color = value
                2 -> pattern.background = value
            }
            fireTableCellUpdated(rowIndex, columnIndex)
        }
    }
}
