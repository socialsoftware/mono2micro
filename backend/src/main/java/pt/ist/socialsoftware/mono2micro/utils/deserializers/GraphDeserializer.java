package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Graph;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

		Set<String> deserializableFields = null;

		try {
			deserializableFields = (Set<String>) ctxt.findInjectableValue(
				"graphDeserializableFields",
				null,
				null
			);

		} catch (Exception ignored) {}

		if (jsonToken == JsonToken.START_OBJECT) {
			Graph graph = new Graph();

			while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
				if (deserializableFields == null || deserializableFields.contains(jsonParser.getCurrentName())) {
					switch (jsonParser.getCurrentName()) {
						case "name":
							graph.setName(jsonParser.getValueAsString());
							break;
						case "codebaseName":
							graph.setCodebaseName(jsonParser.getValueAsString());
							break;
						case "dendrogramName":
							graph.setDendrogramName(jsonParser.getValueAsString());
							break;
						case "cutType":
							graph.setCutType(jsonParser.getValueAsString());
							break;
						case "cutValue":
							graph.setCutValue(jsonParser.getFloatValue());
							break;
						case "silhouetteScore":
							graph.setSilhouetteScore(jsonParser.getFloatValue());
							break;
						case "complexity":
							graph.setComplexity(jsonParser.getFloatValue());
							break;
						case "performance":
							graph.setPerformance(jsonParser.getFloatValue());
							break;
						case "cohesion":
							graph.setCohesion(jsonParser.getFloatValue());
							break;
						case "coupling":
							graph.setCoupling(jsonParser.getFloatValue());
							break;
						case "clusters":
							graph.setClusters(
								jsonParser.readValueAs(
									new TypeReference<Map<String, Cluster>>(){}
								)
							);
							break;
						case "expert":
							graph.setExpert(jsonParser.getBooleanValue());
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

			return graph;
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

