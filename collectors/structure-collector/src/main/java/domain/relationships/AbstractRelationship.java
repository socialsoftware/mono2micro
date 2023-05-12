package domain.relationships;

import domain.Type;

public class AbstractRelationship implements Relationship {

    private String relatedTypeName;

    public AbstractRelationship(String relatedTypeName) {
        this.relatedTypeName = relatedTypeName;
    }

    public String getRelatedType() {
        return this.relatedTypeName;
    }
}
