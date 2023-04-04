package pt.ist.socialsoftware.mono2micro.similarity.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Similarity {
    @Id
    protected String name;
    @DBRef
    private Strategy strategy;
    @DBRef(lazy = true)
    private List<Decomposition> decompositions;

    public Similarity() {}
    public Similarity(Strategy strategy, String name) {
        this.strategy = strategy;
        strategy.addSimilarity(this);
        this.name = name;
        setDecompositions(new ArrayList<>());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getType();

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public List<Decomposition> getDecompositions() {
        return decompositions;
    }

    public Decomposition getDecompositionByName(String decompositionName) {
        return this.decompositions.stream().filter(decomposition -> decomposition.getName().equals(decompositionName)).findFirst().orElse(null);
    }

    public void setDecompositions(List<Decomposition> decompositions) {
        this.decompositions = decompositions;
    }

    public synchronized void addDecomposition(Decomposition decomposition) {
        this.decompositions.add(decomposition);
    }

    public synchronized void removeDecomposition(String decompositionName) {
        this.decompositions = this.decompositions.stream().filter(decomposition -> !decomposition.getName().equals(decompositionName)).collect(Collectors.toList());
    }

    public abstract Clustering getClustering();

    public abstract Map<Short, String> getIDToEntityName() throws Exception;

    public abstract void generate() throws Exception;

    public abstract void removeProperties();

    public abstract boolean equalsDto(SimilarityDto dto);
}
