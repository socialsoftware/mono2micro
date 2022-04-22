package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.FunctionalityDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FunctionalityDtoDeserializer extends StdDeserializer<FunctionalityDto> {

    public FunctionalityDtoDeserializer() {
        this(null);
    }

    public FunctionalityDtoDeserializer(Class<FunctionalityDtoDeserializer> t) { super(t); }

    @Override
    public FunctionalityDto deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        FunctionalityDto functionalityDto = new FunctionalityDto();
        JsonToken jsonToken = jsonParser.currentToken();

        if (jsonToken == JsonToken.START_ARRAY) {
            List<AccessDto> functionalityAccesses = new ArrayList<>();

            while (jsonParser.nextValue() != JsonToken.END_ARRAY)
                functionalityAccesses.add(jsonParser.readValueAs(AccessDto.class));

            functionalityDto.setFunctionalityAccesses(functionalityAccesses);

            return functionalityDto;
        }
        throw new IOException("Error deserializing Functionality");
    }
}
