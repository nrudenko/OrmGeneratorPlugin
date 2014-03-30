package com.github.nrudenko.plugin.ormgenerator.util;

import com.github.nrudenko.orm.commons.Column;
import com.github.nrudenko.plugin.ormgenerator.model.Scheme;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

public class SchemeFileGenerator {

    private static final String TAG = "SchemeFileGenerator";
    private static SchemeFileGenerator instance;

    public static VirtualFile generate(@NotNull Scheme scheme, @NotNull VirtualFile sourceRootDir) {
        VirtualFile result = null;
        try {
            String packagePath = scheme.getSchemePackage().replace(".", File.separator);
            File packageDir = new File(sourceRootDir.getPath() + File.separator + packagePath);
            final File file = generateSchemeClassFile(packageDir, scheme);
            result = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static File generateSchemeClassFile(File packageDir, Scheme scheme) throws IOException {
        if (!packageDir.exists() && !packageDir.mkdirs()) {
            throw new IOException("Cannot create directory " + FileUtil.toSystemDependentName(packageDir.getPath()));
        }
        File file = new File(packageDir, scheme.getJavaFileName());
        final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        try {
            writer.write(getSchemeContent(scheme));
        } finally {
            writer.close();
        }
        return file;
    }

    private static String getSchemeContent(Scheme scheme) {
        StringBuilder columnStringBuilder = new StringBuilder();
        StringBuilder importsStringBuilder = new StringBuilder();

        String template = getTemplate();
        String entityName = scheme.getName();
        template = template.replaceAll("@SCHEME_NAME@", entityName);
        template = template.replaceAll("@PACKAGE@", scheme.getSchemePackage());

        fillImports(importsStringBuilder, scheme);
        template = template.replaceAll("@IMPORTS@", importsStringBuilder.toString());
        fillColumns(columnStringBuilder, scheme);

        template = template.replaceAll("@COLUMNS@", columnStringBuilder.toString());

        return template;
    }

    private static void fillImports(StringBuilder importsStringBuilder, Scheme scheme) {
        Iterator<String> iterator = scheme.getImports().iterator();
        while (iterator.hasNext()) {
            String importString = iterator.next();
            importsStringBuilder.append("import ");
            importsStringBuilder.append(importString);
            importsStringBuilder.append(";\n");
        }
    }

    private static String getTemplate() {
        String template = "";
        try {
            URL url = SchemeManager.class.getClassLoader().getResource("scheme_template");
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return template;
    }

    private static void fillColumns(StringBuilder columnStringBuilder, Scheme scheme) {
        Iterator<Column> iterator = scheme.getColumnList().iterator();
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

