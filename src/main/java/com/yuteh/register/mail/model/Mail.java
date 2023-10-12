package com.yuteh.register.mail.model;

import com.yuteh.register.lang.LangUtil;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Data
public class Mail {
    public Mail() {
    }
    public Mail(String mailTo, String subject, String html, Map<String, Object> props, Map<String, ClassPathResource> attachments) {
        this.mailTo = mailTo;
        this.subject = subject;
        this.html = html;
        this.props = props;
        this.attachments = attachments;
    }

    public Mail(Locale locale, String mailTo, String subject, String html, Map<String, Object> props) {
        this.from = LangUtil.select(locale, "Yuteh Registration Team <register_noreply@yuteh.ntpc.edu.tw>", "裕德國際學校 <register_noreply@yuteh.ntpc.edu.tw>");
        this.mailTo = mailTo;
        this.subject = subject;
        this.html = html;
        this.props = props;
    }

    private String from;
    private String mailTo;
    private String subject;
    private String html;
    private Map<String, Object> props;
    private Map<String, ClassPathResource> attachments = new HashMap<>();
}
