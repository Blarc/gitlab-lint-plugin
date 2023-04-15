package com.github.blarc.gitlab.template.lint.plugin.ui.settings

import com.github.blarc.gitlab.template.lint.plugin.GitlabLintBundle.message
import com.github.blarc.gitlab.template.lint.plugin.extensions.createColumn
import com.github.blarc.gitlab.template.lint.plugin.extensions.reportBugLink
import com.github.blarc.gitlab.template.lint.plugin.settings.AppSettings
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ListTableModel
import javax.swing.ListSelectionModel.SINGLE_SELECTION

class GlobsConfigurable : BoundConfigurable(message("settings.globs.group.title")) {
    private var inclusionGlobs = AppSettings.instance.gitlabLintGlobStrings.toMutableList()
    private var exclusionGlobs = AppSettings.instance.exclusionGlobs.toMutableList()

    private val inclusionTableModel = createTableModel(inclusionGlobs)
    private val exclusionTableModel = createTableModel(exclusionGlobs)

    private val inclusionTable = TableView(inclusionTableModel).apply {
        setShowColumns(false)
        setSelectionMode(SINGLE_SELECTION)
    }

    private val exclusionTable = TableView(exclusionTableModel).apply {
        setShowColumns(false)
        setSelectionMode(SINGLE_SELECTION)
    }

    override fun createPanel() = panel {
        row {
            label(message("settings.globs.inclusions.title"))
        }
        row {
            cell(
                ToolbarDecorator.createDecorator(inclusionTable)
                    .setAddAction { addGlob(inclusionGlobs) }
                    .setEditAction { editGlob(inclusionTable, inclusionGlobs) }
                    .setRemoveAction { removeGlob(inclusionTable, inclusionGlobs) }
                    .disableUpAction()
                    .disableDownAction()
                    .createPanel()
            ).align(Align.FILL)
        }.resizableRow()
        row {
            label(message("settings.globs.exclusions.title"))
        }
        row {
            cell(
                ToolbarDecorator.createDecorator(exclusionTable)
                    .setAddAction { addGlob(exclusionGlobs) }
                    .setEditAction { editGlob(exclusionTable, exclusionGlobs) }
                    .setRemoveAction { removeGlob(exclusionTable, exclusionGlobs) }
                    .disableUpAction()
                    .disableDownAction()
                    .createPanel()
            ).align(Align.FILL)
        }.resizableRow()
        row {
            comment(message("settings.globs.comment"))
        }
        row {
            reportBugLink()
        }
    }

    private fun createTableModel(globs: List<String>): ListTableModel<String> = ListTableModel(
        arrayOf(
            createColumn<String>("Glob") { glob -> glob }
        ),
        globs
    )

    private fun addGlob(globs: MutableList<String>) {
        val dialog = GlobDialog()

        if (dialog.showAndGet()) {
            globs.add(dialog.glob)
            refreshTableModel()
        }
    }

    private fun removeGlob(table: TableView<String>, globs: MutableList<String>) {
        val glob = table.selectedObject ?: return
        globs.remove(glob)
        refreshTableModel()
    }

    private fun editGlob(table: TableView<String>, globs: MutableList<String>) {
        val glob = table.selectedObject ?: return

        val dialog = GlobDialog(glob)
        if (dialog.showAndGet()) {
            if (dialog.glob.isEmpty()) {
                globs.remove(glob)
            } else {
                globs.remove(glob)
                globs.add(dialog.glob)
            }
            refreshTableModel()
        }
    }

    private fun refreshTableModel() {
        inclusionTable.model = createTableModel(inclusionGlobs)
        exclusionTable.model = createTableModel(exclusionGlobs)
    }

    override fun reset() {
        super.reset()
        inclusionGlobs = AppSettings.instance.gitlabLintGlobStrings.toMutableList()
        exclusionGlobs = AppSettings.instance.exclusionGlobs.toMutableList()
        refreshTableModel()
    }

    override fun isModified(): Boolean {
        return super.isModified() ||
                inclusionGlobs != AppSettings.instance.gitlabLintGlobStrings ||
                exclusionGlobs != AppSettings.instance.exclusionGlobs
    }

    override fun apply() {
        super.apply()
        AppSettings.instance.gitlabLintGlobStrings = inclusionGlobs
        AppSettings.instance.exclusionGlobs = exclusionGlobs
    }
}

private class GlobDialog(var glob: String = "") : DialogWrapper(false) {
    init {
        title = message("settings.glob.dialog.title")

        if (glob.isEmpty()) {
            setOKButtonText(message("actions.add"))
        } else {
            setOKButtonText(message("actions.update"))
        }
        init()
    }

    override fun createCenterPanel() = panel {
        row {
            label(message("settings.glob.dialog.label"))
            textField()
                .text(glob)
                .bindText({ glob }, { glob = it })
                .focused()
        }
    }

}