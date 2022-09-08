package pt.ist.socialsoftware.mono2micro.recommendation.domain;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendAccessesSciPyDto;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.util.HashSet;
import java.util.Set;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

@Document("recommendation")
public class RecommendAccessesSciPy extends Recommendation {
    private String profile;
    private int tracesMaxLimit;
    private boolean isCompleted; // true when all the decompositions are calculated

    private Set<Constants.TraceType> traceTypes;

    private Set<String> linkageTypes;

    @Transient
    private int numberOfEntities;

    private Set<String> combinationsInProduction;
    private Set<String> producedCombinations;
    private Set<String> similarityMatricesNames;
    private String recommendationResultName;

    public RecommendAccessesSciPy() {}

    public RecommendAccessesSciPy(RecommendAccessesSciPyDto dto) {
        this.profile = dto.getProfile();
        this.tracesMaxLimit = dto.getTracesMaxLimit();
        this.isCompleted = dto.isCompleted();
        this.traceTypes = dto.getTraceTypes();
        this.linkageTypes = dto.getLinkageTypes();
        this.combinationsInProduction = new HashSet<>();
        this.producedCombinations = new HashSet<>();
        this.similarityMatricesNames = new HashSet<>();
    }

    @Override
    public String getType() {
        return ACCESSES_SCIPY;
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

    public Set<String> getSimilarityMatricesNames() {
        return similarityMatricesNames;
    }

    public void setSimilarityMatricesNames(Set<String> similarityMatricesNames) {
        this.similarityMatricesNames = similarityMatricesNames;
    }

    public void addSimilarityMatrixName(String similarityMatrixName) {
        this.similarityMatricesNames.add(similarityMatrixName);
    }

    public String getRecommendationResultName() {
        return recommendationResultName;
    }

    public void setRecommendationResultName(String recommendationResultName) {
        this.recommendationResultName = recommendationResultName;
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
    public boolean equalsDto(RecommendationDto dto) {
        if (!(dto instanceof RecommendAccessesSciPyDto))
            return false;

        RecommendAccessesSciPyDto recommendAccessesSciPyDto = (RecommendAccessesSciPyDto) dto;

        return recommendAccessesSciPyDto.getProfile().equals(this.profile) && (recommendAccessesSciPyDto.getTracesMaxLimit() == this.tracesMaxLimit);
    }
}