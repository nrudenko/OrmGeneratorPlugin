package com.github.nrudenko.plugin.ormgenerator.util;

import com.github.nrudenko.orm.commons.Column;
import com.github.nrudenko.orm.commons.DbType;
import com.github.nrudenko.plugin.ormgenerator.model.SchemeColumn;
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
import java.util.ArrayList;
import java.util.List;

public class SchemeFileGenerator {

    public static final String SCHEME_TEMPLATE = "scheme_template";
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
        template = template.replaceAll("@CLASS_NAME@", scheme.getClassName());
        template = template.replaceAll("@PACKAGE@", scheme.getSchemePackage());

//        fillColumns(columnStringBuilder, scheme);
        fillColumns2(columnStringBuilder, scheme);
        template = template.replaceAll("@COLUMNS@", columnStringBuilder.toString());

        List<String> imports = new ArrayList<String>();
        imports.add(DbType.class.getName());
        imports.add(Column.class.getName());

        fillImports(importsStringBuilder, imports);
        template = template.replaceAll("@IMPORTS@", importsStringBuilder.toString());

        return template;
    }

    private static void fillImports(StringBuilder importsStringBuilder, List<String> imports) {
        for (String anImport : imports) {
            importsStringBuilder.append("import ");
            importsStringBuilder.append(anImport);
            importsStringBuilder.append(";\n");
        }
    }

    private static String getTemplate() {
        String template = "";
        try {
            URL url = SchemeManager.class.getClassLoader().getResource(SCHEME_TEMPLATE);
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return template;
    }

//    private static void fillColumns(StringBuilder columnStringBuilder, Scheme scheme) {
//        Iterator<SchemeColumn> iterator = scheme.getColumnList().iterator();
//        while (iterator.hasNext()) {
//            Column column = iterator.next();
//            addColumn(columnStringBuilder, column.getTableName(), column.getType().name());
//            columnStringBuilder.append(",\n");
//        }
//        columnStringBuilder.setLength(columnStringBuilder.length() - 2);
//        columnStringBuilder.append(";\n");
//    }

//    private static void addColumn(StringBuilder columnStringBuilder, String name, String dbType) {
//        String nameUpperCase = splitByUpperCase(name).toUpperCase();
//        columnStringBuilder
//                .append("    ")
//                .append(nameUpperCase)
//                .append("(\"")
//                .append(name).append("\", ")
//                .append(dbType)
//                .append(")");
//    }

    private static void fillColumns2(StringBuilder columnStringBuilder, Scheme scheme) {
        List<SchemeColumn> columnList = scheme.getColumnList();
        for (SchemeColumn schemeColumn : columnList) {
            addColumn(columnStringBuilder, schemeColumn, scheme.getTableName());
        }
    }

    private static void addColumn(StringBuilder columnStringBuilder, SchemeColumn column, String tableName) {
        String nameUpperCase = splitByUpperCase(column.getName()).toUpperCase();
        columnStringBuilder
                .append("    public static final Column ")
                .append(nameUpperCase)
                .append(" = new Column(\"");
        if (!column.isVirtual()) {
            columnStringBuilder.append(tableName)
                    .append(".");
        }
        columnStringBuilder
                .append(column.getName())
                .append("\", ")
                .append(" DbType.")
                .append(column.getType().name());

        if (StringUtils.isNotEmpty(column.getCustomAdditional())) {
            columnStringBuilder.append(" \"")
                    .append(column.getCustomAdditional()).append("\"");
        }
        columnStringBuilder.append(");");
        if (StringUtils.isNotEmpty(column.getComments())) {
            columnStringBuilder.append(" // " + column.getComments());

        }
        columnStringBuilder.append("\n");

    }

    public static String splitByUpperCase(String string) {
        String[] stringArray = string.split("(?=\\p{Lu})");
        String newStringName = StringUtils.join(stringArray, "_");
        return newStringName;
    }
}

