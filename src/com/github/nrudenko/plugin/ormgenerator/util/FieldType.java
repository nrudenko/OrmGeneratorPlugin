package com.github.nrudenko.plugin.ormgenerator.util;


import java.util.Date;

public enum FieldType {
    INTEGER(DBType.INT, Integer.class, int.class),
    STRING(DBType.TEXT, String.class),
    BOOLEAN(DBType.INT, boolean.class),
    LONG(DBType.NUMERIC, long.class),
    BYTE(DBType.INT, byte.class),
    SHORT(DBType.INT, short.class),
    FLOAT(DBType.FLOAT, float.class),
    DOUBLE(DBType.FLOAT, double.class),
    BLOB(DBType.BLOB, byte[].class),
    DATE(DBType.NUMERIC, Date.class);

    private final Class[] cls;
    private final DBType dbType;

    private FieldType(DBType dbType, Class... cls) {
        this.dbType = dbType;
        this.cls = cls;
    }

    public Class[] getTypeClass() {
        return cls;
    }

    public DBType getDbType() {
        return dbType;
    }

    public String getDbTypeReference() {
        return DBType.class.getSimpleName() + "." + dbType.getName();
    }

    public static FieldType byType(Class cls) {
        if (cls != null) {
            for (FieldType b : FieldType.values()) {
                Class[] typeClass = b.getTypeClass();
                for (int i = 0; i < typeClass.length; i++) {
                    Class typeClas = typeClass[i];
                    if (cls.equals(typeClas)) {
                        return b;
                    }
                }

            }
        }
        return null;
    }

    public static FieldType byName(String clsName) {
        if (clsName != null) {
            for (FieldType b : FieldType.values()) {
                Class[] typeClass = b.getTypeClass();
                for (int i = 0; i < typeClass.length; i++) {
                    Class typeClas = typeClass[i];
                    if (clsName.equals(typeClas.getSimpleName())) {
                        return b;
                    }
                }

            }
        }
        return null;
    }
}
