package pt.ist.socialsoftware.mono2micro.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.ReducedTraceElementDtoDeserializer;

@JsonDeserialize(using = ReducedTraceElementDtoDeserializer.class)
public abstract class ReducedTraceElementDto {
    protected int occurrences;

    public int getOccurrences() { return occurrences; }
    public void setOccurrences(int occurrences) { this.occurrences = occurrences; }
}
