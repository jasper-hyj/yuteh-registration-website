package com.yuteh.register.form.model;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class Form {
    private String id;
    private String name;
    private String information;
    private String indexHeading;
    private String mail;
    private Timestamp startTime;
    private Timestamp endTime;
    private List<Section> sectionList;
    private boolean open;
    private boolean publicStatus;
}
