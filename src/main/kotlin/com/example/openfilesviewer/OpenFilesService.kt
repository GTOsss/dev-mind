package com.example.openfilesviewer

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class OpenFilesService(private val project: Project) {
    
    private var panel: OpenFilesPanel? = null
    
    fun setPanel(panel: OpenFilesPanel) {
        this.panel = panel
    }
    
    fun refreshPanel() {
        panel?.refreshFilesList()
    }
    
    fun getPanel(): OpenFilesPanel? = panel
}