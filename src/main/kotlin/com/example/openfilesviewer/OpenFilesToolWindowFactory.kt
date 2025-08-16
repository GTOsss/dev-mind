package com.example.openfilesviewer

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class OpenFilesToolWindowFactory : ToolWindowFactory {
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val openFilesPanel = OpenFilesPanel(project)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(openFilesPanel.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
        
        // Регистрируем панель в сервисе для обновлений
        project.service<OpenFilesService>().setPanel(openFilesPanel)
    }
}