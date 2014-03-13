import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
                    generateSchema(psiJavaFile);
                }
            }
        });
    }

    private void generateSchema(@NotNull PsiJavaFile psiJavaFile) {
        String className = psiJavaFile.getName() + "Schema";
        String dirPath = project.getBasePath();
        String packageName = psiJavaFile.getPackageName();

        try {
            PsiClass psiClass = psiJavaFile.getClasses()[0];
            String content = new SchemeGenerator().getSchemaContent(psiClass);
            generateSchemeClass(packageName, new File(dirPath), className, content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final VirtualFile genSourceRoot = LocalFileSystem.getInstance().findFileByPath(dirPath + '/' + packageName.replace('.', '/') + "/" + className);
        if (genSourceRoot != null) {
            genSourceRoot.refresh(false, true);
        }
    }

    private void generateSchemeClass(String aPackage, File outputDir, String className, String content) throws IOException {
        final File packageDir = new File(outputDir.getPath() + '/' + aPackage.replace('.', '/'));
        if (!packageDir.exists() && !packageDir.mkdirs()) {
            throw new IOException("Cannot create directory " + FileUtil.toSystemDependentName(packageDir.getPath()));
        }
        final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(packageDir, className)));
        try {
            writer.write(content);
        } finally {
            writer.close();
        }
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
