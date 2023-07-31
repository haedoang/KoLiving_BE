package com.koliving.api.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageSource extends AbstractMessageSource {

    private final ResourceBundleMessageSource resourceBundleMessageSource;
    private final LanguageRepository languageRepository;

    @Override
    protected MessageFormat resolveCode(String key, Locale locale) {
        String messagePattern = languageRepository
                .findByLocaleAndMessageKey(locale.toString(), key)
                .map(Language::getMessagePattern)
                .orElseGet(() -> resourceBundleMessageSource.getMessage(key, null, locale));
        return new MessageFormat(messagePattern, locale);
    }
}
