package dto;

import java.util.Objects;
public class AccessDto extends ReducedTraceElementDto {
    private short entityID;
    private String mode;

    public AccessDto() {}

    public short getEntityID() { return entityID; }
    public void setEntityID(short entityID) { this.entityID = entityID; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    @Override
	public boolean equals(final Object other) {
        if (other instanceof AccessDto) {
            AccessDto that = (AccessDto) other;
            return this.entityID == that.entityID && this.mode.equals(that.mode);
        }
        
        return false;
    }

    @Override
    public String toString() {
        if (occurrences < 2)
            return "[" + entityID + ',' + mode + ']';

        return "[" + entityID + ',' + mode + ',' + occurrences + ']';
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityID, mode);
    }
}
