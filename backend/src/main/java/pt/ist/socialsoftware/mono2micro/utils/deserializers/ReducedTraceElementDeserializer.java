package deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
// import domain.Access;
// import requitur.ReducedTraceElement;

import java.io.IOException;

public class ReducedTraceElementDeserializer extends StdDeserializer<ReducedTraceElement> {

	public ReducedTraceElementDeserializer() {
		this(null);
	}

	public ReducedTraceElementDeserializer(Class<ReducedTraceElement> t) { super(t); }

	@Override
	public ReducedTraceElement deserialize(
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

				int occurrences = 0;
				if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
					occurrences = jsonParser.getValueAsInt();
					jsonParser.nextToken();
				}

				if (jsonParser.getCurrentToken() != (JsonToken.END_ARRAY)) {
					throw new IOException("Error deserializing ReducedTraceElement - Missing END_ARRAY token on Rule");
				}

				return new Rule(count, occurrences);
			}

			else if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING){ // an access
				String entity = jsonParser.getValueAsString();

				jsonParser.nextValue();
				String type = jsonParser.getValueAsString();

				int occurrences = 0;
				if (jsonParser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
					occurrences = jsonParser.getValueAsInt();
					jsonParser.nextToken();
				}

				if (jsonParser.nextToken() != (JsonToken.END_ARRAY)) {
					throw new IOException("Error deserializing ReducedTraceElement - Missing END_ARRAY token on Access");
				}

				return new Access(entity, Access.Type.valueOf(type));
			}

            else {
                throw new IOException("Error deserializing ReducedTraceElement - Data structure not expected");
            }
		}

		throw new IOException("Error deserializing ReducedTraceElement - First token should be START_ARRAY but was: " + jsonToken);
	}

}