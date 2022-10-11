package pt.ist.socialsoftware.mono2micro.decomposition.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositorySciPyDecomposition.REPOSITORY_SCIPY;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SciPyRequestDto.class, name = ACCESSES_SCIPY),
        @JsonSubTypes.Type(value = SciPyRequestDto.class, name = REPOSITORY_SCIPY),
        @JsonSubTypes.Type(value = SciPyRequestDto.class, name = ACC_AND_REPO_SCIPY),
})
public abstract class DecompositionRequest {
    String decompositionType;
    String similarityName;

    public String getDecompositionType() {
        return decompositionType;
    }

    public void setDecompositionType(String decompositionType) {
        this.decompositionType = decompositionType;
    }

    public String getSimilarityName() {
        return similarityName;
    }

    public void setSimilarityName(String similarityName) {
        this.similarityName = similarityName;
    }
}
