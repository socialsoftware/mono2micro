package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.Access;
import domain.Functionality;

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
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		jsonGenerator.writeStartObject();

		if (functionality.getFrequency() > 1) {
			jsonGenerator.writeNumberField("f", functionality.getFrequency());
		}

		if (functionality.getTraces().size() > 0) {
			jsonGenerator.writeObjectField("traces", functionality.getTraces());
		}

		jsonGenerator.writeEndObject();
	}
}