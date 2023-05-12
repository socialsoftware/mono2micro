package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.DomainEntity;
import domain.relationships.CompositionRelationship;

import java.io.IOException;

public class CompositionRelationshipSerializer extends StdSerializer<CompositionRelationship> {

    public CompositionRelationshipSerializer() {
        this(null);
    }

    protected CompositionRelationshipSerializer(Class<CompositionRelationship> t) {
        super(t);
    }

    @Override
    public void serialize(
            CompositionRelationship compositionRelationship,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("attribute_name", compositionRelationship.getAttributeName());
        jsonGenerator.writeStringField("container", compositionRelationship.getContainerType().toString());
        jsonGenerator.writeStringField("related_entity", compositionRelationship.getRelatedType());
        jsonGenerator.writeEndObject();
    }
}
