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

    private HashMap<String, HashMap<String, Float>> functionalitySpecs;

    public CutInfoDto() {}

    public AnalyserResultDto getAnalyserResultDto() {
        return analyserResultDto;
    }

    public void setAnalyserResultDto(AnalyserResultDto analyserResultDto) {
        this.analyserResultDto = analyserResultDto;
    }

    public HashMap<String, HashMap<String, Float>> getFunctionalitySpecs() {
        return functionalitySpecs;
    }

    public void setFunctionalitySpecs(HashMap<String, HashMap<String, Float>> functionalitySpecs) {
        this.functionalitySpecs = functionalitySpecs;
    }
}
