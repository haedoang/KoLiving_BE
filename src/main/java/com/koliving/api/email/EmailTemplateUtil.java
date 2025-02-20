package com.koliving.api.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailTemplateUtil {

    private final TemplateEngine templateEngine;

    public String generateEmail(MailType type, Map<String, Object> args) {
        String templateName = type.getTemplateName();

        Context context = new Context();
        context.setVariables(args);

        return templateEngine.process(templateName, context);
    }
}
