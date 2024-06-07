package pt.ist.socialsoftware.mono2micro.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.RuleDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.FieldDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimilarityStructureIterator {
    private final JSONObject codebaseAsJSON;
    private JSONObject requestedEntity;

    public SimilarityStructureIterator(
            InputStream file
    ) throws IOException, JSONException {
        codebaseAsJSON = new JSONObject(new String(IOUtils.toByteArray(file)));
        file.close();
    }

    public void getEntityWithName(String entityName) throws JSONException {
        JSONArray entitiesArray = codebaseAsJSON.getJSONArray("entities");
        for (int i = 0; i < entitiesArray.length(); i++) {
            JSONObject entityObject = entitiesArray.getJSONObject(i);
            if (entityObject.getString("name").equals(entityName)) {
                requestedEntity = entityObject;
                return;
            }
        }
    }

    public List<FieldDto> getAllFields() throws JSONException {
        List<FieldDto> fieldsDtos = new ArrayList<>();
        JSONArray fieldsArray = requestedEntity.getJSONArray("fields");
        for (int i = 0; i < fieldsArray.length(); i++) {
            JSONObject fieldJSON = fieldsArray.getJSONObject(i);
            addFields(fieldJSON, fieldsDtos);
        }
        return fieldsDtos;
    }

    /* Tratar de casos das listas de listas*/
    /*public void addFields(JSONObject fieldJSON, List<FieldDto> fieldsDtos) throws JSONException {
        if (fieldJSON.getJSONObject("type").has("parameters")) {
            String parameterName = fieldJSON.getJSONObject("type")
                    .getJSONArray("parameters")
                    .getJSONObject(0)
                    .getString("name");
            fieldsDtos.add(new FieldDto(fieldJSON.getString("name"), parameterName, true));
        } else {
            fieldsDtos.add(new FieldDto(fieldJSON.getString("name"), fieldJSON.getJSONObject("type").getString("name"), false));
        }
    }*/

    public void addFields(JSONObject fieldJSON, List<FieldDto> fieldsDtos) throws JSONException {
        String fieldName = fieldJSON.getString("name");
        JSONObject typeJSON = fieldJSON.getJSONObject("type");

        // Recursively find the innermost type name
        String parameterName = getInnermostTypeName(typeJSON);

        if (typeJSON.has("parameters")) {
            fieldsDtos.add(new FieldDto(fieldName, parameterName, true));
        } else {
            fieldsDtos.add(new FieldDto(fieldName, parameterName, false));
        }
    }

    // Recursive method to find the innermost type name
    private String getInnermostTypeName(JSONObject typeJSON) throws JSONException {
        if (typeJSON.has("parameters")) {
            JSONArray parametersArray = typeJSON.getJSONArray("parameters");
            if (parametersArray.length() > 0) {
                JSONObject firstParameter = parametersArray.getJSONObject(0);
                return getInnermostTypeName(firstParameter);
            }
        }
        return typeJSON.getString("name");
    }


}