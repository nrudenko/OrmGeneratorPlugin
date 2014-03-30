package com.github.nrudenko.plugin.ormgenerator.util;

import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLClassLoader;

public class CompileSourceInMemory {

    public static Class compile(@NotNull PsiClass psiClass) {
        Class cls = null;

        String compileText = psiClass.getText();
        String compileName = psiClass.getName();
        CompilerProjectExtension extension = CompilerProjectExtension.getInstance(psiClass.getProject());
        String outputRootUrl = extension.getCompilerOutputUrl();
        File root = new File(outputRootUrl + "/tmp");
        File sourceFile = new File(root, compileName + ".java");
        sourceFile.getParentFile().mkdirs();
        try {
            new FileWriter(sourceFile).append(compileText).close();

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(null, null, null, sourceFile.getPath());

            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
            cls = Class.forName(compileName, true, classLoader);
            sourceFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cls;
    }
}

