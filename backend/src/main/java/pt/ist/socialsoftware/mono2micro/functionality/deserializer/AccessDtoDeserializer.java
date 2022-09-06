package pt.ist.socialsoftware.mono2micro.functionality.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;

import java.io.IOException;

public class AccessDtoDeserializer extends StdDeserializer<AccessDto> {

    public AccessDtoDeserializer() {
        this(null);
    }

    public AccessDtoDeserializer(Class<AccessDto> t) { super(t); }

    @Override
    public AccessDto deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonToken jsonToken = jsonParser.currentToken();
        if (jsonToken == JsonToken.START_ARRAY) {
            jsonParser.nextValue();
            String mode = jsonParser.getValueAsString();

            jsonParser.nextValue();
            short entityID = jsonParser.getShortValue();

            jsonParser.nextValue(); // consume END_ARRAY

            int occurrences = 0;
            if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
                occurrences = jsonParser.getValueAsInt();
                jsonParser.nextToken();
            }

            if (jsonParser.getCurrentToken() != (JsonToken.END_ARRAY)) {
                throw new IOException("Error deserializing Access");
            }

            AccessDto accessDto = new AccessDto();
            accessDto.setMode((byte) (mode.equals("R") ? 1 : 2));
            accessDto.setEntityID(entityID);
            accessDto.setOccurrences(occurrences);
            return accessDto;
        }
        throw new IOException("Error deserializing Access");
    }
}
