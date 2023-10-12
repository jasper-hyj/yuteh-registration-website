package com.yuteh.register.security.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// OAuth2 prop
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider.google")
@Data
public class OAuth2ProviderProperties {
    String authorizationUri;
    String tokenUri;
    String userInfoUri;
    String jwkSetUri;
    String userNameAttribute;
}
