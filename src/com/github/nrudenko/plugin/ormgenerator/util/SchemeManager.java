package com.github.nrudenko.plugin.ormgenerator.util;

import com.github.nrudenko.orm.commons.DbType;
import com.github.nrudenko.orm.commons.FieldType;
import com.github.nrudenko.plugin.ormgenerator.model.Scheme;
import com.github.nrudenko.plugin.ormgenerator.model.SchemeColumn;
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
    static final String COLUMN_ADDITIONAL = "additional";
    public static final String COLUMN_COMMENT = "column_comment";
    private static final String IS_VIRTUAL = "is_virtual";
    private static final String SHOULD_SKIP = "should_skip";

    enum OrmAnnotation {
        DbColumn, DbSkipField, VirtualColumn
    }

    public static Scheme getScheme(@NotNull PsiJavaFile psiJavaFile, String schemePackage) {
        PsiClass psiClass = psiJavaFile.getClasses()[0];

        Scheme scheme = new Scheme();

        scheme.setTableName(psiClass.getName());
        scheme.setSchemePackage(schemePackage);

        List<SchemeColumn> columns = getColumns(psiClass);
        SchemeColumn id = new SchemeColumn("_id", DbType.INT);
        id.setComments("PRIMARY AUTOINCREMENT");
        columns.add(0, id);

        scheme.setColumnList(columns);

        return scheme;
    }

    private static List<SchemeColumn> getColumns(@NotNull PsiClass psiClass) {
        List<SchemeColumn> result = new ArrayList<SchemeColumn>();

        PsiField[] allFields = psiClass.getAllFields();
        for (int i = 0; i < allFields.length; i++) {
            PsiField field = allFields[i];
            if (isStaticField(field)) {
                continue;
            }
            SchemeColumn column = getColumn(field);
            if (column != null) {
                result.add(column);
            }
        }
        return result;
    }

    private static SchemeColumn getColumn(PsiField field) {
        FieldType fieldType = getFieldType(field);

        HashMap<String, String> columnParams = new HashMap<String, String>();

        columnParams.put(IS_VIRTUAL, "false");
        columnParams.put(COLUMN_NAME, field.getName());
        if (fieldType != null) {
            columnParams.put(COLUMN_TYPE, fieldType.getDbType().name());
        }

        processAnnotations(field, columnParams);

        if (columnParams.containsKey(SHOULD_SKIP)) {
            return null;
        }

        String name = columnParams.get(COLUMN_NAME);
        String type = columnParams.get(COLUMN_TYPE);

        if (StringUtils.isEmpty(type) && StringUtils.isEmpty(name)) {
            return null;
        }

        String additional = columnParams.get(COLUMN_ADDITIONAL);
        boolean isVirtual = Boolean.parseBoolean(columnParams.get(IS_VIRTUAL));

        SchemeColumn result = new SchemeColumn(name, DbType.valueOf(type));
        result.setVirtual(isVirtual);
        result.setAdditional(additional);

        processComments(result);

        return result;
    }

    private static void processComments(SchemeColumn result) {
        StringBuilder comments = new StringBuilder();
        if (result.isVirtual()) {
            comments.append("virtual column");
        }
        if (StringUtils.isNotEmpty(result.getAdditional())) {
            comments.append(result.getAdditional());
        }
        result.setComments(comments.toString());
    }

    private static FieldType getFieldType(PsiField field) {
        FieldType fieldType;

        PsiClass fieldPsiClass = getFieldPsiClass(field);
        if (fieldPsiClass != null && fieldPsiClass.isEnum()) {
            fieldType = FieldType.ENUM;
        } else {
            String typeName = field.getType().getPresentableText();
            fieldType = FieldType.byTypeName(typeName);
        }

        return fieldType;
    }

    private static void processAnnotations(PsiField field, HashMap<String, String> columnParams) {
        PsiAnnotation[] annotations = field.getModifierList().getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            PsiAnnotation annotation = annotations[i];
            OrmAnnotation ormAnnotation;
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
                case DbSkipField:
                    columnParams.put(SHOULD_SKIP, "true");
                    return;
                case VirtualColumn:
                    columnParams.put(IS_VIRTUAL, "true");
                    break;
            }
        }
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
            } else if (v.startsWith(DbType.class.getSimpleName())) {
                String[] split = v.split("\\.");
                if (split.length > 1) {
                    v = split[1];
                }
            }
            String name = attribute.getName();
            columnParams.put(name, v);
        }
    }

}
