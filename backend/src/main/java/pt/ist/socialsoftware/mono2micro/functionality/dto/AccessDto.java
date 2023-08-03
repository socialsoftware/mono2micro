package pt.ist.socialsoftware.mono2micro.functionality.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import pt.ist.socialsoftware.mono2micro.functionality.deserializer.AccessDtoDeserializer;
import pt.ist.socialsoftware.mono2micro.functionality.serializer.AccessDtoSerializer;

import java.util.Objects;

@JsonDeserialize(using = AccessDtoDeserializer.class)
@JsonSerialize(using = AccessDtoSerializer.class)
public class AccessDto extends ReducedTraceElementDto {

    public static final byte READ_MODE = 1;
    public static final byte UPDATE_MODE = 2;
    public static final byte CREATE_MODE = 4;
    public static final byte DELETE_MODE = 8;
    public static final byte READ_UPDATE_MODE = READ_MODE + UPDATE_MODE;
    public static final byte READ_CREATE_MODE = READ_MODE + CREATE_MODE;
    public static final byte READ_DELETE_MODE = READ_MODE + DELETE_MODE;

    private short entityID;
    private byte mode; // "R" -> 1, "W" -> 2

    public AccessDto() {}

    public short getEntityID() { return entityID; }
    public void setEntityID(short entityID) { this.entityID = entityID; }

    public byte getMode() { return mode; }
    public void setMode(byte mode) { this.mode = mode; }

    public static boolean isReadMode(byte mode) {
        return mode == READ_MODE;
    }

    public static boolean isWriteMode(byte mode) {
        return mode % 2 == 0;
    }

    public static boolean containsReadMode(byte mode) {
        return mode % 2 != 0;
    }

    public static boolean containsWriteMode(byte mode) {
        return mode >= UPDATE_MODE;
    }

    public static byte getMergedMode(byte mode1, byte mode2) {
        if (mode1 == mode2) {
            return mode1;
        } else if (mode1 == READ_MODE || mode2 == READ_MODE) {
            return (byte) (mode1 | mode2);
        } else if (mode1 == UPDATE_MODE || mode2 == UPDATE_MODE) {
            return (byte) Integer.max(mode1, mode2);
        } else if (mode1 == CREATE_MODE || mode2 == CREATE_MODE) {
            if (mode1 == READ_UPDATE_MODE || mode2 == READ_UPDATE_MODE) {
                return READ_CREATE_MODE;
            } else {
                return (byte) Integer.max(mode1, mode2);
            }
        } else if (mode1 == DELETE_MODE || mode2 == DELETE_MODE) {
            return READ_DELETE_MODE;
        } else {
            return (byte) Integer.max(mode1, mode2);
        }
    }

    public static byte fromStringMode(String mode) {
        switch (mode) {
            case "U":
                return UPDATE_MODE;
            case "C":
                return CREATE_MODE;
            case "D":
                return DELETE_MODE;
            default:
                return READ_MODE;
        }
    }

    @Override
	public boolean equals(final Object other) {
        if (other instanceof AccessDto) {
            AccessDto that = (AccessDto) other;
            return this.entityID == that.entityID && this.mode == that.mode;
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
