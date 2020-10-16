package pt.ist.socialsoftware.mono2micro.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;

import java.io.IOException;

public class AccessDtoSerializer extends StdSerializer<AccessDto> {

	public AccessDtoSerializer() {
		this(null);
	}

	public AccessDtoSerializer(Class<AccessDto> t) {
		super(t);
	}

	@Override
	public void serialize(
		AccessDto access,
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		jsonGenerator.writeStartArray();
		jsonGenerator.writeString(access.getMode() == 1 ? "R" : "W");
		jsonGenerator.writeNumber(access.getEntityID());

		if (access.getOccurrences() > 1) {
			jsonGenerator.writeNumber(access.getOccurrences());
		}

		jsonGenerator.writeEndArray();
	}
}