package domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import deserializers.AccessWithFrequencyDeserializer;
import serializers.AccessWithFrequencySerializer;
import utils.*;

@JsonSerialize(using = AccessWithFrequencySerializer.class)
@JsonDeserialize(using = AccessWithFrequencyDeserializer.class)
public class AccessWithFrequency extends Access {
	private int frequency;

	@JsonCreator
	public AccessWithFrequency(
		@JsonProperty("entity") String entity,
		@JsonProperty("type") Type type,
		@JsonProperty("f") int frequency
	) {
		super(entity, type);
		this.frequency = frequency;
	}

	public AccessWithFrequency(Access a, int frequency) {
		super(a.getEntity(), a.getType());
		this.frequency = frequency;
	}

	@JsonProperty("f")
	public int getFrequency() {
		return this.frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public void increaseFrequency() {
		this.frequency++;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			Utils.print("this Access w/f == other Access w/f", Utils.lineno());
			return true;
		}

		if (other == null || getClass() != other.getClass()) {
			System.out.println("Different classes");
			return false;
		}

		AccessWithFrequency that = (AccessWithFrequency) other;

//        Utils.print("Access w/f X: " + this, Utils.lineno());
//        Utils.print("Access w/f Y: " + other, Utils.lineno());

		boolean isEqual = entity.equals(that.entity) && type == that.type;
//        Utils.print("EQUAL Accesses w/f", Utils.lineno());
		return isEqual;
	}

	@Override
	public String toString() {
		return "<AccessWithFrequency entity="
			.concat(entity)
			.concat(" type=")
			.concat(type.name())
			.concat(" frequency=")
			.concat("" + frequency)
			.concat(">");
	}
}
