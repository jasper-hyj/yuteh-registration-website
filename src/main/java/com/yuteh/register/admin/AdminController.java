package com.yuteh.register.admin;

import com.yuteh.register.admin.model.analysis.OptionCount;
import com.yuteh.register.admin.model.sql.Sql;
import com.yuteh.register.admin.model.sql.SqlPost;
import com.yuteh.register.file.exporter.FormDataExcelExporter;
import com.yuteh.register.form.FormService;
import com.yuteh.register.form.model.Column;
import com.yuteh.register.form.model.Form;
import com.yuteh.register.form.model.FormData;
import com.yuteh.register.form.model.FormNotFoundException;
import com.yuteh.register.user.UserService;
import com.yuteh.register.user.model.User;
import com.yuteh.register.util.JSONUtil;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Log4j2
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('admin','teacher','principle')")
public class AdminController {
    private final AdminService adminService;
    private final FormService formService;
    private final UserService userService;

    /**
     * Constructor
     *
     * @param adminService admin service
     * @param formService  form service
     * @param userService  user service
     */
    public AdminController(AdminService adminService, FormService formService, UserService userService) {
        this.adminService = adminService;
        this.formService = formService;
        this.userService = userService;
    }


    @GetMapping("")
    public String admin(@AuthenticationPrincipal User user, Model model, Locale locale) {
        model.addAttribute("template", "admin");
        model.addAttribute("publicFormList", formService.getPublicFormList(locale));
        model.addAttribute("user", user);
        return "admin/admin";
    }
    /**
     * Admin main page
     *
     * @param model  for thymeleaf
     * @param locale for language
     * @return html page
     */
    @GetMapping("/records")
    public String dataPage(@AuthenticationPrincipal User user, Model model, Locale locale) {
        model.addAttribute("template", "admin/records");
        model.addAttribute("formList", formService.getFormList(locale));
        if (Objects.equals(user.getRole(), "principle")) {
            return "admin/principle";
        }
        return "admin/records";
    }

    /**
     * Handle searching mapping
     *
     * @param user   for log record
     * @param json   for param request
     * @param locale for language
     * @return return search result in jsonobject
     * @throws JSONException parsing error
     */
    @PostMapping("/search")
    public ResponseEntity<String> search(@AuthenticationPrincipal User user, @RequestBody String json, Locale locale) throws JSONException, FormNotFoundException {
        JSONObject jsonObject = new JSONObject(json);
        log.info(user.getEmail() + " search -> " + jsonObject);
        return adminService.searchListFormData(jsonObject, locale);
    }


    /**
     * Handle admin edit form
     *
     * @param uuid    request uuid for editing form
     * @param session for saving uuid for edit
     * @param model   for generating html
     * @param locale  for language
     * @return html page for editing
     */
    @GetMapping("/edit")
    @PreAuthorize("hasAnyRole('admin','teacher')")
    public String edit(@RequestParam(name = "UUID") String uuid,
                       HttpSession session, Model model, Locale locale) {
        session.setAttribute("formUUID", uuid);
        try {
            FormData studentData = formService.getFormDataByUUID(session.getAttribute("formUUID").toString(), locale);
            Form form = formService.getForm(studentData.getFormId(), locale);
            model.addAttribute("form", form);
            model.addAttribute("formData", JSONUtil.jsonToMap(studentData.getId()));
        } catch (JSONException | NullPointerException jsonException) {
            model.addAttribute("searchRD", "/form/search");
        } catch (FormNotFoundException notFoundException) {
            model.addAttribute("notFound", "Data not found");
        }
        return "form/edit-info-form";
    }

