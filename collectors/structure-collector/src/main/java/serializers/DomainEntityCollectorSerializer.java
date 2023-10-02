package serializers;

import collectors.DomainEntityCollector;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.DomainEntity;

import java.io.IOException;

public class DomainEntityCollectorSerializer extends StdSerializer<DomainEntityCollector> {

    public DomainEntityCollectorSerializer() {
        this(null);
    }

    protected DomainEntityCollectorSerializer(Class<DomainEntityCollector> t) {
        super(t);
    }

    @Override
    public void serialize(DomainEntityCollector collector, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeArrayFieldStart("entities");
        for (DomainEntity domainEntity : collector.getDomainEntities()) {
            jsonGenerator.writeObject(domainEntity);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
