package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import dto.AccessDto;
import dto.ReducedTraceElementDto;
import dto.RuleDto;

import java.io.IOException;

public class ReducedTraceElementDtoSerializer extends StdSerializer<ReducedTraceElementDto> {

	public ReducedTraceElementDtoSerializer() {
		this(null);
	}

	public ReducedTraceElementDtoSerializer(Class<ReducedTraceElementDto> t) {
		super(t);
	}

	@Override
	public void serialize(
		ReducedTraceElementDto reducedTraceElement,
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		jsonGenerator.writeStartArray();

		if (reducedTraceElement instanceof RuleDto) {
			final RuleDto rc = (RuleDto) reducedTraceElement;

			jsonGenerator.writeNumber(rc.getCount());

		} else if (reducedTraceElement instanceof AccessDto) {
			final AccessDto a = (AccessDto) reducedTraceElement;

			jsonGenerator.writeString(a.getMode());
			jsonGenerator.writeNumber(a.getEntityID());
		}

		int occurrences = reducedTraceElement.getOccurrences();

		if (occurrences > 1)
			jsonGenerator.writeNumber(occurrences);

		jsonGenerator.writeEndArray();
	}
}