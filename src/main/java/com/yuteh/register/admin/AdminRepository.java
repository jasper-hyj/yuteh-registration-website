package com.yuteh.register.admin;

import com.yuteh.register.admin.model.sql.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Sql executeSql(SqlPost sqlPost) {
        Sql sql = new Sql();
        switch (sqlPost.getType()) {
            case query -> {
                sql = jdbcTemplate.query(sqlPost.getSqlString(), new SqlQueryExtractor());
            }
            case update -> {
                int i = jdbcTemplate.update(sqlPost.getSqlString());
                List<String> list = new ArrayList<>();
                list.add(i + " rows effect");
                sql.setColumnNameList(list);
            }
            default -> throw new IllegalStateException("Unexpected value: " + sqlPost.getType());
        }
        return sql;
    }
}
