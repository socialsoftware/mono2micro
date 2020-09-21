package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.dto.RuleDto;

import java.io.IOException;

public class ReducedTraceElementDtoDeserializer extends StdDeserializer<ReducedTraceElementDto> {

	public ReducedTraceElementDtoDeserializer() {
		this(null);
	}

	public ReducedTraceElementDtoDeserializer(Class<ReducedTraceElementDto> t) { super(t); }

	@Override
	public ReducedTraceElementDto deserialize(
		JsonParser jsonParser,
		DeserializationContext deserializationContext
	) throws IOException {
		JsonToken jsonToken = jsonParser.currentToken();

		if (jsonToken == JsonToken.START_ARRAY) {
			jsonParser.nextValue();

			// check if value is of type Number
			// if yes, then it is a rule
			if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) { // a rule
				int count = jsonParser.getValueAsInt();

				jsonParser.nextValue();

				int occurrences = 1;
				if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
					occurrences = jsonParser.getValueAsInt();
					jsonParser.nextValue();
				}

				if (jsonParser.getCurrentToken() != (JsonToken.END_ARRAY)) {
					throw new IOException("Error deserializing ReducedTraceElementDto - Missing END_ARRAY token on RuleDto");
				}

				RuleDto r = new RuleDto();

				r.setCount(count);
				r.setOccurrences(occurrences);

				return r;
			}

			else if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING){ // an access
				String entity = jsonParser.getValueAsString();

				jsonParser.nextValue();
				String mode = jsonParser.getValueAsString();

				jsonParser.nextValue();

				int occurrences = 1;
				if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
					occurrences = jsonParser.getValueAsInt();
					jsonParser.nextValue();
				}

				if (jsonParser.getCurrentToken() != (JsonToken.END_ARRAY)) {
					throw new IOException("Error deserializing ReducedTraceElementDto - Missing END_ARRAY token on Access");
				}

				AccessDto a = new AccessDto();

				a.setEntity(entity);
				a.setMode(mode);
				a.setOccurrences(occurrences);

				return a;
			}

            else {
                throw new IOException("Error deserializing ReducedTraceElementDto - Data structure not expected");
            }
		}

		throw new IOException("Error deserializing ReducedTraceElementDto - First token should be START_ARRAY but was: " + jsonToken);
	}

}