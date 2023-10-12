package com.yuteh.register.controller;

import com.yuteh.register.request.RequestService;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/error")
@Log4j2
public class ErrorHandleController implements ErrorController {

    private final RequestService requestService;

    /**
     * Constructor
     * @param requestService request service
     */
    public ErrorHandleController(RequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Error handler
     * @param request request handler
     * @param model for template model
     * @return error template
     */
    @GetMapping()
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = 0;
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
            model.addAttribute("status", statusCode);
        }
        log.info("error - " + requestService.getClientIp(request) + " - " + request.getRequestURL() + " - " + statusCode);
        return "error";
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}
