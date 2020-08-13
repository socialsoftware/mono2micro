package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.nio.json.JSONImporter;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.AccessWithFrequencyDto;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

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
	) throws IOException, JsonProcessingException {
		JsonToken jsonToken = jp.currentToken();

		if (jsonToken == JsonToken.START_OBJECT) {
			jp.nextValue();
			String name = jp.getValueAsString();

			jp.nextValue();
			float complexity = jp.getFloatValue();

			jp.nextValue(); // consume END_ARRAY
			HashMap<String, String> entities = jp.readValueAs(new TypeReference<HashMap<String, String>>() {});

			jp.nextValue();

			JSONImporter<Controller.LocalTransaction, DefaultEdge> importer = new JSONImporter<>();

			ObjectMapper mapper = new ObjectMapper();

			importer.addGraphAttributeConsumer((field, value) -> {
				System.out.println("field: "+ field);
				System.out.println("value: "+ value);
			});

			importer.addVertexAttributeConsumer((field, value) -> {
				System.out.println("field: "+ field);
				System.out.println("value: "+ value);

				Controller.LocalTransaction lt = field.getFirst();
				switch (field.getSecond()) {
					case "_id":
						lt.setId(Integer.parseInt(value.getValue()));
						break;

					case "clusterName":
						lt.setClusterName(value.getValue());
						break;

					case "clusterAccesses":
						try {
							lt.setClusterAccesses(
								mapper.readValue(
									value.getValue(),
									new TypeReference<List<AccessWithFrequencyDto>>() {}
								)
							);
						} catch (IOException ioException) {
							ioException.printStackTrace();
						}
						break;
					default:
						break;
				}
			});

			importer.addVertexConsumer((value) -> {
				System.out.println("value: "+ value);
			});

			DirectedAcyclicGraph<Controller.LocalTransaction, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);

			Supplier<Controller.LocalTransaction> vSupplier = new Supplier<Controller.LocalTransaction>()
			{
				private int id = 0;

				@Override
				public Controller.LocalTransaction get()
				{
					return new Controller.LocalTransaction(--id);
				}
			};

			graph.setVertexSupplier(vSupplier);


			importer.importGraph(graph, new StringReader(jp.getValueAsString()));

			Controller c = new Controller(
				name,
				complexity,
				entities,
				graph
			);

			return c;
		}

		return null;
	}
}