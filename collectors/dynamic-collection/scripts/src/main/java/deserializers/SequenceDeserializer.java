package deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import domain.Access;
import domain.AccessWithFrequency;
import domain.Sequence;

import java.io.IOException;
import java.util.List;

public class SequenceDeserializer extends StdDeserializer<Sequence<?>> {

	public SequenceDeserializer() {
		this(null);
	}

	public SequenceDeserializer(Class<Sequence<?>> t) { super(t); }

	@Override
	public Sequence<?> deserialize(
		JsonParser jsonParser,
		DeserializationContext deserializationContext
	) throws IOException {
		JsonToken jsonToken = jsonParser.currentToken();

		if (jsonToken == JsonToken.START_ARRAY) {
			jsonParser.nextValue();
			List<Access> accesses = null;
			List<AccessWithFrequency> accessesWithFrequency = null;

			try {
				accesses = jsonParser.readValueAs(new TypeReference<List<Access>>(){});

			} catch (IOException e) {
				accessesWithFrequency = jsonParser.readValueAs(new TypeReference<List<AccessWithFrequency>>(){});
			}

			int frequency = 0;
			if (jsonParser.nextValue() == JsonToken.VALUE_NUMBER_INT) {
				frequency = jsonParser.getValueAsInt();
			}

			if (jsonParser.nextToken() != (JsonToken.END_ARRAY)) {
				throw new IOException("Error deserializing Sequence");
			}

			if (accesses != null) {
				return new Sequence<>(accesses, frequency);

			} else if (accessesWithFrequency != null) {
				return new Sequence<>(accessesWithFrequency, frequency);

			} else {
				throw new IOException("Error deserializing Sequence");
			}
		}

		throw new IOException("Error deserializing Sequence");
	}

}