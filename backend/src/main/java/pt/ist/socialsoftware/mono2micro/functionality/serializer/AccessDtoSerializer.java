package pt.ist.socialsoftware.mono2micro.functionality.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;

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
		if (access.getMode() == AccessDto.READ_MODE)
			jsonGenerator.writeString("R");
		else if (access.getMode() == AccessDto.UPDATE_MODE)
			jsonGenerator.writeString("U");
		else if (access.getMode() == AccessDto.READ_UPDATE_MODE)
			jsonGenerator.writeString("RU");
		else if (access.getMode() == AccessDto.CREATE_MODE)
			jsonGenerator.writeString("C");
		else if (access.getMode() == AccessDto.READ_CREATE_MODE)
			jsonGenerator.writeString("RC");
		else if (access.getMode() == AccessDto.DELETE_MODE)
			jsonGenerator.writeString("D");
		else
			jsonGenerator.writeString("RD");
		jsonGenerator.writeNumber(access.getEntityID());

		if (access.getOccurrences() > 1) {
			jsonGenerator.writeNumber(access.getOccurrences());
		}

		jsonGenerator.writeEndArray();
	}
}