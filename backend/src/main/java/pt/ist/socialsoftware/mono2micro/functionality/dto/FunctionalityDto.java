package pt.ist.socialsoftware.mono2micro.functionality.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pt.ist.socialsoftware.mono2micro.functionality.deserializer.FunctionalityDtoDeserializer;

import java.util.List;

@JsonDeserialize(using = FunctionalityDtoDeserializer.class)
public class FunctionalityDto {
    private List<AccessDto> functionalityAccesses;

    public FunctionalityDto() {}

    public List<AccessDto> getFunctionalityAccesses() {
        return functionalityAccesses;
    }

    public void setFunctionalityAccesses(List<AccessDto> functionalityAccesses) {
        this.functionalityAccesses = functionalityAccesses;
    }
}
