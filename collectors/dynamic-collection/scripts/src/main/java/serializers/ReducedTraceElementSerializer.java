package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.Access;
import requitur.ReducedTraceElement;
import requitur.content.Content;
import requitur.content.RuleContent;

import java.io.IOException;

public class ReducedTraceElementSerializer extends StdSerializer<ReducedTraceElement> {

	public ReducedTraceElementSerializer() {
		this(null);
	}

	public ReducedTraceElementSerializer(Class<ReducedTraceElement> t) {
		super(t);
	}

	@Override
	public void serialize(
		ReducedTraceElement reducedTraceElement,
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		jsonGenerator.writeStartArray();

		Content c = reducedTraceElement.getValue();

		if (c instanceof RuleContent) {
			final RuleContent rc = (RuleContent) c;

			jsonGenerator.writeNumber(rc.getCount());

		} else if (c instanceof Access) {
			final Access a = (Access) c;

			jsonGenerator.writeString(a.getEntity());
			jsonGenerator.writeString(a.getType().name());
		}

		int occurrences = reducedTraceElement.getOccurrences();

		if (occurrences > 1)
			jsonGenerator.writeNumber(occurrences);

		jsonGenerator.writeEndArray();
	}
}