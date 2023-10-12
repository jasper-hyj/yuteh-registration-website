package com.yuteh.register.admin.model.sql;

import org.springframework.web.bind.annotation.ModelAttribute;


public class SqlPost {
    private String sqlString;
    private SqlType type;

    @Override
    public String toString() {
        return "SqlPost{" +
                "sqlString='" + sqlString + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @ModelAttribute("sqlString")
    public String getSqlString() {
        return sqlString;
    }

    @ModelAttribute("sqlString")
    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

    @ModelAttribute("type")
    public SqlType getType() {
        return type;
    }

    @ModelAttribute("type")
    public void setType(String type) {
        this.type = SqlType.valueOf(type);
    }
}
