package pt.ist.socialsoftware.mono2micro.decomposition.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public class ExpertRequest extends DecompositionRequest {
    private String expertName;
    private Optional<MultipartFile> expertFile;

    public ExpertRequest(String expertName, Optional<MultipartFile> expertFile) {
        this.expertName = expertName;
        this.expertFile = expertFile;
    }

    public String getExpertName() {
        return expertName;
    }

    public void setExpertName(String expertName) {
        this.expertName = expertName;
    }

    public Optional<MultipartFile> getExpertFile() {
        return expertFile;
    }

    public void setExpertFile(Optional<MultipartFile> expertFile) {
        this.expertFile = expertFile;
    }
}
