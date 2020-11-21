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
		short entityID,
		Type type,
		int frequency
	) {
		super(entityID, type);
		this.frequency = frequency;
	}

	public AccessWithFrequency(Access a, int frequency) {
		super(a.getEntityID(), a.getType());
		this.frequency = frequency;
	}

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

		boolean isEqual = entityID == that.entityID && type == that.type;
//        Utils.print("EQUAL Accesses w/f", Utils.lineno());
		return isEqual;
	}

	@Override
	public String toString() {
		return "["
			.concat(String.valueOf(entityID))
			.concat(",")
			.concat(type.name())
			.concat(",")
			.concat(String.valueOf(frequency))
			.concat("]");
	}
}
