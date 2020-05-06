/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 *  org.springframework.validation.FieldError
 *  org.springframework.validation.ObjectError
 */
package cn.springcloud.gray.bean.properties.bind.validation;

import cn.springcloud.gray.bean.properties.bind.validation.OriginTrackedFieldError;
import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.springframework.util.Assert;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class ValidationErrors
implements Iterable<ObjectError> {
    private final ConfigurationPropertyName name;
    private final Set<ConfigurationProperty> boundProperties;
    private final List<ObjectError> errors;

    ValidationErrors(ConfigurationPropertyName name, Set<ConfigurationProperty> boundProperties, List<ObjectError> errors) {
        Assert.notNull((Object)name, (String)"Name must not be null");
        Assert.notNull(boundProperties, (String)"BoundProperties must not be null");
        Assert.notNull(errors, (String)"Errors must not be null");
        this.name = name;
        this.boundProperties = Collections.unmodifiableSet(boundProperties);
        this.errors = this.convertErrors(name, boundProperties, errors);
    }

    private List<ObjectError> convertErrors(ConfigurationPropertyName name, Set<ConfigurationProperty> boundProperties, List<ObjectError> errors) {
        ArrayList<ObjectError> converted = new ArrayList<ObjectError>(errors.size());
        for (ObjectError error : errors) {
            converted.add(this.convertError(name, boundProperties, error));
        }
        return Collections.unmodifiableList(converted);
    }

    private ObjectError convertError(ConfigurationPropertyName name, Set<ConfigurationProperty> boundProperties, ObjectError error) {
        if (error instanceof FieldError) {
            return this.convertFieldError(name, boundProperties, (FieldError)error);
        }
        return error;
    }

    private FieldError convertFieldError(ConfigurationPropertyName name, Set<ConfigurationProperty> boundProperties, FieldError error) {
        return OriginTrackedFieldError.of(error);
    }

    private boolean isForError(ConfigurationPropertyName name, ConfigurationPropertyName boundPropertyName, FieldError error) {
        return name.isParentOf(boundPropertyName) && boundPropertyName.getLastElement(ConfigurationPropertyName.Form.UNIFORM).equalsIgnoreCase(error.getField());
    }

    public ConfigurationPropertyName getName() {
        return this.name;
    }

    public Set<ConfigurationProperty> getBoundProperties() {
        return this.boundProperties;
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public List<ObjectError> getAllErrors() {
        return this.errors;
    }

    @Override
    public Iterator<ObjectError> iterator() {
        return this.errors.iterator();
    }
}

