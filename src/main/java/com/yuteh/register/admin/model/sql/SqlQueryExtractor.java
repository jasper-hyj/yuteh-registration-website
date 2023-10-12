package com.yuteh.register.admin.model.sql;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqlQueryExtractor implements ResultSetExtractor<Sql> {
    @Override
    public Sql extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        Sql sql = new Sql();
        ResultSetMetaData rsMetaData = resultSet.getMetaData();
        int columnCount = rsMetaData.getColumnCount();
        List<String> columnNames = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(rsMetaData.getColumnName(i));
        }
        sql.setColumnNameList(columnNames);
        List<List<String>> columns = new ArrayList<>();
        while (resultSet.next()) {
            List<String> column = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                column.add(resultSet.getString(i));
            }
            columns.add(column);
        }
        sql.setColumns(columns);
        return sql;
    }
}
