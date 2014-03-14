package com.github.nrudenko.plugin.ormgenerator;

import com.github.nrudenko.plugin.ormgenerator.util.SchemeGenerator;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class OrmModelListener implements Disposable {

    private static final String ORM_MODEL_QUALIFIED_NAME = "com.github.nrudenko.orm.OrmModel";

    private final Project project;
    private PsiClass ormPsiClass;

    public OrmModelListener(@NotNull final Project project) {
        this.project = project;
        StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
            @Override
            public void run() {
                ormPsiClass = JavaPsiFacade.getInstance(project).findClass(ORM_MODEL_QUALIFIED_NAME, GlobalSearchScope.projectScope(project));
            }
        });

        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
            @Override
            public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
                super.childrenChanged(event);
                PsiFile psiFile = event.getFile();
                if (isOrmFile(psiFile)) {
                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                    SchemeGenerator.getInstance().generateSchema(project, psiJavaFile);
                }
            }
        });
    }

    private boolean isOrmFile(@NotNull PsiFile psiFile) {
        boolean result = false;
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            PsiClass[] classes = psiJavaFile.getClasses();
            result = classes[0].isInheritor(ormPsiClass, true);
        }
        return result;
    }

    @Override
    public void dispose() {

    }
}
