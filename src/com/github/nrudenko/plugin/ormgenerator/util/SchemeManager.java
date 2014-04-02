package com.github.nrudenko.plugin.ormgenerator.util;

import com.github.nrudenko.orm.commons.DbType;
import com.github.nrudenko.orm.commons.FieldType;
import com.github.nrudenko.plugin.ormgenerator.model.SchemeColumn;
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
    static final String COLUMN_ADDITIONAL = "customAdditional";
    public static final String COLUMN_COMMENT = "column_comment";
    private String v;

    enum OrmAnnotation {
        DbColumn, SkipFieldInDb, VirtualColumn
    }

    public static Scheme getScheme(@NotNull PsiJavaFile psiJavaFile, String schemePackage) {
        PsiClass psiClass = psiJavaFile.getClasses()[0];

        Scheme scheme = new Scheme();

        scheme.setTableName(psiClass.getName());
        scheme.setSchemePackage(schemePackage);
        scheme.setColumnList(getColumns(psiClass));

        return scheme;
    }

    private static List<SchemeColumn> getColumns(@NotNull PsiClass psiClass) {
        List<SchemeColumn> result = new ArrayList<SchemeColumn>();
        SchemeColumn id = new SchemeColumn("_id", DbType.INT);
        id.setComments("PRIMARY AUTOINCREMENT");
        result.add(id);

        PsiField[] allFields = psiClass.getAllFields();
        for (int i = 0; i < allFields.length; i++) {
            PsiField field = allFields[i];
            String tableName = psiClass.getName();
            SchemeColumn column = getColumn(field);
            if (column != null) {
                result.add(column);
            }
        }
        return result;
    }

    private static SchemeColumn getColumn(PsiField field) {
        if (isStaticField(field)) {
            return null;
        }

        HashMap<String, String> columnParams = new HashMap<String, String>();

        FieldType fieldType;
        PsiClass fieldPsiClass = getFieldPsiClass(field);
        if (fieldPsiClass != null && fieldPsiClass.isEnum()) {
            fieldType = FieldType.ENUM;
        } else {
            String typeName = field.getType().getPresentableText();
            fieldType = FieldType.byTypeName(typeName);
        }

        String fieldName = field.getName();

        SchemeColumn result = null;

        if (fieldType != null && StringUtils.isNotEmpty(fieldName)) {
            columnParams.put(COLUMN_NAME, fieldName);
            columnParams.put(COLUMN_TYPE, fieldType.getDbType().name());
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
                    case VirtualColumn:
                        columnParams.put(COLUMN_COMMENT, "virtual column");
                        break;
                }
            }
            result = new SchemeColumn(columnParams.get(COLUMN_NAME), DbType.valueOf(columnParams.get(COLUMN_TYPE)));
            String columnAdditional = columnParams.get(COLUMN_ADDITIONAL);
            if (StringUtils.isNotEmpty(columnAdditional)) {
                result.setCustomAdditional(columnAdditional);
            }
            String columnComment = columnParams.get(COLUMN_COMMENT);
            if (StringUtils.isNotEmpty(columnComment)) {
                result.setComments(columnComment);
            }
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

                if (v.startsWith("DbType.") && v.length() > "DbType.".length()) {
                    String[] split = v.split(".");
                    v = split[1];
                }
            }
            String name = attribute.getName();
            columnParams.put(name, v);
            if(name!=null && name.equals("additional")){
                columnParams.put(COLUMN_COMMENT, v);
            }
        }
    }

}
