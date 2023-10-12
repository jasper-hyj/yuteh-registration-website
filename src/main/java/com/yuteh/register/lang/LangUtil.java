package com.yuteh.register.lang;

import java.util.Locale;
import java.util.Objects;

public class LangUtil {
    public static String select(Locale locale, String en, String zh_TW) {
        return (Objects.equals(locale.toString(), "en")) ? en : zh_TW;
    }
}
