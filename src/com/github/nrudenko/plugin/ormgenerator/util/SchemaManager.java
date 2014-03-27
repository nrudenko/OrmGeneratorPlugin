package com.github.nrudenko.plugin.ormgenerator.util;

import com.github.nrudenko.orm.commons.Column;
import com.github.nrudenko.orm.commons.FieldType;
import com.github.nrudenko.plugin.ormgenerator.model.Schema;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SchemaManager {

    static final String COLUMN_NAME = "name";
    static final String COLUMN_TYPE = "type";

    enum OrmAnnotation {
        DbColumn, SkipFieldInDb
    }

    private static SchemaManager instance;
    private final Project project;

    private SchemaManager(Project project) {
        this.project = project;
    }

    public static SchemaManager getInstance(Project project) {
        if (instance == null) {
            instance = new SchemaManager(project);
        }
        return instance;
    }

    public Schema getSchema(@NotNull PsiJavaFile psiJavaFile) {
        PsiClass psiClass = psiJavaFile.getClasses()[0];

        Schema schema = new Schema();

        schema.setName(psiClass.getName() + "Schema");
        schema.setSchemaPackage(psiJavaFile.getPackageName() + ".schema");
        schema.setPackageDirPath(psiJavaFile.getVirtualFile().getParent().getPath() + "/schema");
        schema.setColumnList(getColumns(psiClass));
        return schema;
    }

    private List<Column> getColumns(@NotNull PsiClass psiClass) {
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

    private Column getColumn(PsiField field) {
        if (isStaticField(field)) {
            return null;
        }
        HashMap<String, String> columnParams = new HashMap<String, String>();

        String typeName = field.getType().getPresentableText();
        FieldType fieldType = FieldType.byTypeName(typeName);
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

    private boolean isStaticField(PsiField field) {
        PsiModifierList modifierList = field.getModifierList();
        return modifierList.hasModifierProperty("static");
    }

    private void parseDbColumnAnnotation(HashMap<String, String> columnParams, PsiAnnotation annotation) {
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
