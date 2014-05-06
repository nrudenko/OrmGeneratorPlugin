package com.github.nrudenko.plugin.ormgenerator.model;

import com.github.nrudenko.orm.commons.Column;
import com.github.nrudenko.orm.commons.DbType;

public class SchemeColumn extends Column{

    String comments;
    boolean isVirtual;
    private String additional;

    public SchemeColumn(String name) {
        super(name);
    }

    public SchemeColumn(String name, DbType type) {
        super(name, type);
    }

    public SchemeColumn(String name, DbType type, String customAdditional) {
        super(name, type, customAdditional);
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    public void setVirtual(boolean isVirtual) {
        this.isVirtual = isVirtual;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public String getAdditional() {
        return additional;
    }
}
