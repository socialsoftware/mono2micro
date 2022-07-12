package pt.ist.socialsoftware.mono2micro.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.accessesSciPyDtos.AnalyserResultDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.accessesSciPyDtos.CutInfoDto;

import java.io.IOException;

public class CutInfoDtoSerializer extends StdSerializer<CutInfoDto> {
    public CutInfoDtoSerializer() {
        this(null);
    }

    public CutInfoDtoSerializer(Class<CutInfoDto> t) { super(t); }

    @Override
    public void serialize(
        CutInfoDto value,
        JsonGenerator gen,
        SerializerProvider provider
    )
        throws IOException
    {
        AnalyserResultDto analyserResultDto = value.getAnalyserResultDto();
        gen.writeStartObject();

        gen.writeObjectField("accessWeight", analyserResultDto.getAccessWeight());
        gen.writeObjectField("writeWeight", analyserResultDto.getWriteWeight());
        gen.writeObjectField("readWeight", analyserResultDto.getReadWeight());
        gen.writeObjectField("sequenceWeight", analyserResultDto.getSequenceWeight());
        gen.writeObjectField("numberClusters", analyserResultDto.getNumberClusters());
        gen.writeObjectField("maxClusterSize", analyserResultDto.getMaxClusterSize());
        gen.writeObjectField("cohesion", analyserResultDto.getCohesion());
        gen.writeObjectField("coupling", analyserResultDto.getCoupling());
        gen.writeObjectField("complexity", analyserResultDto.getComplexity());
        gen.writeObjectField("performance", analyserResultDto.getPerformance());
        gen.writeObjectField("accuracy", analyserResultDto.getAccuracy());
        gen.writeObjectField("precision", analyserResultDto.getPrecision());
        gen.writeObjectField("recall", analyserResultDto.getRecall());
        gen.writeObjectField("specificity", analyserResultDto.getSpecificity());
        gen.writeObjectField("fmeasure", analyserResultDto.getFmeasure());
        gen.writeObjectField("mojoCommon", analyserResultDto.getMojoCommon());
        gen.writeObjectField("mojoBiggest", analyserResultDto.getMojoBiggest());
        gen.writeObjectField("mojoNew", analyserResultDto.getMojoNew());
        gen.writeObjectField("mojoSingletons", analyserResultDto.getMojoSingletons());
        gen.writeObjectField("functionalitySpecs", value.getFunctionalitySpecs());

        gen.writeEndObject();
    }
}
