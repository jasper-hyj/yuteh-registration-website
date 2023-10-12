package com.yuteh.register.admin;

import com.yuteh.register.form.FormService;
import com.yuteh.register.form.model.Form;
import com.yuteh.register.form.model.FormNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Locale;

@Controller
@RequestMapping("/admin/test")
@PreAuthorize("hasAnyRole('admin','teacher','principle')")
public class TestController {

    private final FormService formService;

    public TestController(FormService formService) {
        this.formService = formService;
    }

    @GetMapping("")
    public String test(Model model, Locale locale) {
        model.addAttribute("formList", formService.getFormList(locale));
        model.addAttribute("template", "admin/test");
        return "admin/test";
    }

    /**
     * Test form mapping
     * @param session session check attribute
     * @param formId get test form id
     * @param model get template model
     * @param locale get language code
     * @return template of form
     */
    @GetMapping("/form")
    public String basicFormPage(@NotNull HttpSession session, @RequestParam(name = "id") String formId, Model model, Locale locale) throws FormNotFoundException {
        Form form = formService.getForm(formId, locale);
        session.setAttribute("formId", formId);
        model.addAttribute("form", form);
        return "form/major-info-form";
    }


//    @GetMapping("/form/detail")
//    public String detailFormPage(@NotNull HttpSession session, Model model, Locale locale) {
//        session.removeAttribute("formUUID");
//        if (session.getAttribute("formData") != null) {
//            Form form = formService.getForm(session.getAttribute("formId").toString(), locale);
//            model.addAttribute("form", form);
//        }
//        return "form/major-info-form";
//    }
}
