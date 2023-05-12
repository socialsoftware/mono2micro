package domain.relationships;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import domain.Type;
import serializers.CompositionRelationshipSerializer;
import serializers.InheritanceRelationshipSerializer;

/**
 * Represents an inheritance relationship between to object types through
 * class extension or interface implementation
 */
@JsonSerialize(using = InheritanceRelationshipSerializer.class)
public class InheritanceRelationship extends AbstractRelationship {

    public enum InheritanceType {
        EXTENDS, IMPLEMENTS
    }

    private InheritanceType inheritanceType;

    public InheritanceRelationship(String relatedType, InheritanceType inheritanceType) {
        super(relatedType);
        this.inheritanceType = inheritanceType;
    }

    public InheritanceType getInheritanceType() {
        return inheritanceType;
    }
}
