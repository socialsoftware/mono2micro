package util;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.serializer.ContextReferenceSerializer;

@JsonSerialize(using = ContextReferenceSerializer.class)
public class ContextReference extends Access {
    private Integer contextIndex;
    private String contextType;

    public ContextReference(Integer contextIndex, String contextType) {
        super("", contextIndex); // FIXME: Access entityId is used temporarily as contextIndex representation in Json
        this.contextIndex = contextIndex;
        this.contextType = contextType;
    }

    public Integer getContextIndex() {
        return contextIndex;
    }

    public void setContextIndex(Integer contextIndex) {
        this.contextIndex = contextIndex;
    }

    public String getContextType() {
        return contextType;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }
}
