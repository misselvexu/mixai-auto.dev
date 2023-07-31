package cc.unitmesh.devti.provider

import cc.unitmesh.devti.context.FileContext
import com.intellij.lang.Language
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.serviceContainer.LazyExtensionInstance
import com.intellij.util.xmlb.annotations.Attribute

data class TestFileContext(
    val isNewFile: Boolean,
    val file: VirtualFile,
    val relatedFiles: List<FileContext> = emptyList(),
    val testClassName: String?,
    val language: Language,
)

abstract class WriteTestService : LazyExtensionInstance<WriteTestService>() {
    @Attribute("language")
    var language: String? = null

    @Attribute("implementation")
    var implementationClass: String? = null

    override fun getImplementationClassName(): String? {
        return implementationClass
    }

    abstract fun isApplicable(element: PsiElement): Boolean

    abstract fun findOrCreateTestFile(sourceFile: PsiFile, project: Project, element: PsiElement): TestFileContext?
    abstract fun lookupRelevantClass(project: Project, element: PsiElement): List<FileContext>
    open fun runTest(project: Project, virtualFile: VirtualFile) {}

    companion object {
        private val EP_NAME: ExtensionPointName<WriteTestService> =
            ExtensionPointName.create("cc.unitmesh.testContextProvider")

        fun context(psiElement: PsiElement): WriteTestService? {
            val lang = psiElement.language.displayName
            val extensionList = EP_NAME.extensionList
            val providers = extensionList.filter {
                it.language?.lowercase() == lang.lowercase() && it.isApplicable(psiElement)
            }

            return providers.firstOrNull()
        }
    }
}