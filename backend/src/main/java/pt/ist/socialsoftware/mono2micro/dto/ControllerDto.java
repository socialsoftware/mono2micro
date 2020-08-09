package pt.ist.socialsoftware.mono2micro.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.ControllerDtoDeserializer;

import java.util.List;

@JsonDeserialize(using = ControllerDtoDeserializer.class)
public class ControllerDto {
    private List<AccessDto> controllerAccesses;

    public ControllerDto() {}

    public List<AccessDto> getControllerAccesses() {
        return controllerAccesses;
    }

    public void setControllerAccesses(List<AccessDto> controllerAccesses) {
        this.controllerAccesses = controllerAccesses;
    }
}
