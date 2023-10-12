package com.yuteh.register.admin.model.sql;

import lombok.Data;

import java.util.List;

@Data
public class Sql {
    private List<String> columnNameList;
    private List<List<String>> columns;
}
