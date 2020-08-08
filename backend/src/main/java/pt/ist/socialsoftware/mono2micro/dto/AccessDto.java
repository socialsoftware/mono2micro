package pt.ist.socialsoftware.mono2micro.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.json.JSONArray;
import org.json.JSONException;
import pt.ist.socialsoftware.mono2micro.utils.AccessDtoDeserializer;

@JsonDeserialize(using = AccessDtoDeserializer.class)
public class AccessDto {
    private String entity;
    private String mode;

    public AccessDto() {}

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "[" + "\"" + entity + "\"" + ',' + "\"" + mode + "\"" + ']';
    }

    public JSONArray toJSONrray() throws JSONException {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        arrayNode.add(entity);
        arrayNode.add(mode);
        return new JSONArray(arrayNode.toString());
    }
}
