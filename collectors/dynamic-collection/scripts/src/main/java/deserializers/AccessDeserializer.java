package deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import domain.Access;

import java.io.IOException;

public class AccessDeserializer extends StdDeserializer<Access> {

	public AccessDeserializer() {
		this(null);
	}

	public AccessDeserializer(Class<Access> t) { super(t); }

	@Override
	public Access deserialize(
		JsonParser jsonParser,
		DeserializationContext deserializationContext
	) throws IOException {
		JsonToken jsonToken = jsonParser.currentToken();

		if (jsonToken == JsonToken.START_ARRAY) {
			jsonParser.nextValue();
			short entityID = jsonParser.getShortValue();

			jsonParser.nextValue();
			String type = jsonParser.getValueAsString();

			if (jsonParser.nextToken() != (JsonToken.END_ARRAY)) {
				throw new IOException("Error deserializing Access");
			}

			return new Access(entityID, Access.Type.valueOf(type));
		}

		throw new IOException("Error deserializing Access");
	}

}