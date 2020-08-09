package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.AccessWithFrequencyDto;

import java.io.IOException;

public class AccessWithFrequencyDtoDeserializer extends StdDeserializer<AccessWithFrequencyDto> {

    public AccessWithFrequencyDtoDeserializer() {
        this(null);
    }

    public AccessWithFrequencyDtoDeserializer(Class<AccessWithFrequencyDto> t) { super(t); }

    @Override
    public AccessWithFrequencyDto deserialize(
        JsonParser jsonParser,
        DeserializationContext ctxt
    ) throws IOException {
        JsonToken jsonToken = jsonParser.currentToken();

        if (jsonToken == JsonToken.START_ARRAY) {
            jsonParser.nextValue();
            String entity = jsonParser.getValueAsString();

            jsonParser.nextValue();
            String mode = jsonParser.getValueAsString();

            jsonParser.nextValue();

            int frequency = 0;
            if (jsonParser.getCurrentValue() == JsonToken.VALUE_NUMBER_INT) {
                frequency = jsonParser.getValueAsInt();
                jsonParser.nextToken();
            }

            if (jsonParser.getCurrentToken() != (JsonToken.END_ARRAY)) {
                throw new IOException("Error deserializing Access w/ frequency");
            }

            AccessWithFrequencyDto accessWithFrequencyDto = new AccessWithFrequencyDto();
            accessWithFrequencyDto.setEntity(entity);
            accessWithFrequencyDto.setMode(mode);
            accessWithFrequencyDto.setFrequency(frequency);

            return accessWithFrequencyDto;
        }

        throw new IOException("Error deserializing Access w/ frequency");
    }
}
