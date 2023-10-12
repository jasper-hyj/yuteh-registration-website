package com.yuteh.register.mail;

import com.yuteh.register.mail.model.Mail;

import javax.mail.MessagingException;

public interface EmailService {
    void sendEmail(Mail mail) throws MessagingException;
}