    @PostMapping("/mark/edit")
    @PreAuthorize("hasAnyRole('admin','teacher')")
    public ResponseEntity<String> editMark(@AuthenticationPrincipal User user, @RequestBody String json, Locale locale) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        log.info(user.getEmail() + " edit mark -> " + jsonObject);
        return adminService.editFormMark(jsonObject, locale);
    }


    /**
     * For form page
     *
     * @param model  for generating html
     * @param locale for language
     * @return return corresponding page
     */
    @GetMapping("/form")
    @PreAuthorize("hasRole('admin')")
    public String formPage(Model model, Locale locale) {
        model.addAttribute("template", "admin/form");
        model.addAttribute("formList", formService.getFormList(locale));
        return "admin/form";
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/user")
    public String userPage(@AuthenticationPrincipal User user, Model model) {
        List<User> userList = userService.getUserList();
        log.info(user.getEmail() + " request user list");
        model.addAttribute("template", "admin/user");
        model.addAttribute("userList", userList);
        return "admin/user";
    }

    @PostMapping("/form/reset")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> resetFormSession(@AuthenticationPrincipal User user) {
        formService.resetFormSession();
        log.info(user.getEmail() + " reset form session");
        return ResponseEntity.ok("Success");
    }

    /**
     * Download excel action
     *
     * @param user       for log record
     * @param id         form id define
     * @param start_time time range set
     * @param end_time   time range set
     * @param locale     for language
     * @param response   for responding excel file
     * @throws JSONException  parsing error
     * @throws IOException    reading file error
     * @throws ParseException parsing error
     */
    @GetMapping("/excel/download")
    public void exportToExcel(@AuthenticationPrincipal @NotNull User user, @RequestParam(name = "id") String id,
                              @RequestParam(name = "start", required = false, defaultValue = "2000-00-00") String start_time,
                              @RequestParam(name = "end", required = false, defaultValue = "2030-00-00") String end_time,
                              Locale locale, HttpServletResponse response) throws JSONException, IOException, ParseException, FormNotFoundException {
        Form form = formService.getForm(id, locale);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        FormDataExcelExporter excelExporter = new FormDataExcelExporter(
                adminService.getListFormData(form, new Timestamp(format.parse(start_time).getTime()), new Timestamp(format.parse(end_time).getTime()), locale), form);
        response.setContentType("application/octet-stream");
        String fileName = URLEncoder.encode(form.getName(), StandardCharsets.UTF_8) + ".xlsx";
        response.setHeader("Content-Disposition", "attachement; filename=\"" + fileName + "\"");
        log.info(user.getEmail() + " download -> " + form.getName() + ".xlsx");
        excelExporter.export(response);
    }

    @GetMapping("/analysis")
    public String analysis(Model model, Locale locale) throws FormNotFoundException {
        Map<Form, Map<Column, List<OptionCount>>> formMap = new LinkedHashMap<>();
        for (Form form : formService.getFormList(locale)) {
            Map<Column, List<OptionCount>> map = adminService.searchOptionCount(form.getId(), locale);
            formMap.put(form, map);
        }
        model.addAttribute("template", "admin/analysis");
        model.addAttribute("formMap", formMap);
        return "admin/analysis";
    }

    /**
     * For admin check latest log
     *
     * @param model for generating html
     * @return html page
     */
    @GetMapping("/log")
    @PreAuthorize("hasRole('admin')")
    public String log(Model model) {
        model.addAttribute("template", "admin/log");
        try {
            FileInputStream stream = new FileInputStream("C:\\Users\\Administrator\\Desktop\\RegisterSystem\\logs\\spring-boot-logger-log4j2.log");
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            List<String> logList = new ArrayList<>();
            String strLine;
            while ((strLine = br.readLine()) != null) {
                logList.add(strLine);
            }
            stream.close();
            model.addAttribute("logList", logList);
        } catch (Exception e) {
            log.info(Arrays.toString(e.getStackTrace()));
        }
        return "admin/log";
    }


    @PreAuthorize("hasRole('admin')")
    @GetMapping("/sql")
    public String sql(Model model) {
        /* Check user can execute sql */
        model.addAttribute("template", "admin/sql");
        return "admin/sql";
    }

    /**
     * Execute sql and return result
     *
     * @param sqlPost SQL to execute (update or query)
     * @param model   for thymeleaf template
     * @return result of the execute statement
     */
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/sql/execute")
    public ResponseEntity<Sql> sqlExecute(@AuthenticationPrincipal User user, @ModelAttribute SqlPost sqlPost, Model model) {
        log.info(user.getEmail() + " execute -> " + sqlPost.getSqlString().replaceAll("\r\n", " "));
        /* Define sql for return */
        Sql sql;
        try {
            /* Get the result of executing sql */
            sql = adminService.sql(sqlPost);
        } catch (SQLException e) {
            /* Set up sql */
            sql = new Sql();
            /* Generate a arrayList for result return */
            List<String> stringList = new ArrayList<>();
            /* Add the exception into the string list */
            stringList.add(e.getMessage());
            /* Add the list to sql column */
            sql.setColumnNameList(stringList);
            /* Return error message and code to js */
            return new ResponseEntity<>(sql, HttpStatus.BAD_REQUEST);
        }

        /* Return success message and execute result to the javascript for display */
        return new ResponseEntity<>(sql, HttpStatus.OK);
    }
}
