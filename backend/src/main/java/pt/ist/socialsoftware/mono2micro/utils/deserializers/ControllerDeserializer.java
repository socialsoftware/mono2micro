package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.domain.Controller;

import java.io.IOException;
import java.util.HashMap;

public class ControllerDeserializer extends StdDeserializer<Controller> {

	public ControllerDeserializer() {
		this(null);
	}

	public ControllerDeserializer(Class<Controller> t) {
		super(t);
	}

	@Override
	public Controller deserialize(
		JsonParser jsonParser,
		DeserializationContext ctxt
	) throws IOException {
		JsonToken jsonToken = jsonParser.currentToken();

		if (jsonToken == JsonToken.START_OBJECT) {
			jsonParser.nextValue();
			String name = jsonParser.getValueAsString();

			jsonParser.nextValue();
			float complexity = jsonParser.getFloatValue();

			jsonParser.nextValue(); // consume END_ARRAY
			HashMap<String, String> entities = jsonParser.readValueAs(new TypeReference<HashMap<String, String>>() {});

			jsonParser.nextValue();

			DirectedAcyclicGraph<Controller.LocalTransaction, DefaultEdge> graph = getGraph(jsonParser);

			jsonParser.nextValue();

			return new Controller(
				name,
				complexity,
				entities,
				graph
			);
		}

		return null;
	}

	private DirectedAcyclicGraph<Controller.LocalTransaction, DefaultEdge> getGraph(JsonParser jsonParser) throws IOException {
		DirectedAcyclicGraph<Controller.LocalTransaction, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);

		HashMap<Integer, Controller.LocalTransaction> idToVertexMap = new HashMap<>();

		jsonParser.nextValue(); // nodes
		while (jsonParser.nextValue() != JsonToken.END_ARRAY) {
			Controller.LocalTransaction lt = jsonParser.readValueAs(Controller.LocalTransaction.class);
			graph.addVertex(lt);
			idToVertexMap.put(lt.getId(), lt);
		}

		jsonParser.nextValue(); // links
		while (jsonParser.nextValue() != JsonToken.END_ARRAY) {
			String link = jsonParser.getValueAsString();
			int index = link.indexOf("->");
			int fromId = Integer.parseInt(link.substring(0, index));
			int toId = Integer.parseInt(link.substring(link.indexOf("->") + 2));
			graph.addEdge(idToVertexMap.get(fromId), idToVertexMap.get(toId));
		}

		jsonParser.nextValue(); // Consume End Array

		return graph;
	}
}