package com.example.openfilesviewer

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.messages.MessageBusConnection
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.nio.charset.StandardCharsets
import javax.swing.*

class OpenFilesPanel(private val project: Project) {

    private val listModel = DefaultListModel<VirtualFile>()
    private val filesList = JBList(listModel)
    private val mainPanel = JPanel(BorderLayout())
    private var messageBusConnection: MessageBusConnection? = null

    init {
        setupUI()
        setupFileEditorListener()
        refreshFilesList()
    }

    private fun setupUI() {
        filesList.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): java.awt.Component {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

                if (value is VirtualFile) {
                    text = value.name
                    toolTipText = value.path
                    icon = value.fileType.icon
                }

                return this
            }
        }

        // Добавляем возможность открыть файл по двойному клику
        filesList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val selectedFile = filesList.selectedValue
                    if (selectedFile != null) {
                        FileEditorManager.getInstance(project).openFile(selectedFile, true)
                    }
                }
            }
        })

        val scrollPane = JBScrollPane(filesList)

        // Создаем панель с кнопками
        val buttonPanel = JPanel(FlowLayout()).apply {
            // Кнопка обновления
            val refreshButton = JButton("Refresh").apply {
                addActionListener { refreshFilesList() }
            }

            // Кнопка предварительного просмотра
            val previewButton = JButton("Preview").apply {
                addActionListener { createPreviewFile() }
            }

            add(refreshButton)
            add(previewButton)
        }

        mainPanel.add(JLabel("Open Files:"), BorderLayout.NORTH)
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        mainPanel.add(buttonPanel, BorderLayout.SOUTH)
    }

    private fun setupFileEditorListener() {
        messageBusConnection = project.messageBus.connect()
        messageBusConnection?.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                SwingUtilities.invokeLater { refreshFilesList() }
            }

            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                SwingUtilities.invokeLater { refreshFilesList() }
            }

            override fun selectionChanged(event: FileEditorManagerEvent) {
                SwingUtilities.invokeLater { refreshFilesList() }
            }
        })
    }

    fun refreshFilesList() {
        val fileEditorManager = FileEditorManager.getInstance(project)
        val openFiles = fileEditorManager.openFiles

        SwingUtilities.invokeLater {
            listModel.clear()
            openFiles.forEach { file ->
                listModel.addElement(file)
            }
        }
    }

    private fun createPreviewFile() {
        ApplicationManager.getApplication().runWriteAction {
            try {
                val fileEditorManager = FileEditorManager.getInstance(project)
                val openFiles = fileEditorManager.openFiles

                if (openFiles.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        mainPanel,
                        "No open files to preview",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE
                    )
                    return@runWriteAction
                }

                // Генерируем markdown контент
                val markdownContent = generateMarkdownContent(openFiles)

                // Создаем временный файл
                val tempFile = File.createTempFile("open_files_preview_", ".md")
                tempFile.writeText(markdownContent, StandardCharsets.UTF_8)

                // Конвертируем в VirtualFile и открываем
                val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tempFile)
                virtualFile?.let { file ->
                    ApplicationManager.getApplication().invokeLater {
                        FileEditorManager.getInstance(project).openFile(file, true)
                    }
                }

            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    mainPanel,
                    "Error creating preview: ${e.message}",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }

    private fun generateMarkdownContent(files: Array<VirtualFile>): String {
        val sb = StringBuilder()

        sb.appendLine("# Open Files Preview")
        sb.appendLine()
        sb.appendLine("Generated on: ${java.time.LocalDateTime.now()}")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        files.forEach { file ->
            try {
                sb.appendLine("## ${file.name}")
                sb.appendLine("**Path:** `${file.path}`")
                sb.appendLine()
                sb.appendLine("```${getLanguageFromExtension(file.extension)}")

                // Читаем содержимое файла
                val content = String(file.contentsToByteArray(), StandardCharsets.UTF_8)
                sb.appendLine(content)

                sb.appendLine("```")
                sb.appendLine()
                sb.appendLine("---")
                sb.appendLine()
            } catch (e: Exception) {
                sb.appendLine("*Error reading file: ${e.message}*")
                sb.appendLine()
                sb.appendLine("---")
                sb.appendLine()
            }
        }

        return sb.toString()
    }

    private fun getLanguageFromExtension(extension: String?): String {
        return when (extension?.lowercase()) {
            "kt" -> "kotlin"
            "java" -> "java"
            "js" -> "javascript"
            "ts" -> "typescript"
            "py" -> "python"
            "html" -> "html"
            "css" -> "css"
            "json" -> "json"
            "xml" -> "xml"
            "yml", "yaml" -> "yaml"
            "md" -> "markdown"
            "sh" -> "bash"
            "sql" -> "sql"
            "php" -> "php"
            "rb" -> "ruby"
            "go" -> "go"
            "rs" -> "rust"
            "cpp", "cc", "cxx" -> "cpp"
            "c" -> "c"
            "cs" -> "csharp"
            "swift" -> "swift"
            "dart" -> "dart"
            else -> "text"
        }
    }

    fun getContent(): JComponent = mainPanel

    fun dispose() {
        messageBusConnection?.disconnect()
    }
}