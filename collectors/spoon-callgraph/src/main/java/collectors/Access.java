package collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"entity", "type" })
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
    public String toString() {
        return "["
                .concat(entity)
                .concat(",")
                .concat(type.name())
                .concat("]");
    }
}