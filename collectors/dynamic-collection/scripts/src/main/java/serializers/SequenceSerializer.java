package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.Sequence;

import java.io.IOException;

public class SequenceSerializer extends StdSerializer<Sequence<?>> {

	public SequenceSerializer() {
		this(null);
	}

	public SequenceSerializer(Class<Sequence<?>> t) {
		super(t);
	}

	@Override
	public void serialize(
		Sequence<?> sequence,
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		jsonGenerator.writeStartArray();
		jsonGenerator.writeObject(sequence.getAccesses());

		// TODO somehow the writeObject sometimes fails others doesn't so will leave the code below here in case it fails again
//		jsonGenerator.writeStartArray();
//
//		for (Access acc : sequence.getAccesses()) {
//			jsonGenerator.writeStartArray();
//			jsonGenerator.writeString(acc.getEntity());
//			jsonGenerator.writeString(acc.getType().name());
//			if (acc instanceof AccessWithFrequency) {
//				if (((AccessWithFrequency) acc).getFrequency() > 1) {
//					jsonGenerator.writeNumber(((AccessWithFrequency) acc).getFrequency());
//				}
//
//			} else { // sequences must all have accesses of type AccessWithFrequency
//				Utils.print("BOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOM\n Access type not expected", Utils.lineno());
//			}
//
//			jsonGenerator.writeEndArray();
//		}
//
//		jsonGenerator.writeEndArray();

		if (sequence.getFrequency() > 1) {
			jsonGenerator.writeNumber(sequence.getFrequency());
		}

		jsonGenerator.writeEndArray();
	}
}