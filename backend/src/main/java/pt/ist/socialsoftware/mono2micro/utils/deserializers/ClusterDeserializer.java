package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class ClusterDeserializer extends StdDeserializer<Cluster> {

	public ClusterDeserializer() {
		this(null);
	}

	public ClusterDeserializer(Class<Cluster> t) { super(t); }

	@Override
	public Cluster deserialize(
		JsonParser jsonParser,
		DeserializationContext ctxt
	) throws IOException {
		JsonToken jsonToken = jsonParser.currentToken();

		Set<String> deserializableFields = null;

		try {
			deserializableFields = (Set<String>) ctxt.findInjectableValue(
				"clusterDeserializableFields",
				null,
				null
			);

		} catch (Exception ignored) {}


		if (jsonToken == JsonToken.START_OBJECT) {

			Cluster cluster = new Cluster();
			while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
				if (deserializableFields == null || deserializableFields.contains(jsonParser.getCurrentName())) {
					switch (jsonParser.getCurrentName()) {
						case "name":
							cluster.setName(jsonParser.getValueAsString());
							break;
						case "complexity":
							cluster.setComplexity(jsonParser.getFloatValue());
							break;
						case "cohesion":
							cluster.setCohesion(jsonParser.getFloatValue());
							break;
						case "coupling":
							cluster.setCoupling(jsonParser.getFloatValue());
							break;
						case "couplingDependencies":
							cluster.setCouplingDependencies(
								jsonParser.readValueAs(
									new TypeReference<HashMap<String, Set<Short>>>() {}
								)
							);
							break;
						case "entities":
							cluster.setEntities(
								jsonParser.readValueAs(
									new TypeReference<Set<Short>>() {}
								)
							);
							break;

						default:
							throw new IOException("Attribute " + jsonParser.getCurrentName() + " does not exist on Cluster object");
					}
				}
				else {
					jsonParser.skipChildren();
				}
			}

			return cluster;
		}

		throw new IOException("Error deserializing Cluster");
	}
}

