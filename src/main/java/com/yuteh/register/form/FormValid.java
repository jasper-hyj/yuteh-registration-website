package com.yuteh.register.form;

import com.yuteh.register.form.model.Form;

import java.sql.Timestamp;

public class FormValid {
    public static boolean open(Form form) {
        return form.getStartTime().compareTo(new Timestamp(System.currentTimeMillis())) < 0 &&
                form.getEndTime().compareTo(new Timestamp(System.currentTimeMillis())) > 0;
    }
}
