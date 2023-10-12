package com.yuteh.register.controller;

import com.yuteh.register.user.model.User;
import com.yuteh.register.request.RequestService;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Log4j2
public class BasicController {
    private final RequestService requestService;

    public BasicController(RequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Add user to model for all request mapping
     *
     * @param model model for template
     * @param user  current user
     */
    @ModelAttribute
    public void user(HttpServletRequest request, Model model, @AuthenticationPrincipal User user) {
        if (user == null) {
            log.trace("request - " + requestService.getClientIp(request) + " - " + request.getRequestURL());
        } else {
            log.debug(user.getEmail() + " request - "+ requestService.getClientIp(request) + " - " + request.getRequestURL());
        }
        model.addAttribute("user", user);
    }
}
