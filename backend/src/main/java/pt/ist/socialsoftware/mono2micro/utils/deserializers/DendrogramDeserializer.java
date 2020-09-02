package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
		Set<String> deserializableFields = (Set<String>) ctxt.getAttribute("dendrogramDeserializableFields");

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
						case "profiles":
							dendrogram.setProfiles(jsonParser.readValueAs(new TypeReference<List<Dendrogram>>(){}));
							break;
						case "graphs":
							dendrogram.setGraphs(jsonParser.readValueAs(new TypeReference<List<Graph>>(){}));
							break;
						case "tracesMaxLimit":
							dendrogram.setTracesMaxLimit(jsonParser.getIntValue());
							break;
						case "typeOfTraces":
							dendrogram.setTypeOfTraces(Constants.TypeOfTraces.valueOf(jsonParser.getValueAsString()));
							break;

						default:
							throw new IOException();
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

