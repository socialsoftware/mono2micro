package pt.ist.socialsoftware.mono2micro.utils;

import java.util.List;
import java.util.Map;

import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Access;

public class EntityAccessData {
    Map<String, Float> e1e2PairCount;
    Map<Short, List<Pair<String, Byte>>> entityFunctionalitiesAccesses;
    Map<String, List<Float>> entityFunctionalitiesProbabilities;
    Map<Access, Float> nextValidAccesses;

    EntityAccessData(Map<String, Float> e1e2PairCount, Map<Short, List<Pair<String, Byte>>> entityFunctionalitiesAccesses, Map<String, List<Float>> entityFunctionalitiesProbabilities, Map<Access, Float> nextValidAccesses) {
        this.e1e2PairCount = e1e2PairCount;
        this.entityFunctionalitiesAccesses = entityFunctionalitiesAccesses;
        this.entityFunctionalitiesProbabilities = entityFunctionalitiesProbabilities;
        this.nextValidAccesses = nextValidAccesses;
    }

    public Map<String, Float> getE1e2PairCount() {
        return e1e2PairCount;
    }

    public Map<Short, List<Pair<String, Byte>>> getEntityFunctionalitiesAccesses() {
        return entityFunctionalitiesAccesses;
    }

    public Map<String, List<Float>> getEntityFunctionalitiesProbabilities() {
        return entityFunctionalitiesProbabilities;
    }

    public Map<Access, Float> getNextValidAccesses() {
        return nextValidAccesses;
    }
}
