package pt.ist.socialsoftware.mono2micro.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.SequenceDtoDeserializer;

import java.util.List;

@JsonDeserialize(using = SequenceDtoDeserializer.class)
public class SequenceDto {
	private int frequency;
	private List<AccessWithFrequencyDto> accesses;

	public SequenceDto() {}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public List<AccessWithFrequencyDto> getAccesses() {
		return accesses;
	}

	public void setAccesses(List<AccessWithFrequencyDto> accesses) {
		this.accesses = accesses;
	}
}
