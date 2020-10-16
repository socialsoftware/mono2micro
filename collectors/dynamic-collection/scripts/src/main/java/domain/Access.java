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
    
    protected short entityID;
    protected Type type;

    @JsonCreator
    public Access(
        short entityID,
        Type type
    ) {
        this.entityID = entityID;
        this.type = type;
    }

    public short getEntityID() {
        return this.entityID;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Access) {
            Access that = (Access) other;
            return this.entityID == that.entityID && this.type == that.type;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.entityID + this.type.hashCode();
    }

    @Override
    public String toString() {
        return "["
                .concat(String.valueOf(entityID))
                .concat(",")
                .concat(type.name())
                .concat("]");
    }
}