package com.yuteh.register.form.model;

import lombok.Data;

import java.util.List;

@Data
public class Section {
    private String id;
    private String name;
    private String content;
    private List<Column> columnList;
}
