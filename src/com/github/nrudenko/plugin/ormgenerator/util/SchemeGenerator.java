package com.github.nrudenko.plugin.ormgenerator.util;

import com.github.nrudenko.plugin.ormgenerator.model.Column;
import com.github.nrudenko.plugin.ormgenerator.model.Schema;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

public class SchemeGenerator {

    private static final String TAG = "SchemeGenerator";
    private static SchemeGenerator instance;

    private SchemeGenerator() {
    }

    public static SchemeGenerator getInstance() {
        if (instance == null) {
            instance = new SchemeGenerator();
        }
        return instance;
    }

    public VirtualFile generateSchema(@NotNull Project project, @NotNull PsiJavaFile psiJavaFile) {
        Schema schema = SchemaManager.getInstance(project).getSchema(psiJavaFile);

        try {
            generateSchemeClass(schema);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final VirtualFile generatedSchema = LocalFileSystem.getInstance().findFileByPath(schema.getSchemaPath());
        if (generatedSchema != null) {
            generatedSchema.refresh(false, true);
        }
        return generatedSchema;
    }

    private void generateSchemeClass(Schema schema) throws IOException {
        final File packageDir = new File(schema.getSchemaDir());

        if (!packageDir.exists() && !packageDir.mkdirs()) {
            throw new IOException("Cannot create directory " + FileUtil.toSystemDependentName(packageDir.getPath()));
        }
        final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(packageDir, schema.getName())));
        try {
            writer.write(getSchemaContent(schema));
        } finally {
            writer.close();
        }
    }

    private String getSchemaContent(Schema schema) {
        StringBuilder columnStringBuilder = new StringBuilder();

        String template = getTemplate();
        String entityName = schema.getName();
        template = template.replaceAll("@SCHEMA_NAME@", entityName);

        fillColumns(columnStringBuilder, schema);

        template = template.replaceAll("@COLUMNS@", columnStringBuilder.toString());

        return template;
    }

    private String getTemplate() {
        String template = "";
        try {
            URL url = getClass().getClassLoader().getResource("scheme_template");
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return template;
    }

    private void fillColumns(StringBuilder columnStringBuilder, Schema schema) {
        Iterator<Column> iterator = schema.getColumnList().iterator();
        while (iterator.hasNext()) {
            Column column = iterator.next();
            addColumn(columnStringBuilder, column.getColumnName(), column.getColumnType());
            columnStringBuilder.append(",\n");
        }
        columnStringBuilder.setLength(columnStringBuilder.length() - 2);
        columnStringBuilder.append(";\n");
    }

    private void addColumn(StringBuilder columnStringBuilder, String dbType, String name) {
        String nameUpperCase = splitByUpperCase(name).toUpperCase();
        columnStringBuilder
                .append("    ")
                .append(nameUpperCase)
                .append("(\"")
                .append(name).append("\", ")
                .append(dbType)
                .append(")");
    }

    public String splitByUpperCase(String string) {
        String[] stringArray = string.split("(?=\\p{Lu})");
        String newStringName = StringUtils.join(stringArray, "_");
        return newStringName;
    }
}
