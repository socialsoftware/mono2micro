package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.dto.SimilarityMatrixDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SimilarityMatrixDtoDeserializer extends StdDeserializer<SimilarityMatrixDto> {

	public SimilarityMatrixDtoDeserializer() {
		this(null);
	}

	public SimilarityMatrixDtoDeserializer(Class<SimilarityMatrixDto> t) { super(t); }

	@Override
	public SimilarityMatrixDto deserialize(
		JsonParser jsonParser,
		DeserializationContext ctxt
	)
		throws IOException
	{
		Set<String> deserializableFields = null;

		try {
			deserializableFields = (Set<String>) ctxt.findInjectableValue(
				"similarityMatrixDtoDeserializableFields",
				null,
				null
			);

		} catch (Exception ignored) {}

		JsonToken jsonToken = jsonParser.currentToken();

		if (jsonToken == JsonToken.START_OBJECT) {

			SimilarityMatrixDto similarityMatrixDto = new SimilarityMatrixDto();
			while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
				if (deserializableFields == null || deserializableFields.contains(jsonParser.getCurrentName())) {
					switch (jsonParser.getCurrentName()) {
						case "linkageType":
							similarityMatrixDto.setLinkageType(jsonParser.getValueAsString());
							break;
						case "entities":
							similarityMatrixDto.setEntities(jsonParser.readValueAs(new TypeReference<Set<String>>() {}));
							break;
						case "couplingDependencies":
							similarityMatrixDto.setMatrix(jsonParser.readValueAs(new TypeReference<List<List<List<Float>>>>() {}));
							break;

						default:
							throw new IOException("Attribute " + jsonParser.getCurrentName() + " does not exist on SimilarityMatrixDto object");
					}
				}
				else {
					jsonParser.skipChildren();
				}
			}

			return similarityMatrixDto;
		}

		throw new IOException("Error deserializing SimilarityMatrixDto");
	}
}