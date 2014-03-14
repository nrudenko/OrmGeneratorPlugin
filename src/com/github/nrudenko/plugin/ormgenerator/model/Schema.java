package com.github.nrudenko.plugin.ormgenerator.model;

import java.util.List;

public class Schema {
    private String name;
    private String schemaPackage;
    private String outputDirPath;
    private List<Column> columnList;

    public String getName() {
        return name;
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

    public String getOutputDirPath() {
        return outputDirPath;
    }

    public void setOutputDirPath(String outputDirPath) {
        this.outputDirPath = outputDirPath;
    }

    public List<Column> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<Column> columnList) {
        this.columnList = columnList;
    }

    public String getSchemaDir() {
        return outputDirPath + '/' + schemaPackage.replace('.', '/');
    }

    public String getSchemaPath() {
        return outputDirPath + '/' + schemaPackage.replace('.', '/') + "/" + name;
    }
}
