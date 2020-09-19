package domain;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import deserializers.AccessDeserializer;
import requitur.content.Content;
import requitur.content.TraceElementContent;
import serializers.AccessSerializer;

import java.util.Arrays;

@JsonSerialize(using = AccessSerializer.class)
@JsonDeserialize(using = AccessDeserializer.class)
public class Access extends Content implements Cloneable  {
    public enum Type {
        R, // Read
        W, // Write
    };
    
    protected String entity;
    protected Type type;

    @JsonCreator
    public Access(
        String entity,
        Type type
    ) {
        this.entity = entity;
        this.type = type;
    }

    public String getEntity() {
        return this.entity;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Access) {
            Access that = (Access) other;
            return this.entity.equals(that.entity) && this.type == that.type;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.entity.hashCode() + this.type.hashCode();
    }

    @Override
    public String toString() {
        return "["
                .concat(entity)
                .concat(",")
                .concat(type.name())
                .concat("]");
    }
}