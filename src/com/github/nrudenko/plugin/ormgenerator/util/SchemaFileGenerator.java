package com.github.nrudenko.plugin.ormgenerator.util;

import com.github.nrudenko.orm.commons.Column;
import com.github.nrudenko.orm.commons.DBType;
import com.github.nrudenko.plugin.ormgenerator.model.Schema;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

public class SchemaFileGenerator {

    private static final String TAG = "SchemaFileGenerator";
    private static SchemaFileGenerator instance;

    public static VirtualFile generate(@NotNull Schema schema, @NotNull VirtualFile sourceRootDir) {
        VirtualFile result = null;
        try {
            String packagePath = schema.getSchemaPackage().replace(".", File.separator);
            File packageDir = new File(sourceRootDir.getPath() + File.separator + packagePath);
            final File file = generateSchemaClassFile(packageDir, schema);
            result = LocalFileSystem.getInstance().findFileByIoFile(file);
//            VirtualFileManager.getInstance().asyncRefresh(new Runnable() {
//                @Override
//                public void run() {
//                    result = ;
//                }
//            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static File generateSchemaClassFile(File packageDir, Schema schema) throws IOException {
        if (!packageDir.exists() && !packageDir.mkdirs()) {
            throw new IOException("Cannot create directory " + FileUtil.toSystemDependentName(packageDir.getPath()));
        }
        File file = new File(packageDir, schema.getJavaFileName());
        final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        try {
            writer.write(getSchemaContent(schema));
        } finally {
            writer.close();
        }
        return file;
    }

    private static String getSchemaContent(Schema schema) {
        StringBuilder columnStringBuilder = new StringBuilder();

        String template = getTemplate();
        String entityName = schema.getName();
        template = template.replaceAll("@SCHEMA_NAME@", entityName);
        template = template.replaceAll("@PACKAGE@", schema.getSchemaPackage());
        template = template.replaceAll("@DB_TYPE_CLASS@", DBType.class.getName());

        fillColumns(columnStringBuilder, schema);

        template = template.replaceAll("@COLUMNS@", columnStringBuilder.toString());

        return template;
    }

    private static String getTemplate() {
        String template = "";
        try {
            URL url = SchemaManager.class.getClassLoader().getResource("scheme_template");
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return template;
    }

    private static void fillColumns(StringBuilder columnStringBuilder, Schema schema) {
        Iterator<Column> iterator = schema.getColumnList().iterator();
        while (iterator.hasNext()) {
            Column column = iterator.next();
            addColumn(columnStringBuilder, column.getName(), column.getType());
            columnStringBuilder.append(",\n");
        }
        columnStringBuilder.setLength(columnStringBuilder.length() - 2);
        columnStringBuilder.append(";\n");
    }

    private static void addColumn(StringBuilder columnStringBuilder, String dbType, String name) {
        String nameUpperCase = splitByUpperCase(name).toUpperCase();
        columnStringBuilder
                .append("    ")
                .append(nameUpperCase)
                .append("(\"")
                .append(name).append("\", ")
                .append(dbType)
                .append(")");
    }

    public static String splitByUpperCase(String string) {
        String[] stringArray = string.split("(?=\\p{Lu})");
        String newStringName = StringUtils.join(stringArray, "_");
        return newStringName;
    }
}
