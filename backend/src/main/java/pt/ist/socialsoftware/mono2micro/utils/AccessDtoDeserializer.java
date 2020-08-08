package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;

import java.io.IOException;

public class AccessDtoDeserializer extends StdDeserializer<AccessDto> {

    public AccessDtoDeserializer() {
        this(null);
    }

    public AccessDtoDeserializer(Class<ControllerDtoDeserializer> t) { super(t); }

    @Override
    public AccessDto deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonToken jsonToken = jsonParser.currentToken();
        if (jsonToken == JsonToken.START_ARRAY) {
            jsonParser.nextValue();
            String entity = jsonParser.getValueAsString();
            jsonParser.nextValue();
            String mode = jsonParser.getValueAsString();

            jsonParser.nextValue(); // consume END_ARRAY

            AccessDto accessDto = new AccessDto();
            accessDto.setEntity(entity);
            accessDto.setMode(mode);
            return accessDto;
        }
        throw new IOException("Error deserializing Access");
    }
}
