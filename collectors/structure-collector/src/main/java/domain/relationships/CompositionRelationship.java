package domain.relationships;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import domain.Type;
import serializers.CompositionRelationshipSerializer;
import serializers.DomainEntitySerializer;
import spoon.reflect.declaration.CtField;

/**
 * Represents a composition relationship between to object types through a field
 */
@JsonSerialize(using = CompositionRelationshipSerializer.class)
public class CompositionRelationship extends AbstractRelationship {

    public enum ContainerType {
        NONE, LIST, SET, COLLECTION;

        public static ContainerType parseString(String candidate) {
            switch (candidate) {
                case "List": return LIST;
                case "Set": return SET;
                case "Queue": return COLLECTION;
                default: return NONE;
            }
        }
    }

    private String attributeName;

    private ContainerType containerType;

    public CompositionRelationship(String relatedType, String attributeName) {
        this(relatedType, attributeName, ContainerType.NONE);
    }

    public CompositionRelationship(String relatedType, String attributeName, ContainerType containerType) {
        super(relatedType);
        this.attributeName = attributeName;
        this.containerType = containerType;
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public ContainerType getContainerType() {
        return this.containerType;
    }
}
