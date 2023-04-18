package pt.ist.socialsoftware.mono2micro.decomposition.dto.representationInfoDto;

public abstract class RepresentationInfoDto {
    protected String type;

    public RepresentationInfoDto(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
