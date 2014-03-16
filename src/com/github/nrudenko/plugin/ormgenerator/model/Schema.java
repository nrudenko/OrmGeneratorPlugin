package com.github.nrudenko.plugin.ormgenerator.model;

import com.github.nrudenko.orm.commons.Column;

import java.util.List;

public class Schema {
    private String name;
    private String schemaPackage;
    private String packageDirPath;
    private List<Column> columnList;

    public String getName() {
        return name;
    }

    public String getJavaFileName() {
        return name + ".java";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchemaPackage() {
        return schemaPackage;
    }

    public void setSchemaPackage(String schemaPackage) {
        this.schemaPackage = schemaPackage;
    }

    public String getPackageDirPath() {
        return packageDirPath;
    }

    public void setPackageDirPath(String packageDirPath) {
        this.packageDirPath = packageDirPath;
    }

    public List<Column> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<Column> columnList) {
        this.columnList = columnList;
    }

    public String getSchemaPath() {
        return packageDirPath + "/" + getJavaFileName();
    }
}
