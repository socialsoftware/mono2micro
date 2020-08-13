package pt.ist.socialsoftware.mono2micro.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.AccessWithFrequencyDtoDeserializer;
import pt.ist.socialsoftware.mono2micro.utils.serializers.AccessWithFrequencyDtoSerializer;

@JsonDeserialize(using = AccessWithFrequencyDtoDeserializer.class)
@JsonSerialize(using = AccessWithFrequencyDtoSerializer.class)
public class AccessWithFrequencyDto extends AccessDto {
	int frequency;

	public AccessWithFrequencyDto() {}

	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	@Override
	public String toString() {
		return "[" + this.getEntity() + ',' + this.getMode() + ',' + frequency + ']';
	}
}
