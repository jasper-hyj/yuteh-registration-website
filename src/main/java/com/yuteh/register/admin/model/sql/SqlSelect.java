package com.yuteh.register.admin.model.sql;

import lombok.Data;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Data
public class SqlSelect {
    private List<String> select = new ArrayList<>();
    private List<String> from = new ArrayList<>();
    private List<String> orderBy;
    private List<String> where;
    private List<Object> parameters = new ArrayList<>();
    private List<Types> types = new ArrayList<>();
    private int limit = 0;

    public String sql() {
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        sql.append(String.join(",", select));
        sql.append(" from ");
        sql.append(String.join(",", from));
        if (where != null) {
            sql.append(" where ");
            sql.append(String.join(" and ", where));
        }
        if (limit != 0) {
            sql.append(" limit ").append(limit);
        }
        if (orderBy != null) {
            sql.append(" order by ");
            sql.append(String.join(", ", orderBy));
        }
        sql.append(";");
        return sql.toString();
    }
}
