package com.zxy.work.service;

import com.zxy.work.entities.MyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

@Component
@Slf4j
public class EmailService {
    @Resource
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendSimpleMessage(String to, String subject, String text) throws MyException {
        try {
            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            emailSender.send(message);
        } catch (Exception e) {
            log.error("邮件验证码发送错误msg={}", e.getMessage());
            throw new MyException("邮件验证码发送错误");
        }
    }
}