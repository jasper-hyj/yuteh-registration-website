package com.yuteh.register.controller;

import com.yuteh.register.form.FormService;
import com.yuteh.register.form.model.Form;
import com.yuteh.register.lang.LangUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/")
public class DefaultController {

    private final FormService formService;

    /**
     * Constructor
     * @param formService form service
     */
    public DefaultController(FormService formService) {
        this.formService = formService;
    }

    /**
     * Main page (index page)
     * @return index
     */
    @GetMapping()
    public String main(HttpSession session, Model model, Locale locale) {
        List<Form> formList = formService.getPublicFormList(locale);
        if (formList.isEmpty()) {
            model.addAttribute("status", LangUtil.select(locale, "No Registration Available.", "目前暫無可報名表單"));
        } else {
            model.addAttribute("formList", formList);
        }
        session.removeAttribute("formUUID");
        session.removeAttribute("formId");
        return "index";
    }
}
