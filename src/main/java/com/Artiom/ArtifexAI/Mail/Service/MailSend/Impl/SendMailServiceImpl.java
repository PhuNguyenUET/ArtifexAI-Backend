package com.Artiom.ArtifexAI.Mail.Service.MailSend.Impl;

import com.Artiom.ArtifexAI.Mail.Model.SendMailTask;
import com.Artiom.ArtifexAI.Mail.Service.MailSend.SendMailService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SendMailServiceImpl implements SendMailService {
    @Value("${smtp.auth}")
    private boolean smtpAuth;

    @Value("${smtp.startTls}")
    private boolean startTls;

    @Value("${smtp.host}")
    private String host;

    @Value("${smtp.port}")
    private int port;

    @Value("${smtp.senderEmail}")
    private String senderEmail;

    @Value("${smtp.senderPassword}")
    private String senderPassword;

    @Value("${smtp.senderName}")
    private String senderName;

    private Properties mailSendProperties;
    private final ScheduledExecutorService mailSendersPool = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentLinkedQueue<SendMailTask> queue = new ConcurrentLinkedQueue<>();

    @PostConstruct
    private void initSendMail() {
        mailSendProperties = new Properties();
        mailSendProperties.put("mail.smtp.auth", smtpAuth);
        mailSendProperties.put("mail.smtp.starttls.enable", startTls);
        mailSendProperties.put("mail.smtp.host", host);
        mailSendProperties.put("mail.smtp.port", port);

        mailSendersPool.scheduleWithFixedDelay(this::sendMail, 1, 1, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void sendMail() {
        try {
            Session session = Session.getInstance(mailSendProperties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            while (!queue.isEmpty()) {
                SendMailTask task = queue.poll();
                MimeMessage mimeMessage = new MimeMessage(session);
                mimeMessage.setFrom(new InternetAddress(senderEmail, senderName));
                mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(task.getTo()));
                mimeMessage.setSubject(task.getSubject());
                mimeMessage.setText(task.getBody(), "utf-8");
                Transport.send(mimeMessage);
            }
        } catch (Exception e) {
            log.error("Error sending email", e);
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        mailSendersPool.shutdown();
        try {
            if (!mailSendersPool.awaitTermination(60, TimeUnit.SECONDS)) {
                mailSendersPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            mailSendersPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void addToQueue(String to, String subject, String body) {
        queue.add(new SendMailTask(to, subject, body));
    }
}
