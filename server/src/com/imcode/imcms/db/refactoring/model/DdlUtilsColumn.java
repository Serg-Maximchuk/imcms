package com.imcode.imcms.db.refactoring.model;

public class DdlUtilsColumn implements Column {

    private final org.apache.ddlutils.model.Column column;

    public DdlUtilsColumn(org.apache.ddlutils.model.Column column) {
        this.column = column;
    }

    public boolean isNullable() {
        return !column.isRequired();
    }

    public boolean hasDefault() {
        return null != column.getDefaultValue();
    }

    public String getDefaultValue() {
        return column.getDefaultValue();
    }

    public boolean isAutoIncremented() {
        return column.isAutoIncrement();
    }

    public boolean isPrimaryKey() {
        return column.isPrimaryKey();
    }

    public Type getType() {
        return column.isOfNumericType() ? Type.INTEGER : Type.VARCHAR;
    }

    public int getSize() {
        return column.getSizeAsInt();
    }

    public String getName() {
        return column.getName();
    }
}