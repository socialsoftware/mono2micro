package domain;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import deserializers.AccessDeserializer;
import serializers.AccessSerializer;

@JsonSerialize(using = AccessSerializer.class)
@JsonDeserialize(using = AccessDeserializer.class)
public class Access implements Cloneable {
    public enum Type {
        R, // Read
        W, // Write
    };
    
    protected String entity;
    protected Type type;

    @JsonCreator
    public Access(
        @JsonProperty("entity") String entity,
        @JsonProperty("type") Type type
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
//        Utils.print("Access X: " + this, Utils.lineno());
//        Utils.print("Access Y: " + other, Utils.lineno());
        if (this == other) {
//            Utils.print("this Access == other Access", Utils.lineno());
            return true;
        }

        if (other == null || !getClass().isAssignableFrom(other.getClass())) {
//            Utils.print("Different classes", Utils.lineno());
            return false;
        }

        Access that = (Access) other;

        boolean isEqual = this.entity.equals(that.entity) && this.type == that.type;
//        Utils.print("EQUAL accesses", Utils.lineno());
        return isEqual;
    }

    @Override
    public String toString() {
        return "<Access entity="
                .concat(entity)
                .concat(" type=")
                .concat(type.name())
                .concat(">");
    }
}