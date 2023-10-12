package com.yuteh.register.security.security;

import lombok.Data;

// Cookie default prop
@Data
public class CookieProperties {
    String domain;
    String path;
    boolean httpOnly;
    boolean secure;
    int maxAgeInMinutes;
}
