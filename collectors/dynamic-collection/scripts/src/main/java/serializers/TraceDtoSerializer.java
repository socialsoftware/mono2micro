package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import dto.TraceDto;

import java.io.IOException;

public class TraceDtoSerializer extends StdSerializer<TraceDto> {
	public TraceDtoSerializer() {
		this(null);
	}

	public TraceDtoSerializer(Class<TraceDto> t) {
		super(t);
	}

	@Override
	public void serialize(
		TraceDto trace,
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		jsonGenerator.writeStartObject();

		jsonGenerator.writeNumberField("id", trace.getId());

		if (trace.getFrequency() > 1) {
			jsonGenerator.writeNumberField("f", trace.getFrequency());
		}

		if (trace.getElements() != null && trace.getElements().size() > 0) {
			jsonGenerator.writeObjectField("a", trace.getElements());
		}

		jsonGenerator.writeEndObject();
	}
}