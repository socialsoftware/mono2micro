package pt.ist.socialsoftware.mono2micro.functionality.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import pt.ist.socialsoftware.mono2micro.functionality.deserializer.AccessDtoDeserializer;
import pt.ist.socialsoftware.mono2micro.functionality.serializer.AccessDtoSerializer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@JsonDeserialize(using = AccessDtoDeserializer.class)
@JsonSerialize(using = AccessDtoSerializer.class)
public class AccessDto extends ReducedTraceElementDto {
    private static AtomicLong ID_COUNTER = new AtomicLong(0);

    private long id;
    private short entityID;
    private byte mode; // "R" -> 1, "W" -> 2
    private float probability = 1.0f;

    public AccessDto() {
        id = ID_COUNTER.getAndIncrement();
        entityID = -1;
    }

    public short getEntityID() { return entityID; }
    public void setEntityID(short entityID) { this.entityID = entityID; }

    public byte getMode() { return mode; }
    public void setMode(byte mode) { this.mode = mode; }

    public float getProbability() { return probability; }
    public void setProbability(float probability) { this.probability = probability; }

    public long getId() {
        return id;
    }

    @Override
	public boolean equals(final Object other) {
        if (other instanceof AccessDto) {
            AccessDto that = (AccessDto) other;
            return this.entityID == that.entityID && this.mode == that.mode && this.id == that.id;
        }
        
        return false;
    }

    @Override
    public String toString() {
        if (occurrences < 2)
            return "[" + entityID + ',' + mode + ",p=" + probability + ",id=" + id + ']';

        return "[" + entityID + ',' + mode + ',' + occurrences + ",p=" + probability + ",id=" + id + ']';
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityID, mode);
    }
}
