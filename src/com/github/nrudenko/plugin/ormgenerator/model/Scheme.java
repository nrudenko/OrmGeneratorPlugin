package com.github.nrudenko.plugin.ormgenerator.model;

import java.util.ArrayList;
import java.util.List;

public class Scheme {
    private String tableName;
    private String schemePackage;
    private List<SchemeColumn> columnList = new ArrayList<SchemeColumn>();

    public String getTableName() {
        return tableName;
    }

    public String getJavaFileName() {
        return getClassName() + ".java";
    }

    public String getClassName() {
        return tableName + "Scheme";
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSchemePackage() {
        return schemePackage;
    }

    public void setSchemePackage(String schemePackage) {
        this.schemePackage = schemePackage;
    }


    public List<SchemeColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<SchemeColumn> columnList) {
        this.columnList = columnList;
    }

}
