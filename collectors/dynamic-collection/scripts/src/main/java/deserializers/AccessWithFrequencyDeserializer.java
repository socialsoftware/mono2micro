package deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import domain.Access;
import domain.AccessWithFrequency;

import java.io.IOException;

public class AccessWithFrequencyDeserializer extends StdDeserializer<AccessWithFrequency> {

	public AccessWithFrequencyDeserializer() {
		this(null);
	}

	public AccessWithFrequencyDeserializer(Class<AccessWithFrequency> t) { super(t); }

	@Override
	public AccessWithFrequency deserialize(
		JsonParser jsonParser,
		DeserializationContext deserializationContext
	) throws IOException {
		JsonToken jsonToken = jsonParser.currentToken();

		if (jsonToken == JsonToken.START_ARRAY) {
			jsonParser.nextValue();
			String entity = jsonParser.getValueAsString();

			jsonParser.nextValue();
			String type = jsonParser.getValueAsString();

			int frequency = 0;
			if (jsonParser.nextValue() == JsonToken.VALUE_NUMBER_INT) {
				frequency = jsonParser.getValueAsInt();
			}

			if (jsonParser.nextToken() != (JsonToken.END_ARRAY)) {
				throw new IOException("Error deserializing Access");
			}

			return new AccessWithFrequency(entity, Access.Type.valueOf(type), frequency);
		}

		throw new IOException("Error deserializing Access");
	}

}