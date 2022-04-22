package pt.ist.socialsoftware.mono2micro.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.FunctionalityDtoDeserializer;

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
