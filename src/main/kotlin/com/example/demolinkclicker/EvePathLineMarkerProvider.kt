package com.example.demolinkclicker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyStringLiteralExpression
import javax.swing.Icon
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.util.Function
import java.awt.Desktop
import java.io.File
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir

class EvePathLineMarkerProvider : LineMarkerProvider {
    private data class PathInfo(
        val url: String,
        val icon: Icon,
        val tooltip: String,
        val isLocalFile: Boolean = false
    )

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Only process Python string literals
        if (element !is PyStringLiteralExpression) {
            return null
        }

        val stringContent = element.stringValue
        
        // Check if it matches any of our supported patterns
        if (!isSupported(stringContent)) {
            return null
        }

        // Get path information based on the pattern
        val pathInfo = getPathInfo(stringContent, element.project) ?: return null

        // Create a line marker with navigation handler
        return createLineMarker(element, pathInfo)
    }

    private fun isSupported(path: String): Boolean {
        return path.startsWith("/EVE/UI/") || 
               path.startsWith("UI/") ||
               path.startsWith("/Carbon/") ||
               path.startsWith("res:/")
    }

    private fun getPathInfo(path: String, project: Project): PathInfo? {
        return when {
            path.startsWith("res:/") -> handleResourcePath(path, project)
            path.startsWith("/Carbon/") ||path.startsWith("/EVE/UI/") || path.startsWith("UI/") -> handleLocalizationPath(path)
            else -> null
        }
    }

    private fun handleResourcePath(path: String, project: Project): PathInfo {
        val relativePath = path.removePrefix("res:/")
        val projectDir = project.guessProjectDir()?.path
        val fullPath = "$projectDir/eve/client/res/$relativePath"
        
        return PathInfo(
            url = fullPath,
            icon = AllIcons.FileTypes.Any_type,
            tooltip = "Open resource file: $fullPath",
            isLocalFile = true
        )
    }

    private fun handleLocalizationPath(path: String): PathInfo {
        return PathInfo(
            url = "http://localhost:8000/localization/editlabel/$path",
            icon = AllIcons.General.Web,
            tooltip = "Edit/Create Label in FSD editor"
        )
    }

    private fun createLineMarker(
        element: PsiElement,
        pathInfo: PathInfo
    ): LineMarkerInfo<PsiElement> {
        return LineMarkerInfo(
            element,
            element.textRange,
            pathInfo.icon,
            Function { pathInfo.tooltip },
            { _, _ -> openPath(pathInfo) },
            GutterIconRenderer.Alignment.LEFT,
            { pathInfo.tooltip }
        )
    }

    private fun openPath(pathInfo: PathInfo) {
        if (pathInfo.isLocalFile) {
            val file = File(pathInfo.url)
            if (file.exists()) {
                Desktop.getDesktop().open(file)
            }
        } else {
            BrowserUtil.browse(pathInfo.url)
        }
    }
} 