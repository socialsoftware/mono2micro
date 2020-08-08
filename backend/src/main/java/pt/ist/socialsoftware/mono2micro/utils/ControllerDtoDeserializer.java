package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.ControllerDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ControllerDtoDeserializer extends StdDeserializer<ControllerDto> {

    public ControllerDtoDeserializer() {
        this(null);
    }

    public ControllerDtoDeserializer(Class<ControllerDtoDeserializer> t) { super(t); }

    @Override
    public ControllerDto deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        ControllerDto controllerDto = new ControllerDto();
        JsonToken jsonToken = jsonParser.currentToken();
        if (jsonToken == JsonToken.START_ARRAY) {
            List<AccessDto> controllerAccesses = new ArrayList<>();
            while (jsonParser.nextValue() != JsonToken.END_ARRAY) {
                controllerAccesses.add(jsonParser.readValueAs(AccessDto.class));
            }
            controllerDto.setControllerAccesses(controllerAccesses);
            return controllerDto;
        }
        throw new IOException("Error deserializing Controller");
    }
}
