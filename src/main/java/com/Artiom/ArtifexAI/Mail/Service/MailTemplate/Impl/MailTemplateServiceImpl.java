package com.Artiom.ArtifexAI.Mail.Service.MailTemplate.Impl;

import com.Artiom.ArtifexAI.Mail.Model.MailType;
import com.Artiom.ArtifexAI.Mail.Service.MailTemplate.MailTemplateService;
import com.Artiom.ArtifexAI.Util.FileUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class MailTemplateServiceImpl implements MailTemplateService {
    private final Map<MailType, String> templates = new HashMap<>();

    @PostConstruct
    private void init() {
        loadTemplate(MailType.RESET_PASSWORD, "reset-password.txt");
        loadTemplate(MailType.CONFIRM_EMAIL, "confirm-email.txt");
    }

    @Override
    public void loadTemplate(MailType mailType, String fileName) {
        try {
            String templatePath = System.getProperty("user.dir") + File.separator + "mail_template" + File.separator + fileName;

            templates.put(mailType, FileUtils.readFromFile(templatePath));
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getTemplate(MailType mailType) {
        return templates.get(mailType);
    }
}
