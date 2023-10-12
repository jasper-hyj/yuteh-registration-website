package com.yuteh.register.security.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

// Security prop
@Component
@ConfigurationProperties(prefix = "spring.security")
@Data
public class SecurityProperties {

    CookieProperties cookieProps;
    boolean allowCredentials;
    List<String> allowedOrigins;
    List<String> allowedHeaders;
    List<String> exposedHeaders;
    List<String> allowedMethods;
    List<String> allowedPublicApis;

}
