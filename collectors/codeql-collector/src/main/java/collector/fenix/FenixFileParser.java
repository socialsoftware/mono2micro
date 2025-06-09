package collector.fenix;

import collector.results.EntityFields;
import collector.results.FileParser;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class FenixFileParser extends FileParser {

    @Override
    public List<EntityFields> readEntityFields(JsonNode rootNode) {
        List<EntityFields> entityFields = super.readEntityFields(rootNode);
        // Field name has to be changed for the Fenix framework
        entityFields.forEach(ef -> {
            // getFieldName -> fieldName
            String fieldNameWithoutGet = ef.getField().substring(3);
            String fieldName = fieldNameWithoutGet.substring(0, 1).toLowerCase() + fieldNameWithoutGet.substring(1);
            ef.setField(fieldName);
        });
        return entityFields;
    }

}
