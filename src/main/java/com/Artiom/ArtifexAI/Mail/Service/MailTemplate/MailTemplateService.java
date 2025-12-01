package com.Artiom.ArtifexAI.Mail.Service.MailTemplate;

import com.Artiom.ArtifexAI.Mail.Model.MailType;

public interface MailTemplateService {
    void loadTemplate(MailType mailType, String fileName);

    String getTemplate(MailType mailType);
}
