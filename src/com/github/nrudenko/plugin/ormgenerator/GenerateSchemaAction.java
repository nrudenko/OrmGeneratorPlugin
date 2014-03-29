package com.github.nrudenko.plugin.ormgenerator;

import com.github.nrudenko.plugin.ormgenerator.model.Schema;
import com.github.nrudenko.plugin.ormgenerator.util.PsiFileChecker;
import com.github.nrudenko.plugin.ormgenerator.util.SchemaFileGenerator;
import com.github.nrudenko.plugin.ormgenerator.util.SchemaManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

public class GenerateSchemaAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        Module module = e.getData(DataKeys.MODULE);

        final PsiJavaFile psiFile = (PsiJavaFile) getPsiFile(e);
        final VirtualFile sourceRoot =
                ProjectFileIndex.SERVICE.getInstance(module.getProject()).getSourceRootForFile(psiFile.getVirtualFile());
        System.out.println(sourceRoot.getCanonicalFile());
        PackageChooserPopup packageChooserPopup = new PackageChooserPopup(project, sourceRoot);
        JBPopup popup = packageChooserPopup.createPopup(new PackageChooserPopup.OnPackageSelectedListener() {
            @Override
            public void onPackageSelected(String qualifiedPackageName) {
                Schema schema = SchemaManager.getSchema(psiFile, qualifiedPackageName);
                VirtualFile generatedFile = SchemaFileGenerator.generate(schema, sourceRoot);
                FileEditorManager.getInstance(project).openFile(generatedFile, true);
            }
        });
        popup.showCenteredInCurrentWindow(project);
    }

    private PsiFile getPsiFile(AnActionEvent e) {
        PsiFile psiFile = e.getData(DataKeys.PSI_FILE);
        return psiFile;
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        PsiFile psiFile = getPsiFile(e);
        if (PsiFileChecker.isTableModel(psiFile)) {
            e.getPresentation().setVisible(true);
        } else {
            e.getPresentation().setVisible(false);
        }
    }
}
