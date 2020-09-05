package pt.ist.socialsoftware.mono2micro.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.domain.Controller;

import java.io.IOException;

public class ControllerSerializer extends StdSerializer<Controller> {

	public ControllerSerializer() {
		this(null);
	}

	public ControllerSerializer(Class<Controller> t) {
		super(t);
	}

	@Override
	public void serialize(
		Controller controller,
		JsonGenerator jg,
		SerializerProvider provider
	) throws IOException {
		jg.writeStartObject();
		jg.writeStringField("name", controller.getName());
		jg.writeNumberField("complexity", controller.getComplexity());
		jg.writeObjectField("entities", controller.getEntities());

		jg.writeFieldName("localTransactionsGraph");
		if (controller.getLocalTransactionsGraph() != null) {

			ObjectMapper mapper = new ObjectMapper();
			SimpleModule module = new SimpleModule("GraphSerializer");
			module.addSerializer(
					new GraphSerializer(
							(Class<DirectedAcyclicGraph<Controller.LocalTransaction, DefaultEdge>>) controller
									.getLocalTransactionsGraph().getClass()
					)
			);

			mapper.registerModule(module);
			String graphString = mapper.writeValueAsString(controller.getLocalTransactionsGraph());
			jg.writeRawValue(graphString);
		}

		jg.writeEndObject();
	}
}