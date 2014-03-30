package com.github.nrudenko.plugin.ormgenerator;

import com.github.nrudenko.plugin.ormgenerator.model.Scheme;
import com.github.nrudenko.plugin.ormgenerator.util.PsiFileChecker;
import com.github.nrudenko.plugin.ormgenerator.util.SchemeFileGenerator;
import com.github.nrudenko.plugin.ormgenerator.util.SchemeManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class OrmModelListener implements Disposable {

    private static final String ORM_MODEL_QUALIFIED_NAME = "com.github.nrudenko.orm.OrmModel";

    private final Project project;

    public OrmModelListener(@NotNull final Project project) {
        this.project = project;

        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
            @Override
            public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {
                super.beforeChildRemoval(event);
                PsiFile psiFile = event.getChild().getContainingFile();
                if (psiFile != null && isOrmFile(psiFile)) {
                    new File(psiFile.getVirtualFile().getPath()).delete();
                }
            }

            @Override
            public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {
                super.beforeChildMovement(event);
            }

            @Override
            public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {
                super.beforeChildReplacement(event);
            }

            @Override
            public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
                super.childrenChanged(event);
                PsiFile psiFile = event.getFile();
                if (isOrmFile(psiFile)) {
                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                    Scheme scheme = SchemeManager.getScheme(psiJavaFile, psiJavaFile.getPackageName() + ".scheme");
                    final VirtualFile sourceRoot =
                            ProjectFileIndex.SERVICE.getInstance(project).getSourceRootForFile(psiFile.getVirtualFile());
                    SchemeFileGenerator.generate(scheme, sourceRoot);
                }
            }
        });
    }

    private boolean isOrmFile(@NotNull PsiFile psiFile) {
        return PsiFileChecker.isTableModel(psiFile);
    }

    @Override
    public void dispose() {

    }
}
