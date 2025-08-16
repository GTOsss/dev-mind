package com.example.openfilesviewer

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class RefreshOpenFilesAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project? = e.project
        project?.let {
            val service = it.service<OpenFilesService>()
            service.refreshPanel()
        }
    }
    
    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }
}