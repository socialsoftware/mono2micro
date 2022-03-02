package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CodebaseDeserializer extends StdDeserializer<Codebase> {

	public CodebaseDeserializer() {
		this(null);
	}

	public CodebaseDeserializer(Class<Codebase> t) { super(t); }

	@Override
	public Codebase deserialize(
		JsonParser jsonParser,
		DeserializationContext ctxt
	) throws IOException {
		JsonToken jsonToken = jsonParser.currentToken();

		Set<String> deserializableFields = null;

		try {
			deserializableFields = (Set<String>) ctxt.findInjectableValue(
				"codebaseDeserializableFields",
				null,
				null
			);

		} catch (Exception ignored) {}

		if (jsonToken == JsonToken.START_OBJECT) {

			Codebase codebase = new Codebase();
			while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
				if (deserializableFields == null || deserializableFields.contains(jsonParser.getCurrentName())) {
					switch (jsonParser.getCurrentName()) {
						case "name":
							codebase.setName(jsonParser.getValueAsString());
							break;

						case "dendrograms":
							codebase.setDendrograms(
								jsonParser.readValueAs(
									new TypeReference<List<Dendrogram>>(){}
								)
							);
							break;

						case "profiles":
							codebase.setProfiles(
								jsonParser.readValueAs(
									new TypeReference<Map<String, Set<String>>>(){}
								)
							);
							break;

						case "datafilePath":
							codebase.setDatafilePath(jsonParser.getValueAsString());
							break;

						default:
							throw new IOException("Attribute " + jsonParser.getCurrentName() + " does not exist on Codebase object");
					}
				}
				else {
					jsonParser.skipChildren();
				}
			}

			return codebase;
		}

		throw new IOException("Error deserializing Access");
	}
}

