package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DendrogramDeserializer extends StdDeserializer<Dendrogram> {

	public DendrogramDeserializer() {
		this(null);
	}

	public DendrogramDeserializer(Class<Dendrogram> t) { super(t); }

	@Override
	public Dendrogram deserialize(
		JsonParser jsonParser,
		DeserializationContext ctxt
	) throws IOException {
		JsonToken jsonToken = jsonParser.currentToken();

		Set<String> deserializableFields = null;

		try {
			deserializableFields = (Set<String>) ctxt.findInjectableValue(
				"dendrogramDeserializableFields",
				null,
				null
			);

		} catch (Exception ignored) {}

		if (jsonToken == JsonToken.START_OBJECT) {

			Dendrogram dendrogram = new Dendrogram();
			while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
				if (deserializableFields == null || deserializableFields.contains(jsonParser.getCurrentName())) {
					switch (jsonParser.getCurrentName()) {
						case "name":
							dendrogram.setName(jsonParser.getValueAsString());
							break;
						case "codebaseName":
							dendrogram.setCodebaseName(jsonParser.getValueAsString());
							break;
						case "linkageType":
							dendrogram.setLinkageType(jsonParser.getValueAsString());
							break;
						case "accessMetricWeight":
							dendrogram.setAccessMetricWeight(jsonParser.getFloatValue());
							break;
						case "writeMetricWeight":
							dendrogram.setWriteMetricWeight(jsonParser.getFloatValue());
							break;
						case "readMetricWeight":
							dendrogram.setReadMetricWeight(jsonParser.getFloatValue());
							break;
						case "sequenceMetricWeight":
							dendrogram.setSequenceMetricWeight(jsonParser.getFloatValue());
							break;
						case "profile":
							dendrogram.setProfile(jsonParser.getValueAsString());
							break;
						case "decompositions":
							dendrogram.setDecompositions(
								jsonParser.readValueAs(
									new TypeReference<List<Decomposition>>(){}
								)
							);
							break;
						case "tracesMaxLimit":
							dendrogram.setTracesMaxLimit(jsonParser.getIntValue());
							break;
						case "traceType":
							dendrogram.setTraceType(Constants.TraceType.valueOf(jsonParser.getValueAsString()));
							break;

						default:
							throw new IOException("Attribute " + jsonParser.getCurrentName() + " does not exist on Dendrogram object");
					}
				}
				else {
					jsonParser.skipChildren();
				}
			}

			return dendrogram;
		}

		throw new IOException("Error deserializing Dendrogram");
	}
}

