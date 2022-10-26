package pt.ist.socialsoftware.mono2micro.decomposition.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition.DecompositionType.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SciPyRequestDto.class, name = ACCESSES_DECOMPOSITION),
        @JsonSubTypes.Type(value = SciPyRequestDto.class, name = REPOSITORY_DECOMPOSITION),
        @JsonSubTypes.Type(value = SciPyRequestDto.class, name = ACC_AND_REPO_DECOMPOSITION),
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