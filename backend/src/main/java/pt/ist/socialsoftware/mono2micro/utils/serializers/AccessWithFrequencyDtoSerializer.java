package pt.ist.socialsoftware.mono2micro.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pt.ist.socialsoftware.mono2micro.dto.AccessWithFrequencyDto;

import java.io.IOException;

public class AccessWithFrequencyDtoSerializer extends StdSerializer<AccessWithFrequencyDto> {

	public AccessWithFrequencyDtoSerializer() {
		this(null);
	}

	public AccessWithFrequencyDtoSerializer(Class<AccessWithFrequencyDto> t) {
		super(t);
	}

	@Override
	public void serialize(
		AccessWithFrequencyDto accessWithFrequency,
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		jsonGenerator.writeStartArray();

		jsonGenerator.writeString(accessWithFrequency.getEntity());
		jsonGenerator.writeString(accessWithFrequency.getMode());

		if (accessWithFrequency.getFrequency() > 1) {
			jsonGenerator.writeNumber(accessWithFrequency.getFrequency());
		}

		jsonGenerator.writeEndArray();
	}
}