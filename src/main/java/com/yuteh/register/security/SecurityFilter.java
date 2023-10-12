package com.yuteh.register.security;

import com.yuteh.register.user.model.User;
import com.yuteh.register.user.CustomUserService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

// filter to customized user information
@Component
@Log4j2
public class SecurityFilter extends OncePerRequestFilter {

    private final CustomUserService userService;

    /**
     * Set up user service to connect to the user repository
     *
     * @param userService user service of method do to user
     */
    public SecurityFilter(CustomUserService userService) {
        this.userService = userService;
    }

    @SneakyThrows
    @Override
    /* Set up filter for custom user setting */
    protected void doFilterInternal(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull FilterChain filterChain) {

        /* Get authentication from security context holder */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        /* Check if user first login */
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();
            user = new User();
            user.setEmail(oAuth2User.getAttribute("email"));
            user.setName(oAuth2User.getAttribute("name"));
            log.info("google oauth2 login -> " + user);
            /* Get user information from user service by email */
            user = userService.getUser(user);
            log.info("user login -> " + user);
        }
        /* Check if user login already and update user info */
        else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            /* Get principal from authentication token */
            UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;
            user = (User) authenticationToken.getPrincipal();
            /* Get user information from user service by email */
            user = userService.getUser(user);
        }

        /* Check if user login */
        if (user != null) {
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
            List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
            grantedAuthorityList.add(grantedAuthority);
            /* Build up custom authentication token */
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, grantedAuthorityList);

            /* Set web authentication detail source from request */
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));

            /* Set up custom token into the context */
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}