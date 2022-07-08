package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.metrics.Metric;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class FunctionalityDeserializer extends StdDeserializer<Functionality> {

	public FunctionalityDeserializer() {
		this(null);
	}

	public FunctionalityDeserializer(Class<Functionality> t) {
		super(t);
	}

	@Override
	public Functionality deserialize(
		JsonParser jsonParser,
		DeserializationContext ctxt
	) throws IOException {
		JsonToken jsonToken = jsonParser.currentToken();

		Set<String> deserializableFields = null;

		try {
			deserializableFields = (Set<String>) ctxt.findInjectableValue(
				"functionalityDeserializableFields",
				null,
				null
			);

		} catch (Exception ignored) {}

		if (jsonToken == JsonToken.START_OBJECT) {

			Functionality functionality = new Functionality();
			while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
				if (deserializableFields == null || deserializableFields.contains(jsonParser.getCurrentName())) {
					switch (jsonParser.getCurrentName()) {
						case "name":
							functionality.setName(jsonParser.getValueAsString());
							break;
						case "metrics":
							functionality.setMetrics(
								jsonParser.readValueAs(
									new TypeReference<List<Metric>>() {}
								)
							);
							break;
						case "entities":
							functionality.setEntities(
								jsonParser.readValueAs(
									new TypeReference<HashMap<Short, Byte>>() {}
								)
							);
							break;
						case "functionalityRedesigns":
							functionality.setFunctionalityRedesigns(
									jsonParser.readValueAs(
											new TypeReference<List<FunctionalityRedesign>>() {}
									));
							break;
						case "type":
							functionality.setType(FunctionalityType.valueOf(jsonParser.getValueAsString()));
							break;
						case "entitiesPerCluster":
							functionality.setEntitiesPerCluster(
									jsonParser.readValueAs(
											new TypeReference<HashMap<Short, Set<Short>>>() {}
									)
							);
							break;

						default:
							throw new IOException("Attribute " + jsonParser.getCurrentName() + " does not exist on Functionality object");
					}
				}
				else {
					jsonParser.skipChildren();
				}
			}

			return functionality;
		}

		throw new IOException("Error deserializing Functionality");
	}
}