package com.yuteh.register.form;

import com.yuteh.register.form.model.Form;
import com.yuteh.register.form.model.FormData;
import com.yuteh.register.form.model.FormDataInvalidException;
import com.yuteh.register.form.model.FormNotFoundException;
import com.yuteh.register.lang.LangUtil;
import com.yuteh.register.mail.EmailService;
import com.yuteh.register.mail.model.Mail;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.*;

@Service
@Log4j2
public class FormService {
    private final FormRepository formRepository;
    private final EmailService emailService;
    private static final Map<String, Form> formMap = new HashMap<>();

    public FormService(FormRepository formRepository, EmailService emailService) {
        this.formRepository = formRepository;
        this.emailService = emailService;
    }

    /**
     * Get list of form
     *
     * @param locale for language
     * @return return list of form from db
     */
    public List<Form> getFormList(@NotNull Locale locale) {
        return formRepository.select(locale.toString());
    }

    public List<Form> getPublicFormList(@NotNull Locale locale) {
        List<Form> formList = getFormList(locale);
        List<Form> publicFormList = new ArrayList<>();
        for (Form form : formList) {
            if (form.isPublicStatus()) {
                publicFormList.add(form);
            }
        }
        return publicFormList;
    }

    /**
     * get form data using form id
     *
     * @param formId form to select
     * @param locale for language
     * @return return form with section, column, and option
     */
    public Form getForm(String formId, @NotNull Locale locale) throws FormNotFoundException {
        if (!formMap.containsKey(formId + "-" + locale)) {
            Form form = formRepository.select(formId, locale.toString());
            if (form == null) {
                throw new FormNotFoundException("Form Id incorrect");
            }
            form.setOpen(FormValid.open(form));
            form.setSectionList(formRepository.select(form, locale.toString()));
            int sectionListSize = form.getSectionList().size();
            for (int sectionIdx = 0; sectionIdx < sectionListSize; sectionIdx++) {
                form.getSectionList()
                        .get(sectionIdx)
                        .setColumnList(
                                formRepository.select(
                                        form.getSectionList().get(sectionIdx), locale.toString()));
                int columnListSize = form.getSectionList().get(sectionIdx).getColumnList().size();
                for (int columnIdx = 0; columnIdx < columnListSize; columnIdx++) {
                    if (form.getSectionList().get(sectionIdx).getColumnList().get(columnIdx).getOption()) {
                        form.getSectionList()
                                .get(sectionIdx)
                                .getColumnList()
                                .get(columnIdx)
                                .setOptionList(formRepository.select(form.getSectionList()
                                        .get(sectionIdx)
                                        .getColumnList()
                                        .get(columnIdx), locale.toString()));
                    }
                }
            }
            formMap.put(formId + "-" + locale, form);
            log.info("form: " + formId + "-" + locale + " set");
        }
        Form form = formMap.get(formId + "-" + locale);
        form.setOpen(FormValid.open(form));
        return form;
    }

    /**
     * Add form data to db and call email service to send email to client
     *
     * @param jsonData json data from client submission
     * @param locale   for language
     */
    public void addFormData(JSONObject jsonData, Locale locale) throws FormDataInvalidException, JSONException, MessagingException, FormNotFoundException {
        FormData formData = setUpFormData(jsonData, locale);
        formRepository.insert(formData);
        emailService.sendEmail(
                new Mail(
                        locale,
                        formData.getStudentEmail(),
                        LangUtil.select(locale, "Thank You For Submitting", "已收到您的報名表單"),
                        getForm(formData.getFormId(), locale).getMail(),
                        new HashMap<>()
                ));
    }


    public void updateFormData(JSONObject jsonData, Locale locale) throws FormDataInvalidException, JSONException, MessagingException {
        FormData formData = setUpFormData(jsonData, locale);
        formRepository.insert(formData);
    }

    public FormData addBasicFormData(JSONObject jsonData, Locale locale) throws FormDataInvalidException, JSONException {
        FormData formData = new FormData(jsonData, locale);
        formRepository.insert(formData);
        return formData;
    }

    /**
     * Get form data using json info from user submission form
     *
     * @param UUID   form UUID
     * @param locale for language
     * @return return form data with formdata and id data
     */
    public FormData getFormDataByUUID(String UUID, Locale locale) throws JSONException, FormNotFoundException {
        FormData formData = formRepository.selectFormByUUID(UUID);
        if (formData == null) {
            throw new FormNotFoundException("Form Result None");
        }
        Form form = getForm(formData.getFormId(), locale);
        formData.setData(form, locale);
        return formData;
    }


    public FormData getFormDataByUserBirthdateAndId(String birthdate, String id) throws FormNotFoundException {
        FormData formData = new FormData();
        formData.setStudentBirthDate(birthdate);
        formData.setStudentId(id);
        formData = formRepository.select(formData);
        if (formData == null) {
            throw new FormNotFoundException("Form Result None");
        }
        return formData;
    }

    public void resetFormSession() {
        formMap.clear();
    }

    public static @NotNull FormData setUpFormData(@NotNull JSONObject json, Locale locale) throws JSONException, FormDataInvalidException {
        FormData formData = new FormData();
        formData.setFormId(json.getString("form_id"));
        json.remove("form_id");
        formData.setStudentId(json.getString("F1_C1-06"));
        formData.setStudentBirthDate(findStudentBirthData(json));
        formData.setStudentEmail(json.getString("F1_C2-19"));
        formData.setUUID(FormUtil.findUUID(json));
        formData.setId(json);
        if (formData.getStudentId().length() != 10) {
            throw new FormDataInvalidException(LangUtil.select(locale, "Student Id Format Incorrect", "學生身分證字號或居留證號有誤"));
        } else if (formData.getStudentBirthDate().length() != 10) {
            throw new FormDataInvalidException(LangUtil.select(locale, "Student Birthdate format Incorrect", "學生生日格式有誤"));
        }
        return formData;
    }

    public static @Nullable String findStudentBirthData(@NotNull JSONObject json) throws JSONException {
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.endsWith("C1-05")) {
                return (String) json.get(key);
            }
        }
        return null;
    }
}

