package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.relationships.InheritanceRelationship;

import java.io.IOException;

public class InheritanceRelationshipSerializer extends StdSerializer<InheritanceRelationship> {

    public InheritanceRelationshipSerializer() {
        this(null);
    }

    protected InheritanceRelationshipSerializer(Class<InheritanceRelationship> t) {
        super(t);
    }

    @Override
    public void serialize(
            InheritanceRelationship inheritanceRelationship,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("related_entity", inheritanceRelationship.getRelatedType());
        jsonGenerator.writeStringField("type", inheritanceRelationship.getInheritanceType().toString());
        jsonGenerator.writeEndObject();
    }
}
