package com.koliving.api.validation;

import com.koliving.api.annotation.PasswordConstraint;
import com.koliving.api.exception.PasswordInvalidException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<PasswordConstraint, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String password = value.trim();

        // 대소문자, 숫자 필수
        Pattern pattern = Pattern.compile("^(?=.*[A-z])(?=.*\\d).{6,30}$");
        Matcher matcher = pattern.matcher(password);

        if (matcher.matches()) {
            return true;
        }

        throw new PasswordInvalidException("invalid_password:" + password);
    }
}
