package pt.ist.socialsoftware.mono2micro.similarity.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.List;
import java.util.stream.Collectors;

public abstract class Similarity {
    @Id
    private String name;
    private String decompositionType;
    @DBRef
    private Strategy strategy;
    @DBRef(lazy = true)
    private List<Decomposition> decompositions;
    public abstract List<String> getImplementations();

    public boolean containsImplementation(String implementation) {
        return getImplementations().contains(implementation);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getType();

    public String getDecompositionType() {
        return decompositionType;
    }

    public void setDecompositionType(String decompositionType) {
        this.decompositionType = decompositionType;
    }

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

    public abstract boolean equalsDto(SimilarityDto dto);
}
