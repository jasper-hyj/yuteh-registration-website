package com.yuteh.register.form;

import com.yuteh.register.form.model.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.*;

public class FormUtil {

    /**
     * Return jsonobject with form>section>column: formdata
     * 將id json 轉換為 data json
     *
     * @param form     form to implement
     * @param formData formdata from db
     * @param locale   for language use
     * @return return jsonobject of data in word
     */
    public static @NotNull JSONObject parseIdJSON(Form form, @NotNull FormData formData, @NotNull Locale locale, FormJsonType type) throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject idJson = formData.getId();
        if (locale.toString().equals("en")) {
            json.put("Form Name", form.getName());
        } else {
            json.put("表單名稱", form.getName());
        }

        for (Section section : form.getSectionList()) {
            JSONObject jsonSection = new JSONObject();
            for (Column column : section.getColumnList()) {
                if (idJson.has(column.getId())) {
                    if (column.getOptionList() != null) {
                        JSONArray optionArr = new JSONArray();
                        Object options = idJson.get(column.getId());
                        if (options instanceof JSONArray) {
                            JSONArray optionJsonArray = (JSONArray) options;
                            List<String> optionList = new ArrayList<>();
                            for (int i = 0; i < optionJsonArray.length(); i++) {
                                optionList.add(optionJsonArray.getString(i));
                            }
                            for (Option option : column.getOptionList()) {
                                if (optionList.contains(option.getId())) {
                                    optionArr.put(option.getName());
                                }
                            }
                            if (optionList.contains(column.getId() + "_O99") && !Objects.equals(optionList.get(optionList.size() - 1), column.getId() + "_O99")) {
                                optionArr.put(optionList.get(optionList.size() - 1));
                            }
                        } else {
                            for (Option option : column.getOptionList()) {
                                if (options.equals(option.getId())) {
                                    optionArr.put(option.getName());
                                    break;
                                }
                            }
                        }
                        switch (type) {
                            case idData -> jsonSection.put(column.getId(), optionArr);
                            case data -> jsonSection.put(column.getName(), optionArr);
                        }
                    } else {
                        switch (type) {
                            case idData -> jsonSection.put(column.getId(), idJson.get(column.getId()));
                            case data -> jsonSection.put(column.getName(), idJson.get(column.getId()));
                        }
                    }
                }
            }
            switch (type) {
                case idData -> json.put(section.getId(), jsonSection);
                case data -> json.put(section.getName(), jsonSection);
            }

        }
        return json;
    }

    /**
     * Check if json has key UUID, generate if uuid doesn't exist, remove UUID key and return uuid value if exist
     * @param json json to check UUID
     * @return uuid generated or inside the json
     * @throws JSONException json parsing error
     */
    public static String findUUID(@NotNull JSONObject json) throws JSONException {
        String uuid;
        if (json.has("UUID")) {
            uuid = json.getString("UUID");
            json.remove("UUID");
        } else {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }
}
