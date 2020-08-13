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
		JsonParser jp,
		DeserializationContext ctxt
	) throws IOException {
		JsonToken jsonToken = jp.currentToken();

		if (jsonToken == JsonToken.START_OBJECT) {
			jp.nextValue();
			String name = jp.getValueAsString();

			jp.nextValue();
			float complexity = jp.getFloatValue();

			jp.nextValue(); // consume END_ARRAY
			HashMap<String, String> entities = jp.readValueAs(new TypeReference<HashMap<String, String>>() {});

			jp.nextValue();

			DirectedAcyclicGraph<Controller.LocalTransaction, DefaultEdge> graph = getGraph(jp);

			return new Controller(
				name,
				complexity,
				entities,
				graph
			);
		}

		return null;
	}

	private DirectedAcyclicGraph<Controller.LocalTransaction, DefaultEdge> getGraph(JsonParser jp) throws IOException {
		DirectedAcyclicGraph<Controller.LocalTransaction, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);

		HashMap<Integer, Controller.LocalTransaction> idToVertexMap = new HashMap<>();

		jp.nextValue(); // nodes
		while (jp.nextValue() != JsonToken.END_ARRAY) {
			Controller.LocalTransaction lt = jp.readValueAs(Controller.LocalTransaction.class);
			graph.addVertex(lt);
			idToVertexMap.put(lt.getId(), lt);
		}

		jp.nextValue(); // links
		while (jp.nextValue() != JsonToken.END_ARRAY) {
			String link = jp.getValueAsString();
			int index = link.indexOf("->");
			int fromId = Integer.parseInt(link.substring(0, index));
			int toId = Integer.parseInt(link.substring(link.indexOf("->") + 2));
			graph.addEdge(idToVertexMap.get(fromId), idToVertexMap.get(toId));
		}

		jp.nextValue(); // Consume End Array

		return graph;
	}
}