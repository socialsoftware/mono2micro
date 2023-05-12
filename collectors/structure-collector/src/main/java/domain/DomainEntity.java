package domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import domain.relationships.Relationship;
import serializers.DomainEntitySerializer;
import spoon.reflect.declaration.CtClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity that represents a Domain Entity in the codebase.
 */
@JsonSerialize(using = DomainEntitySerializer.class)
public class DomainEntity implements Type {

    private static int idCounter = 0;

    private final int id;

    private List<Relationship> relationships;

    private final CtClass<?> ctDomainEntity;

    public DomainEntity(CtClass<?> ctDomainEntity) {
        this.id = idCounter++;
        this.ctDomainEntity = ctDomainEntity;

        this.relationships = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getSimpleName() {
        return ctDomainEntity.getSimpleName();
    }

    public String getQualifiedName() {
        return ctDomainEntity.getQualifiedName();
    }

    public CtClass<?> getCtDomainEntity() {
        return ctDomainEntity;
    }

    public List<Relationship> getRelationships() {
        return this.relationships;
    }

    public void addRelationship(Relationship relationship) {
        this.relationships.add(relationship);
    }
}
