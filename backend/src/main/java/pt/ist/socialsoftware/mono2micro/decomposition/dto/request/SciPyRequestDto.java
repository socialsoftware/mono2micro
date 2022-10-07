package pt.ist.socialsoftware.mono2micro.decomposition.dto.request;

public class SciPyRequestDto extends DecompositionRequest {
    private String similarityName;
    private String cutType;
    private float cutValue;

    public SciPyRequestDto() {}

    public SciPyRequestDto(String similarityName, String cutType, float cutValue) {
        this.similarityName = similarityName;
        this.cutType = cutType;
        this.cutValue = cutValue;
    }

    public String getSimilarityName() {
        return similarityName;
    }

    public void setSimilarityName(String similarityName) {
        this.similarityName = similarityName;
    }

    public String getCutType() {
        return cutType;
    }

    public void setCutType(String cutType) {
        this.cutType = cutType;
    }

    public float getCutValue() {
        return cutValue;
    }

    public void setCutValue(float cutValue) {
        this.cutValue = cutValue;
    }
}
