package com.yuteh.register.form;

import com.yuteh.register.form.model.*;
import com.yuteh.register.lang.LangUtil;
import com.yuteh.register.util.JSONUtil;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Locale;

@Controller
@RequestMapping("/form")
@Log4j2
public class FormController {
    private final FormService formService;

    public FormController(FormService formService) {
        this.formService = formService;
    }


    @GetMapping("")
    public String basicFormPage(@NotNull HttpSession session, @RequestParam(name = "id") String formId, Model model, Locale locale) {
        try {
            Form form = formService.getForm(formId, locale);
            session.setAttribute("formId", formId);
            model.addAttribute((form.isOpen() ? "form" : "status"), form);
        } catch (FormNotFoundException e) {
           log.info(Arrays.toString(e.getStackTrace()));
            model.addAttribute("status", 404);
           return "error";
        }
        return "form/major-info-form";
    }

//    /**
//     * Get first page for basic info
//     *
//     * @param model  for thymeleaf
//     * @param locale for language check
//     * @return return template with form data
//     */
//    @GetMapping("/detail")
//    public String detailFormPage(@NotNull HttpSession session, Model model, Locale locale) {
//        session.removeAttribute("formUUID");
//        if (session.getAttribute("formData") != null) {
//            Form form = formService.getForm(session.getAttribute("formId").toString(), locale);
//            model.addAttribute("form", form);
//        }
//        return "form/major-info-form";
//    }

//    @PostMapping("")
//    public ResponseEntity<String> getBasicForm(@NotNull HttpSession session, @RequestBody String formData, Locale locale) {
//        try {
//            JSONObject jsonBasicForm = new JSONObject(formData);
//            if (session.getAttribute("formUUID") != null) {
//                jsonBasicForm.put("UUID", session.getAttribute("formUUID"));
//            }
//            formService.addBasicFormData(jsonBasicForm, locale);
//            return new ResponseEntity<>("Success", HttpStatus.OK);
//        } catch (JSONException jsonException) {
//            log.info(Arrays.toString(jsonException.getStackTrace()));
//            return ResponseEntity.badRequest().body(LangUtil.select(locale, "Form Data not valid", "表單資料有誤"));
//        } catch (FormDataInvalidException formDataInvalidException) {
//            log.info(Arrays.toString(formDataInvalidException.getStackTrace()));
//            return new ResponseEntity<>(formDataInvalidException.getMessage(), HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
//            log.error(Arrays.toString(e.getStackTrace()));
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    /**
     * Process post form from client
     *
     * @param formData data that parents filled in
     * @param locale   for language
     * @return status
     */
    @PostMapping("")
    public ResponseEntity<String> getForm(@NotNull HttpSession session, @RequestBody String formData, Locale locale) {
        try {
            JSONObject jsonForm = new JSONObject(formData);
            if (session.getAttribute("formUUID") != null) {
                jsonForm.put("UUID", session.getAttribute("formUUID"));
                formService.updateFormData(jsonForm, locale);
                session.removeAttribute("formUUID");
            } else {
                formService.addFormData(jsonForm, locale);
            }
            return new ResponseEntity<>(LangUtil.select(locale, "Form Submit Successfully, you can check or edit your form within the open time from this website.", "表單已成功送出，如需更改或查詢登記資料，請至網站右上方點選查詢資料"), HttpStatus.OK);
        } catch (JSONException jsonException) {
            log.trace(Arrays.toString(jsonException.getStackTrace()));
            return ResponseEntity.badRequest().body(LangUtil.select(locale, "Form Data not valid", "表單資料有誤"));
        } catch (FormDataInvalidException formDataInvalidException) {
            log.trace(Arrays.toString(formDataInvalidException.getStackTrace()));
            return new ResponseEntity<>(formDataInvalidException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    /**
     * For client searching form from db
     *
     * @return template with a form asking (birthdate and identity)
     */
    @GetMapping("/search")
    public String searchForm(@NotNull HttpSession session) {
        session.removeAttribute("formUUID");
        return "form/search-record-form";
    }

    /**
     * Get data from user posting in JSON
     *
     * @param formData formdata from client for db searching
     * @param locale   for language
     * @return search result (normal: in json, error: in string)
     */
    @PostMapping("/search/student")
    public ResponseEntity<String> searchResultForm(@NotNull HttpSession session, @RequestBody String formData, Locale locale) {
        try {
            JSONObject jsonObject = new JSONObject(formData);
            FormData studentData = formService.getFormDataByUserBirthdateAndId(jsonObject.getString("student_birth_date"), jsonObject.getString("student_id"));
            session.setAttribute("formUUID", studentData.getUUID());
            Form form = formService.getForm(studentData.getFormId(), locale);
            return ResponseEntity.ok(FormUtil.parseIdJSON(form, studentData, locale, FormJsonType.data).toString());
        } catch (JSONException jsonException) {
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (FormNotFoundException fe) {
            return new ResponseEntity<>("Form not found", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get user birthdate and id and set into session, redirect user to /form/edit in js
     *
     * @param formData form with birthdate and id
     * @param session  session for saving birthdate and id
     * @return return status
     */
    @PostMapping("/edit")
    public ResponseEntity<String> editForm(@RequestBody String formData, @NotNull HttpSession session) {
        try {
            JSONObject jsonObject = new JSONObject(formData);
            session.setAttribute("formUUID", formService.getFormDataByUserBirthdateAndId(jsonObject.getString("student_birth_date"), jsonObject.getString("student_id")).getUUID());
        } catch (JSONException | FormNotFoundException jsonException) {
            return new ResponseEntity<>("Data wrong", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("Ok");
    }

    /**
     * Use session data of birthdate and id and return form with user's form data in input version
     *
     * @param session session for getting user data
     * @param model   model for thymeleaf
     * @param locale  language display
     * @return template with form
     */
    @GetMapping("/edit")
    public String editFormTemp(HttpSession session, Model model, Locale locale) {
        try {
            FormData studentData = formService.getFormDataByUUID(session.getAttribute("formUUID").toString(), locale);
            Form form = formService.getForm(studentData.getFormId(), locale);
            if (form.isOpen()) {
                model.addAttribute("form", form);
                model.addAttribute("formData", JSONUtil.jsonToMap(studentData.getId()));
            } else {
                model.addAttribute("status", form);
            }
        } catch (JSONException | NullPointerException jsonException) {
            model.addAttribute("searchRD", "/form/search");
        } catch (FormNotFoundException notFoundException) {
            model.addAttribute("notFound", "Data not found");
        }
        return "form/edit-info-form";
    }
}
