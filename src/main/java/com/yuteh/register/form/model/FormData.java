package com.yuteh.register.form.model;

import com.yuteh.register.form.FormUtil;
import com.yuteh.register.lang.LangUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;


@Data
public class FormData {
    public FormData() {}

    public FormData(JSONObject json, Locale locale) throws JSONException, FormDataInvalidException {
        this.formId = json.getString("form_id");
        this.studentId = json.getString("student_id");
        this.studentBirthDate = json.getString("student_birth_date");
        this.studentEmail = json.getString("student_email");
        this.UUID = FormUtil.findUUID(json);
        if (this.getStudentId().length() != 10) {
            throw new FormDataInvalidException(LangUtil.select(locale, "Student Id Format Incorrect", "學生身分證字號或居留證號有誤"));
        } else if (this.getStudentBirthDate().length() != 10) {
            throw new FormDataInvalidException(LangUtil.select(locale, "Student Birthdate format Incorrect", "學生生日格式有誤"));
        } else if (this.getFormId().length() != 4) {
            throw new FormDataInvalidException("Please select the right form.");
        }
    }

    /* Primary Data for searching and sending info */
    private String studentId;
    private String studentBirthDate;
    private String studentEmail;

    /* PK of the FormData */
    private String UUID;

    /* Specifying form id for entering */
    private String formId;

    /* For admin marking students */
    private String mark;

    /* Json with only id {id: id} */
    private JSONObject id = new JSONObject();

    /* Json with id and data {id: data} */
    private JSONObject idData = new JSONObject();

    /* Json with all display name {name: data} */
    private JSONObject data = new JSONObject();

    /* Create and Update Time recorded */
    private Timestamp createTime;
    private Timestamp updateTime;

    public void setIdData(Form form, Locale locale) throws JSONException {
        this.idData = FormUtil.parseIdJSON(form, this, locale, FormJsonType.idData);
    }

    public void setData(Form form, Locale locale) throws JSONException {
        this.data = FormUtil.parseIdJSON(form, this, locale, FormJsonType.data);
    }
    /**
     * Parse the complete FormData into JSONObject format
     * @param formData FormData to parse
     * @return JSONObject of the form
     * @throws JSONException exception throw if putting data to JSONObject has error
     */
    public static @NotNull JSONObject toJson(FormData formData) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("UUID", formData.UUID);
        jsonObject.put("studentId", formData.studentId);
        jsonObject.put("studentBirthDate", formData.studentBirthDate);
        jsonObject.put("studentEmail", formData.studentEmail);
        jsonObject.put("mark", formData.mark);
        try {
            jsonObject.put("studentChineseName", formData.id.get("F1_C1-01"));
        } catch (JSONException jsonException) {
            jsonObject.put("studentChineseName", formData.id.get("F3-1_C1-01"));
        }

        jsonObject.put("formId", formData.formId);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        jsonObject.put("createTime", formatter.format(formData.createTime));
        jsonObject.put("updateTime", formatter.format(formData.updateTime));
        jsonObject.put("idJSON", formData.id);
        jsonObject.put("idDataJSON", formData.idData);
        jsonObject.put("dataJSON", formData.data);
        return jsonObject;
    }


}
