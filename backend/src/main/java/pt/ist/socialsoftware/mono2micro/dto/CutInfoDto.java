package pt.ist.socialsoftware.mono2micro.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.CutInfoDtoDeserializer;
import pt.ist.socialsoftware.mono2micro.utils.serializers.CutInfoDtoSerializer;

import java.util.HashMap;

@JsonSerialize(using = CutInfoDtoSerializer.class)
@JsonDeserialize(using = CutInfoDtoDeserializer.class)
public class CutInfoDto {

    private AnalyserResultDto analyserResultDto;

    private HashMap<String, Float> controllerComplexities;

    public CutInfoDto() {}

    public AnalyserResultDto getAnalyserResultDto() {
        return analyserResultDto;
    }

    public void setAnalyserResultDto(AnalyserResultDto analyserResultDto) {
        this.analyserResultDto = analyserResultDto;
    }

    public HashMap<String, Float> getControllerComplexities() {
        return controllerComplexities;
    }

    public void setControllerComplexities(HashMap<String, Float> controllerComplexities) {
        this.controllerComplexities = controllerComplexities;
    }
}
