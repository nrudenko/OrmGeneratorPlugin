package com.github.nrudenko.plugin.ormgenerator.util;

import com.github.nrudenko.orm.commons.Column;
import com.github.nrudenko.orm.commons.DbType;
import com.github.nrudenko.orm.commons.FieldType;
import com.github.nrudenko.plugin.ormgenerator.model.Scheme;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SchemeManager {

    static final String COLUMN_NAME = "name";
    static final String COLUMN_TYPE = "type";

    enum OrmAnnotation {
        DbColumn, SkipFieldInDb
    }

    public static Scheme getScheme(@NotNull PsiJavaFile psiJavaFile, String schemePackage) {
        PsiClass psiClass = psiJavaFile.getClasses()[0];

        Scheme scheme = new Scheme();

        scheme.setName(psiClass.getName() + "Scheme");
        scheme.setSchemePackage(schemePackage);
        scheme.setColumnList(getColumns(psiClass));

        scheme.addImport(DbType.class.getName());

        return scheme;
    }

    private static List<Column> getColumns(@NotNull PsiClass psiClass) {
        List<Column> result = new ArrayList<Column>();
        PsiField[] allFields = psiClass.getAllFields();
        for (int i = 0; i < allFields.length; i++) {
            PsiField field = allFields[i];
            Column column = getColumn(field);
            if (column != null) {
                result.add(column);
            }
        }
        return result;
    }

    private static Column getColumn(PsiField field) {
        if (isStaticField(field)) {
            return null;
        }

        HashMap<String, String> columnParams = new HashMap<String, String>();

        FieldType fieldType = null;
        PsiClass fieldPsiClass = getFieldPsiClass(field);
        if (fieldPsiClass != null && fieldPsiClass.isEnum()) {
            fieldType = FieldType.ENUM;
        } else {
            String typeName = field.getType().getPresentableText();
            fieldType = FieldType.byTypeName(typeName);
        }

        String fieldName = field.getName();

        Column result = null;

        if (fieldType != null && !StringUtils.isEmpty(fieldName)) {

            columnParams.put(COLUMN_NAME, fieldName);
            columnParams.put(COLUMN_TYPE, fieldType.getDbTypeReference());
            PsiAnnotation[] annotations = field.getModifierList().getAnnotations();
            for (int i = 0; i < annotations.length; i++) {
                PsiAnnotation annotation = annotations[i];
                OrmAnnotation ormAnnotation = null;
                try {
                    ormAnnotation = OrmAnnotation.valueOf(annotation.getNameReferenceElement().getText());
                } catch (IllegalArgumentException e) {
                    //eat for skipping undefined annotations
                    continue;
                }
                switch (ormAnnotation) {
                    case DbColumn:
                        parseDbColumnAnnotation(columnParams, annotation);
                        break;
                    case SkipFieldInDb:
                        return null;
                }
            }
            result = new Column(columnParams.get(COLUMN_TYPE), columnParams.get(COLUMN_NAME));
        }
        return result;
    }

    private static PsiClass getFieldPsiClass(PsiField field) {
        PsiClass psiClass;
        Project project = field.getProject();
        String typeFullName = field.getType().getInternalCanonicalText();
        psiClass = JavaPsiFacade.getInstance(project).findClass(typeFullName, GlobalSearchScope.projectScope(project));
        return psiClass;
    }

    private static boolean isStaticField(PsiField field) {
        PsiModifierList modifierList = field.getModifierList();
        return modifierList.hasModifierProperty("static");
    }

    private static void parseDbColumnAnnotation(HashMap<String, String> columnParams, PsiAnnotation annotation) {
        PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
        for (int j = 0; j < attributes.length; j++) {
            PsiNameValuePair attribute = attributes[j];
            PsiAnnotationMemberValue value = attribute.getValue();
            String v = value.getText();
            if (value instanceof PsiLiteralExpression) {
                PsiLiteralExpression literalExpression = (PsiLiteralExpression) value;
                v = (String) literalExpression.getValue();
            }
            columnParams.put(attribute.getName(), v);
        }
    }

}
