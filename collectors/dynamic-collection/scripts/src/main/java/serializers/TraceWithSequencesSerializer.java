package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.TraceWithAccesses;
import domain.TraceWithSequences;

import java.io.IOException;

public class TraceWithSequencesSerializer extends StdSerializer<TraceWithSequences> {

	public TraceWithSequencesSerializer() {
		this(null);
	}

	public TraceWithSequencesSerializer(Class<TraceWithSequences> t) {
		super(t);
	}

	@Override
	public void serialize(
		TraceWithSequences traceWithSequences,
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		jsonGenerator.writeStartObject();

		jsonGenerator.writeNumberField("id", traceWithSequences.getId());

		if (traceWithSequences.getFrequency() > 1) {
			jsonGenerator.writeNumberField("f", traceWithSequences.getFrequency());
		}

		if (traceWithSequences.getSequences().size() > 0) {
			jsonGenerator.writeObjectField("seqs", traceWithSequences.getSequences());
		}

		jsonGenerator.writeEndObject();
	}
}