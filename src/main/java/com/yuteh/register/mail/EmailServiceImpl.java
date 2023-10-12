package com.yuteh.register.mail;

import com.yuteh.register.mail.model.Mail;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailServiceImpl(JavaMailSender emailSender, SpringTemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(@NotNull Mail mail) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        Context context = new Context();
        context.setVariables(mail.getProps());

        MimeMessageHelper helper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
        helper.setTo(mail.getMailTo());
        helper.setText(mail.getHtml(), true);
        helper.setSubject(mail.getSubject());
        helper.setFrom(mail.getFrom());

        for (Map.Entry<String, ClassPathResource> e : mail.getAttachments().entrySet()) {
            helper.addAttachment(e.getKey(), e.getValue());
        }
        emailSender.send(message);
    }
}
