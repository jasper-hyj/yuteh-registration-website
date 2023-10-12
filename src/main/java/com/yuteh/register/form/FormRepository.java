package com.yuteh.register.form;

import com.yuteh.register.admin.model.sql.SqlSelect;
import com.yuteh.register.form.model.*;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository("FormRepository")
public class FormRepository {
    private final JdbcTemplate jdbcTemplate;

    public FormRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Select form using language code
     *
     * @param locale for language
     * @return list of form class
     */
    public List<Form> select(String locale) {
        return jdbcTemplate.query(
                """
                        select * from v_form v where v.lang = ?;
                        """, new Object[]{locale}, new int[]{Types.VARCHAR},
                rs -> {
                    List<Form> formList = new ArrayList<>();
                    while (rs.next()) {
                        formList.add(Extractor.form(rs));
                    }
                    return formList;
                }
        );
    }

    /**
     * Select specific form from db using form id
     *
     * @param formId form id for where checking
     * @param locale for language
     * @return specific form
     */
    public Form select(String formId, String locale) {
        return jdbcTemplate.query(
                """
                        select * from v_form v where v.lang = ? and v.form_id = ?;
                        """, new Object[]{locale, formId}, new int[]{Types.VARCHAR, Types.VARCHAR},
                rs -> {
                    if (!rs.next()) return null;
                    return Extractor.form(rs);
                }
        );
    }

    /**
     * Select list of section from database
     *
     * @param form   form to select section
     * @param locale for language
     * @return list of sections
     */
    public List<Section> select(Form form, String locale) {
        return jdbcTemplate.query(
                """
                        select * from v_section v where v.section_id like concat(? ,"%") and lang = ?;
                        """, new Object[]{form.getId(), locale}, new int[]{Types.VARCHAR, Types.VARCHAR},
                rs -> {
                    List<Section> sectionList = new ArrayList<>();
                    while (rs.next()) {
                        sectionList.add(Extractor.section(rs));
                    }
                    return sectionList;
                });
    }

    /**
     * Select list of column from database
     *
     * @param section section to select column
     * @param locale  for language
     * @return list of columns
     */
    public List<Column> select(Section section, String locale) {
        return jdbcTemplate.query(
                """
                        select * from v_column v where v.section_id = ? and lang = ?;
                        """, new Object[]{section.getId(), locale}, new int[]{Types.VARCHAR, Types.VARCHAR},
                rs -> {
                    List<Column> columnList = new ArrayList<>();
                    while (rs.next()) {
                        columnList.add(Extractor.column(rs));
                    }
                    return columnList;
                });
    }

    /**
     * Select list of option if column has options inside
     *
     * @param column column to select option
     * @param locale for language
     * @return list of option(s)
     */
    public List<Option> select(Column column, String locale) {
        return jdbcTemplate.query(
                """
                        select * from v_option v where v.option_id like concat(?, "%") and lang = ?;
                        """, new Object[]{column.getId(), locale}, new int[]{Types.VARCHAR, Types.VARCHAR},
                rs -> {
                    List<Option> optionList = new ArrayList<>();
                    while (rs.next()) {
                        optionList.add(Extractor.option(rs));
                    }
                    return optionList;
                });
    }

    /**
     * Select specific form data from db
     *
     * @param formData formData with student id and student birthdate
     * @return form data
     */
    public FormData select(FormData formData) {
        return jdbcTemplate.query(
                """
                        select * from form_data fd
                        where fd.student_id = ?
                        and fd.student_birth_date = ?
                        """, new Object[]{formData.getStudentId(), formData.getStudentBirthDate()}, new int[]{Types.VARCHAR, Types.VARCHAR},
                rs -> {
                    if (!rs.next()) return null;
                    try {
                        return Extractor.formData(rs);
                    } catch (JSONException e) {
                        return null;
                    }
                }
        );
    }

    /**
     * Get form by UUID
     *
     * @param formUUID form UUID
     * @return form selected from db using UUID as Key
     */
    public FormData selectFormByUUID(String formUUID) {
        return jdbcTemplate.query(
                """
                        select * from form_data fd
                        where fd.UUID = ?
                        """, new Object[]{formUUID}, new int[]{Types.VARCHAR},
                rs -> {
                    if (!rs.next()) return null;
                    try {
                        return Extractor.formData(rs);
                    } catch (JSONException e) {
                        return null;
                    }
                }
        );
    }

    /**
     * Insert form data using procedure into db
     *
     * @param formData formdata that client post
     * @return status map
     */
    public Map<String, Object> insert(FormData formData) {
        List<SqlParameter> parameters = Arrays.asList(
                new SqlParameter(Types.VARCHAR),
                new SqlParameter(Types.VARCHAR),
                new SqlParameter(Types.VARCHAR),
                new SqlParameter(Types.VARCHAR));
        return jdbcTemplate.call(connection -> {
            CallableStatement cs = connection.prepareCall("{call sp_add_or_update_form(?,?,?,?,?,?)}");
            cs.setString(1, formData.getUUID());
            cs.setString(2, formData.getFormId());
            cs.setString(3, formData.getStudentId());
            cs.setString(4, formData.getStudentBirthDate());
            cs.setString(5, formData.getStudentEmail());
            cs.setString(6, String.valueOf(formData.getId()));
            return cs;
        }, parameters);
    }

    /**
     * Select list of form data according to detail query inside sql select
     *
     * @param sqlSelect select option and attributes
     * @return list of form data
     */
    public List<FormData> select(SqlSelect sqlSelect) {
        return jdbcTemplate.query(sqlSelect.sql(),
                rs -> {
                    List<FormData> formDataList = new ArrayList<>();
                    try {
                        while (rs.next()) {
                            formDataList.add(Extractor.formData(rs));
                        }
                    } catch (JSONException e) {
                        return null;
                    }
                    return formDataList;
                });
    }

    /**
     * Select list of form data using form id and keyword (matches any part of the data)
     *
     * @param formId  form id
     * @param keyWord query keyword
     * @return list of result form data query from the db
     */
    public List<FormData> selectFormData(String formId, String keyWord) {
        return jdbcTemplate.query(
                """
                        select *
                        from form_data fd
                        where fd.`data` like concat("%", ?, "%")
                        and fd.`form_id` = ?
                        order by fd.`create_time`""",
                new Object[]{keyWord, formId}, new int[]{Types.VARCHAR, Types.VARCHAR},
                rs -> {
                    List<FormData> formDataList = new ArrayList<>();
                    try {
                        while (rs.next()) {
                            formDataList.add(Extractor.formData(rs));
                        }
                    } catch (JSONException e) {
                        return null;
                    }
                    return formDataList;
                });
    }

    /**
     * update form mark in the form data table using procedure
     *
     * @param UUID form to update
     * @param mark mark to update in
     * @return status map
     */
    public Map<String, Object> editFormMark(String UUID, String mark) {
        List<SqlParameter> parameters = Arrays.asList(
                new SqlParameter(Types.CHAR),
                new SqlParameter(Types.VARCHAR));
        return jdbcTemplate.call(connection -> {
            CallableStatement cs = connection.prepareCall("{call sp_set_form_mark(?,?)}");
            cs.setString(1, UUID);
            cs.setString(2, mark);
            return cs;
        }, parameters);
    }
}