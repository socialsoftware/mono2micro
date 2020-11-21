package util;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.serializer.AccessSerializer;

@JsonSerialize(using = AccessSerializer.class)
public class Access {
    private String mode;
    private Integer entityID;

    public Access(String mode, Integer entityID) {
        this.mode = mode;
        this.entityID = entityID;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getEntityID() {
        return entityID;
    }

    public void setEntityID(Integer entityID) {
        this.entityID = entityID;
    }
}
