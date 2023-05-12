package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.DomainEntity;
import domain.relationships.CompositionRelationship;
import domain.relationships.InheritanceRelationship;
import domain.relationships.Relationship;

import java.io.IOException;
import java.util.stream.Collectors;

public class DomainEntitySerializer extends StdSerializer<DomainEntity> {

    public DomainEntitySerializer() {
        this(null);
    }

    protected DomainEntitySerializer(Class<DomainEntity> t) {
        super(t);
    }

    @Override
    public void serialize(
            DomainEntity domainEntity,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("name", domainEntity.getSimpleName());

        jsonGenerator.writeObjectFieldStart("relationships");
        jsonGenerator.writeObjectField("composition", domainEntity.getRelationships().stream()
                .filter(relationship -> relationship instanceof CompositionRelationship)
                .collect(Collectors.toList()));

        jsonGenerator.writeObjectField("inheritance", domainEntity.getRelationships().stream()
                .filter(relationship -> relationship instanceof InheritanceRelationship)
                .collect(Collectors.toList()));
        jsonGenerator.writeEndObject();

        jsonGenerator.writeEndObject();
    }
}
