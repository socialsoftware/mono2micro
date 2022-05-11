package pt.ist.socialsoftware.mono2micro.domain.strategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.HashSet;
import java.util.Set;

import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.RECOMMENDATION_ACCESSES_SCIPY;

public class RecommendAccessesSciPyStrategy extends Strategy {
    // Used in Similarity Generator
    private String profile;
    private int tracesMaxLimit;
    private boolean isCompleted; // true when all the decompositions are calculated

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<Constants.TraceType> traceTypes;

    // Used in Clustering Algorithm
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<String> linkageTypes;

    @JsonIgnore
    private int numberOfEntities;

    private Set<String> combinationsInProduction = new HashSet<>();
    private Set<String> producedCombinations = new HashSet<>();

    @Override
    public String getType() {
        return RECOMMENDATION_ACCESSES_SCIPY;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public int getTracesMaxLimit() {
        return tracesMaxLimit;
    }

    public void setTracesMaxLimit(int tracesMaxLimit) {
        this.tracesMaxLimit = tracesMaxLimit;
    }

    public Set<Constants.TraceType> getTraceTypes() {
        return traceTypes;
    }

    public void setTraceTypes(Set<Constants.TraceType> traceTypes) {
        this.traceTypes = traceTypes;
    }

    public Set<String> getLinkageTypes() {
        return linkageTypes;
    }

    public void setLinkageTypes(Set<String> linkageTypes) {
        this.linkageTypes = linkageTypes;
    }

    public Set<String> getCombinationsInProduction() {
        return combinationsInProduction;
    }

    public void setCombinationsInProduction(Set<String> combinationsInProduction) {
        this.combinationsInProduction = combinationsInProduction;
    }

    public Set<String> getProducedCombinations() {
        return producedCombinations;
    }

    public void setProducedCombinations(Set<String> producedCombinations) {
        this.producedCombinations = producedCombinations;
    }

    public void addCombinationsInProduction() {
        for (Constants.TraceType traceType : traceTypes)
            for (String linkageType : linkageTypes)
                combinationsInProduction.add(traceType + linkageType);
    }

    public boolean containsRequestedCombinations() {
        for (Constants.TraceType traceType : traceTypes)
            for (String linkageType : linkageTypes)
                if (!combinationsInProduction.contains(traceType + linkageType))
                    return false;
        return true;
    }

    public void addProducedCombinations() {
        for (Constants.TraceType traceType : traceTypes)
            for (String linkageType : linkageTypes)
                producedCombinations.add(traceType + linkageType);
    }

    public boolean containsCombination(Constants.TraceType traceType, String linkageType) {
        return producedCombinations.contains(traceType + linkageType);
    }

    public int getNumberOfEntities() {
        return numberOfEntities;
    }

    public void setNumberOfEntities(int numberOfEntities) {
        this.numberOfEntities = numberOfEntities;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RecommendAccessesSciPyStrategy))
            return false;

        RecommendAccessesSciPyStrategy strategy = (RecommendAccessesSciPyStrategy) obj;

        return strategy.getProfile().equals(this.profile) && (strategy.getTracesMaxLimit() == this.tracesMaxLimit);
    }
}
