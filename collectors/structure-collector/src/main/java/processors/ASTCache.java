package processors;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import domain.DomainEntity;
import serializers.DomainEntitySerializer;
import serializers.StructureSerializer;

import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = StructureSerializer.class)
public class ASTCache {

    private Map<String, DomainEntity> domainEntities;

    public ASTCache() {
        this.domainEntities = new HashMap<>();
    }

    public Map<String, DomainEntity> getDomainEntities() {
        return this.domainEntities;
    }

    public DomainEntity getDomainEntity(String domainEntityName) {
        return this.domainEntities.get(domainEntityName);
    }

    public void addDomainEntity(DomainEntity domainEntity) {
        this.domainEntities.put(domainEntity.getQualifiedName(), domainEntity);
    }

    public boolean hasDomainEntity(String domainEntityName) {
        return this.domainEntities.containsKey(domainEntityName);
    }
}
