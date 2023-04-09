package com.github.blarc.gitlab.template.lint.plugin

import com.github.blarc.gitlab.template.lint.plugin.pipeline.Pipeline
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.psi.PsiFile
import com.intellij.ui.EditorNotifications


fun runLinting(file: PsiFile) {
    if (GitlabLintUtils.isGitlabYaml(file.virtualFile)) {
        runBackgroundableTask(GitlabLintBundle.message("inspection.title"), file.project) { indicator ->
            indicator.isIndeterminate = true
            val project = file.project
            val pipeline = project.service<Pipeline>()
            pipeline.runLint(file)
            EditorNotifications.getInstance(project).updateAllNotifications()
        }
    }
}
