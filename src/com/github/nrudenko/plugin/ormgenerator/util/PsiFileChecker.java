package com.github.nrudenko.plugin.ormgenerator.util;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiModifierListOwner;
import org.jetbrains.annotations.NotNull;

public class PsiFileChecker {
    private static final String TABLE_NAME = "com.github.nrudenko.orm.annotation.Table";

    public static boolean isTableModel(@NotNull PsiFile psiFile) {
        boolean result = false;
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            PsiClass psiClass = psiJavaFile.getClasses()[0];
            if (psiClass instanceof PsiModifierListOwner) {
                result = AnnotationUtil.isAnnotated(psiClass, TABLE_NAME, false);
            }
        }
        return result;
    }
}
