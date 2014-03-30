package com.github.nrudenko.plugin.ormgenerator.model;

import com.github.nrudenko.orm.commons.Column;

import java.util.ArrayList;
import java.util.List;

public class Scheme {
    private String name;
    private String schemePackage;
    private List<Column> columnList = new ArrayList<Column>();
    private List<String> imports = new ArrayList<String>();

    public String getName() {
        return name;
    }

    public String getJavaFileName() {
        return name + ".java";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchemePackage() {
        return schemePackage;
    }

    public void setSchemePackage(String schemePackage) {
        this.schemePackage = schemePackage;
    }


    public List<Column> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<Column> columnList) {
        this.columnList = columnList;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public void addImport(String importString) {
        this.imports.add(importString);
    }
}
