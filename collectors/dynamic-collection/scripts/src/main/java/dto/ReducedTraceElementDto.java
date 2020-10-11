package dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import deserializers.ReducedTraceElementDtoDeserializer;
import serializers.ReducedTraceElementDtoSerializer;

@JsonDeserialize(using = ReducedTraceElementDtoDeserializer.class)
@JsonSerialize(using = ReducedTraceElementDtoSerializer.class)
public abstract class ReducedTraceElementDto {
    protected int occurrences;

    public int getOccurrences() { return occurrences; }
    public void setOccurrences(int occurrences) { this.occurrences = occurrences; }
}
