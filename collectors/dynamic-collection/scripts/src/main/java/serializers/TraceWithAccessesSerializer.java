package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.TraceWithAccesses;

import java.io.IOException;

public class TraceWithAccessesSerializer extends StdSerializer<TraceWithAccesses> {

	public TraceWithAccessesSerializer() {
		this(null);
	}

	public TraceWithAccessesSerializer(Class<TraceWithAccesses> t) {
		super(t);
	}

	@Override
	public void serialize(
		TraceWithAccesses trace,
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		// FIXME if i don't have any accesses, don't write
		jsonGenerator.writeStartObject();

		jsonGenerator.writeNumberField("id", trace.getId());

		if (trace.getFrequency() > 1) {
			jsonGenerator.writeNumberField("f", trace.getFrequency());
		}

		if (trace.getAccesses().size() > 0) {
			jsonGenerator.writeObjectField("a", trace.getAccesses());
		}

		jsonGenerator.writeEndObject();
	}
}