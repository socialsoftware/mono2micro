package collectors;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import domain.DomainEntity;
import serializers.DomainEntityCollectorSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores {@link DomainEntity} data.
 */
@JsonSerialize(using = DomainEntityCollectorSerializer.class)
public class DomainEntityCollector extends AbstractCollector {

    private final List<DomainEntity> domainEntities;

    public DomainEntityCollector() {
        this.domainEntities = new ArrayList<>();
    }

    public List<DomainEntity> getDomainEntities() {
        return Collections.unmodifiableList(domainEntities);
    }

    public void addDomainEntity(DomainEntity domainEntity) {
        this.domainEntities.add(domainEntity);
    }

    public int getDomainEntitiesSize() {
        return domainEntities.size();
    }
}
