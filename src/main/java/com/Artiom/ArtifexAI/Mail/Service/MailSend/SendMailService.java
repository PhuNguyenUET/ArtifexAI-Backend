package com.Artiom.ArtifexAI.Mail.Service.MailSend;

public interface SendMailService {
    void addToQueue(String to, String subject, String body);
}
