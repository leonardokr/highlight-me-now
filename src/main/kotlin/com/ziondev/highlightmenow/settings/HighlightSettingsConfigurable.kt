package com.ziondev.highlightmenow.settings

import com.ziondev.highlightmenow.HighlightMeNowBundle
import com.intellij.openapi.options.Configurable
import com.intellij.ui.ColorUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.table.JBTable
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

class HighlightSettingsConfigurable : Configurable {
    private var settingsPanel: JPanel? = null
    private val patterns = mutableListOf<HighlightPattern>()
    private val tableModel = HighlightTableModel(patterns)
    private val table = JBTable(tableModel)
    private val highlightEntireLineCheckbox = JBCheckBox(
        HighlightMeNowBundle.message("settings.highlight.entire.line")
    )

    override fun getDisplayName(): String = HighlightMeNowBundle.message("settings.display.name")

    override fun createComponent(): JComponent {
        val panel = JPanel(BorderLayout())

        // Setup color columns with renderer and click handler
        table.columnModel.getColumn(1).cellRenderer = ColorCellRenderer()
        table.columnModel.getColumn(2).cellRenderer = ColorCellRenderer()

        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val row = table.rowAtPoint(e.point)
                val col = table.columnAtPoint(e.point)
                if (row >= 0 && (col == 1 || col == 2)) {
                    val currentValue = table.getValueAt(row, col) as String
                    val currentColor = parseColor(currentValue)
                    val newColor = JColorChooser.showDialog(
                        panel,
                        if (col == 1) "Choose Text Color" else "Choose Background Color",
                        currentColor
                    )
                    if (newColor != null) {
                        val hexColor = "#${ColorUtil.toHex(newColor).uppercase()}"
                        table.setValueAt(hexColor, row, col)
                    }
                }
            }
        })

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

        val optionsPanel = JPanel()
        optionsPanel.layout = BoxLayout(optionsPanel, BoxLayout.Y_AXIS)
        optionsPanel.border = BorderFactory.createEmptyBorder(10, 0, 0, 0)
        optionsPanel.add(highlightEntireLineCheckbox)

        panel.add(optionsPanel, BorderLayout.SOUTH)

        settingsPanel = panel
        return panel
    }

    override fun isModified(): Boolean {
        val state = HighlightSettingsState.getInstance()
        return patterns != state.patterns ||
               highlightEntireLineCheckbox.isSelected != state.highlightEntireLine
    }

    override fun apply() {
        val state = HighlightSettingsState.getInstance()
        state.patterns = patterns.map { it.copy() }.toMutableList()
        state.highlightEntireLine = highlightEntireLineCheckbox.isSelected
    }

    override fun reset() {
        val state = HighlightSettingsState.getInstance()
        patterns.clear()
        patterns.addAll(state.patterns.map { it.copy() })
        tableModel.fireTableDataChanged()
        highlightEntireLineCheckbox.isSelected = state.highlightEntireLine
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }

    private fun parseColor(colorStr: String): Color {
        val trimmed = colorStr.trim()
        return try {
            ColorUtil.fromHex(if (trimmed.startsWith("#")) trimmed else "#$trimmed")
        } catch (e: Exception) {
            Color.WHITE
        }
    }

    private class ColorCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            val colorStr = value?.toString() ?: ""
            val color = try {
                val trimmed = colorStr.trim()
                ColorUtil.fromHex(if (trimmed.startsWith("#")) trimmed else "#$trimmed")
            } catch (e: Exception) {
                Color.WHITE
            }
            component.background = color
            component.foreground = getContrastColor(color)
            (component as? JLabel)?.text = colorStr.uppercase()
            return component
        }

        private fun getContrastColor(bg: Color): Color {
            val luminance = (0.299 * bg.red + 0.587 * bg.green + 0.114 * bg.blue) / 255
            return if (luminance > 0.5) Color.BLACK else Color.WHITE
        }
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
