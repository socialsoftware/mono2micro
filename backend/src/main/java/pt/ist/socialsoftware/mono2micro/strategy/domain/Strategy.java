package pt.ist.socialsoftware.mono2micro.strategy.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.strategy.dto.StrategyDto;

import java.util.List;
import java.util.stream.Collectors;

public abstract class Strategy {
    @Id
    private String name;
    @DBRef(lazy = true)
    private Codebase codebase;
    @DBRef(lazy = true)
    private List<Decomposition> decompositions;

    public abstract String getType();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Codebase getCodebase() {
        return codebase;
    }

    public void setCodebase(Codebase codebase) {
        this.codebase = codebase;
    }

    public List<Decomposition> getDecompositions() {
        return decompositions;
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

    @Override
    public abstract boolean equals(Object obj);

    public abstract boolean equalsDto(StrategyDto dto);
}