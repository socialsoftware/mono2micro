package pt.ist.socialsoftware.mono2micro.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.AccessWithFrequencyDtoDeserializer;

@JsonDeserialize(using = AccessWithFrequencyDtoDeserializer.class)
public class AccessWithFrequencyDto extends AccessDto {
	int frequency;

	public AccessWithFrequencyDto() {}

	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
}
