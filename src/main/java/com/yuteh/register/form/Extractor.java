package com.yuteh.register.form;

import com.yuteh.register.admin.model.analysis.OptionCount;
import com.yuteh.register.form.model.*;
import com.yuteh.register.user.model.User;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.dao.DataAccessException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Extractor {

    public static User user(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setEmail(resultSet.getString("email"));
        user.setName(resultSet.getString("name"));
        user.setValidity(resultSet.getBoolean("validity"));
        user.setRole(resultSet.getString("role"));
        user.setCreateTime(resultSet.getTimestamp("create_time"));
        user.setCreateUser(resultSet.getString("create_user"));
        user.setUpdateTime(resultSet.getTimestamp("update_time"));
        user.setUpdateUser(resultSet.getString("update_user"));
        return user;
    }

    public static Column column(ResultSet resultSet) throws SQLException, DataAccessException {
        Column column = new Column();
        column.setId(resultSet.getString("column_id"));
        column.setRequired(resultSet.getBoolean("column_required"));
        column.setOption(resultSet.getBoolean("column_option"));
        column.setOther(resultSet.getBoolean("column_other"));
        column.setType(resultSet.getString("column_type"));
        column.setSize(resultSet.getInt("column_size"));
        column.setRestrict(resultSet.getString("column_restrict"));
        column.setName(resultSet.getString("column_name"));
        column.setPlaceholder(resultSet.getString("column_placeholder"));
        return column;
    }

    public static FormData formData(ResultSet resultSet) throws SQLException, JSONException {
        FormData formData = new FormData();
        formData.setUUID(resultSet.getString("UUID"));
        formData.setStudentBirthDate(resultSet.getString("student_birth_date"));
        formData.setFormId(resultSet.getString("form_id"));
        formData.setStudentEmail(resultSet.getString("student_email"));
        formData.setMark(resultSet.getString("mark"));
        formData.setId(new JSONObject(resultSet.getString("data")));
        formData.setStudentId(resultSet.getString("student_id"));
        formData.setCreateTime(resultSet.getTimestamp("create_time"));
        formData.setUpdateTime(resultSet.getTimestamp("update_time"));
        return formData;
    }

    public static Form form(ResultSet resultSet) throws SQLException {
        Form form = new Form();
        form.setId(resultSet.getString("form_id"));
        form.setName(resultSet.getString("form_name"));
        form.setInformation(resultSet.getString("form_information"));
        form.setIndexHeading(resultSet.getString("form_index_heading"));
        form.setStartTime(resultSet.getTimestamp("form_start_time"));
        form.setEndTime(resultSet.getTimestamp("form_end_time"));
        form.setMail(resultSet.getString("form_mail"));
        form.setPublicStatus(resultSet.getBoolean("form_public_status"));
        return form;
    }

    public static Option option(ResultSet resultSet) throws SQLException {
        Option option = new Option();
        option.setId(resultSet.getString("option_id"));
        option.setName(resultSet.getString("option_name"));
        return option;
    }

    public static Section section(ResultSet resultSet) throws SQLException {
        Section section = new Section();
        section.setId(resultSet.getString("section_id"));
        section.setName(resultSet.getString("section_name"));
        section.setContent(resultSet.getString("section_content"));
        return section;
    }

    public static OptionCount optionCount(ResultSet resultSet) throws SQLException {
        OptionCount optionCount = new OptionCount();
        optionCount.setId(resultSet.getString("option_count_id"));
        optionCount.setName(resultSet.getString("option_count_name"));
        optionCount.setCount(resultSet.getInt("option_count_count"));
        return optionCount;
    }
}
