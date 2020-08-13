package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.SequenceDto;

import java.io.IOException;
import java.util.List;

public class SequenceDtoDeserializer extends StdDeserializer<SequenceDto> {
	public SequenceDtoDeserializer() { this(null); }
	public SequenceDtoDeserializer(Class<SequenceDto> t) { super(t); }

	@Override
	public SequenceDto deserialize(
		JsonParser jsonParser,
		DeserializationContext deserializationContext
	) throws IOException {

		JsonToken jsonToken = jsonParser.currentToken();

		if (jsonToken == JsonToken.START_ARRAY) {
			jsonParser.nextValue();
			List<AccessDto> accesses = jsonParser.readValueAs(new TypeReference<List<AccessDto>>(){});

			jsonParser.nextValue();

			int frequency = 0;
			if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
				frequency = jsonParser.getValueAsInt();
				jsonParser.nextToken();
			}

			if (jsonParser.getCurrentToken() != (JsonToken.END_ARRAY)) {
				throw new IOException("Error deserializing Sequence");
			}

			SequenceDto s = new SequenceDto();
			s.setAccesses(accesses);
			s.setFrequency(frequency);

			return s;
		}

		throw new IOException("Error deserializing Sequence");
	}

}
