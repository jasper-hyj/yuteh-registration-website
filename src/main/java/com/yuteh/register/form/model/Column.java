package com.yuteh.register.form.model;

import lombok.Data;

import java.util.List;

@Data
public class Column {
    private String id;
    private Boolean required;
    private Boolean option;
    private Boolean other;
    private String type;
    private int size;
    private String restrict;
    private String name;
    private String placeholder;
    private List<Option> optionList;
}
