package pt.ist.socialsoftware.mono2micro.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Functionality;

import java.io.IOException;

public class FunctionalitySerializer extends StdSerializer<Functionality> {

	public FunctionalitySerializer() {
		this(null);
	}

	public FunctionalitySerializer(Class<Functionality> t) {
		super(t);
	}

	@Override
	public void serialize(
		Functionality functionality,
		JsonGenerator jg,
		SerializerProvider provider
	) throws IOException {
		jg.writeStartObject();
		jg.writeStringField("name", functionality.getName());
		jg.writeObjectField("type", functionality.getType());
		jg.writeArrayFieldStart("metrics");
		functionality.getMetrics().forEach(fr -> {
			try {
				jg.writeObject(fr);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		jg.writeEndArray();
		if(functionality.getType() != null)
			jg.writeStringField("type", functionality.getType().name());
		jg.writeObjectField("entities", functionality.getEntities());
		jg.writeArrayFieldStart("functionalityRedesigns");
			functionality.getFunctionalityRedesigns().forEach(fr -> {
				try {
					jg.writeObject(fr);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		jg.writeEndArray();

		jg.writeObjectField("entitiesPerCluster", functionality.getEntitiesPerCluster());

//		// FIXME DEPRECATED - Left this here in case for some reason there is the need of serializing this type of graph
//		jg.writeFieldName("localTransactionsGraph");
//		if (functionality.getLocalTransactionsGraph() != null) {
//
//			ObjectMapper mapper = new ObjectMapper();
//			SimpleModule module = new SimpleModule("GraphSerializer");
//			module.addSerializer(
//					new LocalTransactionGraphSerializer(
//							(Class<DirectedAcyclicGraph<Graph.LocalTransaction, DefaultEdge>>) functionality
//									.getLocalTransactionsGraph().getClass()
//					)
//			);
//
//			mapper.registerModule(module);
//			String graphString = mapper.writeValueAsString(functionality.getLocalTransactionsGraph());
//			jg.writeRawValue(graphString);
//
//		} else {
//			jg.writeObject(null);
//		}

		jg.writeEndObject();
	}
}