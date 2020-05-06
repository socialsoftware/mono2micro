/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.validation.FieldError
 */
package cn.springcloud.gray.bean.properties.bind.validation;

import org.springframework.validation.FieldError;

final class OriginTrackedFieldError
extends FieldError {
    private OriginTrackedFieldError(FieldError fieldError) {
        super(fieldError.getObjectName(), fieldError.getField(), fieldError.getRejectedValue(), fieldError.isBindingFailure(), fieldError.getCodes(), fieldError.getArguments(), fieldError.getDefaultMessage());
    }

    public String toString() {
        return super.toString();
    }

    public static FieldError of(FieldError fieldError) {
        if (fieldError == null) {
            return fieldError;
        }
        return new OriginTrackedFieldError(fieldError);
    }
}

