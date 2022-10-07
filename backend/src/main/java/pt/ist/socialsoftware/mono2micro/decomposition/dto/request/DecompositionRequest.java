package pt.ist.socialsoftware.mono2micro.decomposition.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccAndRepoSciPyStrategy.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.RepositorySciPyStrategy.REPOSITORY_SCIPY;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SciPyRequestDto.class, name = ACCESSES_SCIPY),
        @JsonSubTypes.Type(value = SciPyRequestDto.class, name = REPOSITORY_SCIPY),
        @JsonSubTypes.Type(value = SciPyRequestDto.class, name = ACC_AND_REPO_SCIPY),
})
public abstract class DecompositionRequest {
    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
