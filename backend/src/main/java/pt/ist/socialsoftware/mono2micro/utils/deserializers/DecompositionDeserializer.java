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

	public DecompositionDeserializer(Class<Decomposition> t) { super(t); }

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

		} catch (Exception ignored) {}

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
						case "dendrogramName":
							decomposition.setDendrogramName(jsonParser.getValueAsString());
							break;
						case "cutType":
							decomposition.setCutType(jsonParser.getValueAsString());
							break;
						case "cutValue":
							decomposition.setCutValue(jsonParser.getFloatValue());
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
									new TypeReference<Map<String, Cluster>>(){}
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
						case "entityIDToClusterName":
							decomposition.setEntityIDToClusterName(
								jsonParser.readValueAs(
									new TypeReference<Map<Short, String>>(){}
								)
							);
						case "expert":
							decomposition.setExpert(jsonParser.getBooleanValue());
							break;
//						case "localTransactionsGraph": FIXME For now it's not necessary to deserialize LT graphs
//							graph.setLocalTransactionsGraph(getGraph(jsonParser));
//							break;

						default:
							throw new IOException("Attribute " + jsonParser.getCurrentName() + " does not exist on Graph object");
					}
				}
				else {
					jsonParser.skipChildren();
				}
			}

			return decomposition;
		}

		throw new IOException("Error deserializing Access");
	}

	// FIXME DEPRECATED - NOT BEING USED
//	private DirectedAcyclicGraph<Graph.LocalTransaction, DefaultEdge> getGraph(
//		JsonParser jsonParser
//	)
//		throws IOException
//	{
//		DirectedAcyclicGraph<Graph.LocalTransaction, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
//
//		HashMap<Integer, Graph.LocalTransaction> idToVertexMap = new HashMap<>();
//
//		jsonParser.nextValue(); // nodes
//		while (jsonParser.nextValue() != JsonToken.END_ARRAY) {
//			Graph.LocalTransaction lt = jsonParser.readValueAs(Graph.LocalTransaction.class);
//			graph.addVertex(lt);
//			idToVertexMap.put(lt.getId(), lt);
//		}
//
//		jsonParser.nextValue(); // links
//		while (jsonParser.nextValue() != JsonToken.END_ARRAY) {
//			String link = jsonParser.getValueAsString();
//			int index = link.indexOf("->");
//			int fromId = Integer.parseInt(link.substring(0, index));
//			int toId = Integer.parseInt(link.substring(link.indexOf("->") + 2));
//			graph.addEdge(idToVertexMap.get(fromId), idToVertexMap.get(toId));
//		}
//
//		jsonParser.nextValue(); // Consume End Array
//
//		return graph;
//	}
}

