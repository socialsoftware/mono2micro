package pt.ist.socialsoftware.mono2micro.dto.decompositionDto;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public class AccessesSciPyRequestDto implements RequestDto {
    private String expertName;
    private float cutValue;
    private String cutType;
    private Optional<MultipartFile> expertFile;

    public AccessesSciPyRequestDto() {}

    public AccessesSciPyRequestDto( String cutType, float cutValue) {
        this.cutType = cutType;
        this.cutValue = cutValue;
    }

    public AccessesSciPyRequestDto(String expertName, Optional<MultipartFile> expertFile) {
        this.expertName = expertName;
        this.expertFile = expertFile;
    }

    public Optional<MultipartFile> getExpertFile() {
        return expertFile;
    }

    public void setExpertFile(Optional<MultipartFile> expertFile) {
        this.expertFile = expertFile;
    }

    public String getExpertName() {
        return expertName;
    }

    public void setExpertName(String expertName) {
        this.expertName = expertName;
    }

    public float getCutValue() {
        return cutValue;
    }

    public void setCutValue(float cutValue) {
        this.cutValue = cutValue;
    }

    public String getCutType() {
        return cutType;
    }

    public void setCutType(String cutType) {
        this.cutType = cutType;
    }
}