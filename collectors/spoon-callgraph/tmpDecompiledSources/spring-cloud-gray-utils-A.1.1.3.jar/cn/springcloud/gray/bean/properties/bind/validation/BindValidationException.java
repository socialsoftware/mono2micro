/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 *  org.springframework.validation.ObjectError
 */
package cn.springcloud.gray.bean.properties.bind.validation;

import cn.springcloud.gray.bean.properties.bind.validation.ValidationErrors;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.util.Assert;
import org.springframework.validation.ObjectError;

public class BindValidationException
extends RuntimeException {
    private final ValidationErrors validationErrors;

    BindValidationException(ValidationErrors validationErrors) {
        super(BindValidationException.getMessage(validationErrors));
        Assert.notNull((Object)validationErrors, (String)"ValidationErrors must not be null");
        this.validationErrors = validationErrors;
    }

    public ValidationErrors getValidationErrors() {
        return this.validationErrors;
    }

    private static String getMessage(ValidationErrors errors) {
        StringBuilder message = new StringBuilder("Binding validation errors");
        if (errors != null) {
            message.append(" on " + errors.getName());
            errors.getAllErrors().forEach(error -> message.append(String.format("%n   - %s", error)));
        }
        return message.toString();
    }
}

