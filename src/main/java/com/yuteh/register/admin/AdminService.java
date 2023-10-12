package com.yuteh.register.admin;

import com.yuteh.register.form.FormRepository;
import com.yuteh.register.form.FormService;
import com.yuteh.register.form.model.*;
import com.yuteh.register.admin.model.sql.SqlSelect;
import com.yuteh.register.admin.model.analysis.OptionCount;
import com.yuteh.register.admin.model.sql.Sql;
import com.yuteh.register.admin.model.sql.SqlPost;
import com.yuteh.register.lang.LangUtil;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Service
public class AdminService {
    private final FormService formService;
    private final FormRepository formRepository;
    private final AdminRepository adminRepository;
    private final AnalysisRepository analysisRepository;

    /**
     * Constructor
     * @param formService (Spring) formService
     * @param formRepository (Spring) formRepository
     * @param adminRepository (Spring) adminRepository
     * @param analysisRepository (Spring) analysisRepository
     */
    public AdminService(FormService formService, FormRepository formRepository, AdminRepository adminRepository, AnalysisRepository analysisRepository) {
        this.formService = formService;
        this.formRepository = formRepository;
        this.adminRepository = adminRepository;
        this.analysisRepository = analysisRepository;
    }

    /**
     * Get form data list for admin exporting to excel function
     * @param form specify form to search in
     * @param startTime start time of the search query
     * @param endTime end time of the search query
     * @param locale language for searching
     * @return list of form data
     * @throws JSONException exception for parsing json in FormUtil class static function
     */
    public List<FormData> getListFormData(Form form, Timestamp startTime, Timestamp endTime, Locale locale) throws JSONException {
        SqlSelect select = new SqlSelect();
        select.setSelect(Collections.singletonList("*"));
        select.setFrom(Collections.singletonList("form_data v"));
        select.setWhere(new ArrayList<>() {
            {
                add("v.form_id = '" + form.getId() + "'");
                add("v.update_time >= '" + startTime + "'");
                add("v.update_time <= '" + endTime + "'");
            }
        });
        select.setOrderBy(Collections.singletonList("v.create_time"));
        List<FormData> formDataList = formRepository.select(select);
        for (FormData formData : formDataList) {
            formData.setIdData(form, locale);
        }
        return formDataList;
    }

    /**
     * Get full list of json array of form data using specific querying keyword (including blank) and form id
     * @param json storing the id of the form and querying keyword
     * @param locale specifying language
     * @return return responseEntity with jsonArray
     * @throws JSONException error for parsing json input (if "formId" or "keyword" not exist)
     */
    public ResponseEntity<String> searchListFormData(JSONObject json, Locale locale) throws JSONException, FormNotFoundException {
        List<FormData> formDataList = formRepository.selectFormData(json.getString("formId"), json.getString("keyword"));
        Map<String, Form> tempFormMap = new HashMap<>();
        JSONArray jsonArray = new JSONArray();
        for (FormData formData : formDataList) {
            if (!tempFormMap.containsKey(formData.getFormId())) {
                tempFormMap.put(formData.getFormId(), formService.getForm(formData.getFormId(), locale));
            }
            formData.setData(tempFormMap.get(formData.getFormId()), locale);
            jsonArray.put(FormData.toJson(formData));
        }
        return new ResponseEntity<>(jsonArray.toString(), HttpStatus.OK);
    }

    /**
     * Admin sql querying method
     * @param sqlPost with sql string and method ("query" or "update")
     * @return execution result
     * @throws SQLException for sql string incorrect case
     */
    public Sql sql(SqlPost sqlPost) throws SQLException {
        return adminRepository.executeSql(sqlPost);
    }

    /**
     * For admin edit
     * @param json contain two attributes ("UUID" for specifying form, "mark" for specifying edit message)
     * @return return responseEntity showing success edit
     * @throws JSONException for json getString problem
     */
    public ResponseEntity<String> editFormMark(JSONObject json, Locale locale) throws JSONException {
        Map<String, Object> get = formRepository.editFormMark(json.getString("id"), json.getString("mark"));
        return ResponseEntity.ok(LangUtil.select(locale, "Success","成功"));
    }

    /**
     * For analysis purpose of each option select amounts
     * @param formId id of the form to search amounts of
     * @param locale the language to return
     * @return map of column list map (Column, List(OptionCount))
     */
    public Map<Column, List<OptionCount>> searchOptionCount(String formId, Locale locale) throws FormNotFoundException {
        Map<Column, List<OptionCount>> columnListMap = new LinkedHashMap<>();
        Form form = formService.getForm(formId, locale);
        for (Section section : form.getSectionList()) {
            for (Column column : section.getColumnList()) {
                columnListMap.put(column, analysisRepository.select(formId, column.getId(), locale.toString()));
            }
        }
        return columnListMap;
    }
}
