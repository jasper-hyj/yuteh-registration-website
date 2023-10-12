package com.yuteh.register.security.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// OAuth2 registration prop
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.google")
@Data
public class OAuth2RegistrationProperties {
    String registrationId;
    String clientId;
    String clientSecret;
    String clientAuthenticationMethod;
    String redirectUriTemplate;
    String scope;
    String clientName;
}
