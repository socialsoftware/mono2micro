package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.AccessWithFrequency;

import java.io.IOException;

public class AccessWithFrequencySerializer extends StdSerializer<AccessWithFrequency> {

	public AccessWithFrequencySerializer() {
		this(null);
	}

	public AccessWithFrequencySerializer(Class<AccessWithFrequency> t) {
		super(t);
	}

	@Override
	public void serialize(
		AccessWithFrequency accessWithFrequency,
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		jsonGenerator.writeStartArray();

		jsonGenerator.writeString(accessWithFrequency.getType().name());
		jsonGenerator.writeNumber(accessWithFrequency.getEntityID());

		if (accessWithFrequency.getFrequency() > 1) {
			jsonGenerator.writeNumber(accessWithFrequency.getFrequency());
		}

		jsonGenerator.writeEndArray();
	}
}