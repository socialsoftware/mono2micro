package pt.ist.socialsoftware.mono2micro.recommendation.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.dendrogram.dto.DendrogramDto;

public abstract class Recommendation {
    @DBRef(lazy = true)
    private Codebase codebase;

    public Codebase getCodebase() {
        return codebase;
    }

    public void setCodebase(Codebase codebase) {
        this.codebase = codebase;
    }

    public abstract String getType();

    public abstract boolean equalsDto(DendrogramDto dto);
}
