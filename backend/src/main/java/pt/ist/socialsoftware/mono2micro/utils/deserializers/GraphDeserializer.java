package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class GraphDeserializer extends StdDeserializer<Graph> {

	public GraphDeserializer() {
		this(null);
	}

	public GraphDeserializer(Class<Graph> t) { super(t); }

	@Override
	public Graph deserialize(
		JsonParser jsonParser,
		DeserializationContext ctxt
	) throws IOException {
		JsonToken jsonToken = jsonParser.currentToken();
		Set<String> deserializableFields = (Set<String>) ctxt.getAttribute("graphDeserializableFields");

		if (jsonToken == JsonToken.START_OBJECT) {

			Graph dendrogram = new Graph();
			while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
				if (deserializableFields == null || deserializableFields.contains(jsonParser.getCurrentName())) {
					switch (jsonParser.getCurrentName()) {
						case "name":
							dendrogram.setName(jsonParser.getValueAsString());
							break;
						case "codebaseName":
							dendrogram.setCodebaseName(jsonParser.getValueAsString());
							break;
						case "dendrogramName":
							dendrogram.setDendrogramName(jsonParser.getValueAsString());
							break;
						case "cutType":
							dendrogram.setCutType(jsonParser.getValueAsString());
							break;
						case "cutValue":
							dendrogram.setCutValue(jsonParser.getFloatValue());
							break;
						case "silhouetteScore":
							dendrogram.setSilhouetteScore(jsonParser.getFloatValue());
							break;
						case "complexity":
							dendrogram.setComplexity(jsonParser.getFloatValue());
							break;
						case "cohesion":
							dendrogram.setCohesion(jsonParser.getFloatValue());
							break;
						case "coupling":
							dendrogram.setCoupling(jsonParser.getFloatValue());
							break;
						case "controllers":
							dendrogram.setControllers(jsonParser.readValueAs(new TypeReference<List<Controller>>(){}));
							break;
						case "clusters":
							dendrogram.setClusters(jsonParser.readValueAs(new TypeReference<List<Cluster>>(){}));
							break;
						case "expert":
							dendrogram.setExpert(jsonParser.getBooleanValue());
							break;

						default:
							throw new IOException("Attribute " + jsonParser.getCurrentName() + " does not exist on Graph object");
					}
				}
				else {
					jsonParser.skipChildren();
				}
			}

			return dendrogram;
		}

		throw new IOException("Error deserializing Access");
	}
}

