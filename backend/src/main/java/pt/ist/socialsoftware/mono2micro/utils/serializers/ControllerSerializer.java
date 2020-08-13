package pt.ist.socialsoftware.mono2micro.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.json.JSONExporter;
import pt.ist.socialsoftware.mono2micro.domain.Controller;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

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

		JSONExporter<Controller.LocalTransaction, DefaultEdge> exporter = new JSONExporter<>();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

		exporter.setVertexAttributeProvider((lt) -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			map.put("_id", DefaultAttribute.createAttribute(lt.getId()));

			map.put("clusterName", DefaultAttribute.createAttribute(lt.getClusterName()));

			try {
				map.put("clusterAccesses", DefaultAttribute.createAttribute(
					mapper.writeValueAsString(lt.getClusterAccesses())
				));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			return map;
		});

		Writer writer = new StringWriter();
		exporter.exportGraph(controller.getLocalTransactionsGraph(), writer);

		jg.writeStringField("localTransactionsGraph", writer.toString());
		jg.writeEndObject();
	}
}