package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class DecompositionDeserializer extends StdDeserializer<Decomposition> {

	public DecompositionDeserializer() {
		this(null);
	}

	public DecompositionDeserializer(Class<Decomposition> t) {
		super(t);
	}

	@Override
	public Decomposition deserialize(
			JsonParser jsonParser,
			DeserializationContext ctxt
	) throws IOException {
		JsonToken jsonToken = jsonParser.currentToken();

		Set<String> deserializableFields = null;

		try {
			deserializableFields = (Set<String>) ctxt.findInjectableValue(
					"decompositionDeserializableFields",
					null,
					null
			);

		} catch (Exception ignored) {
		}

		if (jsonToken == JsonToken.START_OBJECT) {
			Decomposition decomposition = new Decomposition();

			while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
				if (deserializableFields == null || deserializableFields.contains(jsonParser.getCurrentName())) {
					switch (jsonParser.getCurrentName()) {
						case "name":
							decomposition.setName(jsonParser.getValueAsString());
							break;
						case "codebaseName":
							decomposition.setCodebaseName(jsonParser.getValueAsString());
							break;
						case "strategyName":
							decomposition.setStrategyName(jsonParser.getValueAsString());
							break;
						case "silhouetteScore":
							decomposition.setSilhouetteScore(jsonParser.getFloatValue());
							break;
						case "complexity":
							decomposition.setComplexity(jsonParser.getFloatValue());
							break;
						case "performance":
							decomposition.setPerformance(jsonParser.getFloatValue());
							break;
						case "cohesion":
							decomposition.setCohesion(jsonParser.getFloatValue());
							break;
						case "coupling":
							decomposition.setCoupling(jsonParser.getFloatValue());
							break;
						case "clusters":
							decomposition.setClusters(
								jsonParser.readValueAs(
									new TypeReference<Map<Short, Cluster>>(){}
								)
							);
							break;

						case "controllers":
							decomposition.setControllers(
								jsonParser.readValueAs(
									new TypeReference<Map<String, Controller>>(){}
								)
							);

							break;
						case "entityIDToClusterID":
							decomposition.setEntityIDToClusterID(
								jsonParser.readValueAs(
									new TypeReference<Map<Short, Short>>(){}
								)
							);
							break;
						case "expert":
							decomposition.setExpert(jsonParser.getBooleanValue());
							break;

						default:
							throw new IOException("Attribute " + jsonParser.getCurrentName() + " does not exist on Decomposition object");
					}
				} else {
					jsonParser.skipChildren();
				}
			}

			return decomposition;
		}

		throw new IOException("Error deserializing Access");
	}
}