import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.intellij.psi.*;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SchemeGenerator {

    private static final String TAG = "SchemeGenerator";

    static final String COLUMN_NAME = "columnName";
    static final String COLUMN_TYPE = "columnType";

    StringBuilder enumStringBuilder = new StringBuilder();

    enum OrmAnnotation {
        DbColumn, SkipFieldInDb
    }

    class Column {
        String columnName;
        String columnType;

        public Column(String columnName, String columnType) {
            this.columnName = columnName;
            this.columnType = columnType;
        }
    }

    public String getSchemaContent(PsiClass psiClass) {

        String template = "";
        try {
            URL url = getClass().getClassLoader().getResource("scheme_template");
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String entityName = psiClass.getName();
        template = template.replaceAll("@ENTITY_NAME@", entityName);

        List<Column> columnList = getColumns(psiClass);

        for (int i = 0; i < columnList.size(); i++) {
            Column column = columnList.get(i);
            addEnumColumn(column.columnType, column.columnName);
        }

        enumStringBuilder.append(";\n");

        template = template.replaceAll("@COLUMNS@", enumStringBuilder.toString());

        return template;
    }

    private List<Column> getColumns(PsiClass psiClass) {
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
        HashMap<String, String> columnParams = new HashMap<String, String>();

        String typeName = field.getType().getInternalCanonicalText();

        columnParams.put(COLUMN_NAME, field.getName());
        columnParams.put(COLUMN_TYPE, typeName);

        PsiAnnotation[] annotations = field.getModifierList().getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            PsiAnnotation annotation = annotations[i];
            OrmAnnotation ormAnnotation = OrmAnnotation.valueOf(annotation.getNameReferenceElement().getText());
            switch (ormAnnotation) {
                case DbColumn:
                    parseDbColumnAnnotation(columnParams, annotation);
                    break;
                case SkipFieldInDb:
                    return null;
            }
        }
        Column result = new Column(columnParams.get(COLUMN_NAME), columnParams.get(COLUMN_TYPE));
        return result;
    }

    private void parseDbColumnAnnotation(HashMap<String, String> columnParams, PsiAnnotation annotation) {
        PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
        for (int j = 0; j < attributes.length; j++) {
            PsiNameValuePair attribute = attributes[j];
            columnParams.put(attribute.getName(), attribute.getValue().getText());
        }
    }

    private void addEnumColumn(String dbType, String name) {
        String nameUpperCase = splitByUpperCase(name).toUpperCase();
        enumStringBuilder
                .append("    ")
                .append(nameUpperCase)
                .append("(\"")
                .append(name).append("\", ")
                .append(dbType)
                .append("),\n");
    }

    public String splitByUpperCase(String string) {
        String[] stringArray = string.split("(?=\\p{Lu})");
        String newStringName = StringUtils.join(stringArray, "_");
        return newStringName;
    }
}
