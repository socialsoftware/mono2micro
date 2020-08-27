package domain;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import serializers.SequenceSerializer;

import java.util.ArrayList;
import java.util.List;
import utils.*;

@JsonSerialize(using = SequenceSerializer.class)
public class Sequence<T extends Access> {
	int frequency;
	private List<T> accesses;

	@JsonCreator
	public Sequence(
		@JsonProperty("accs") List<T> accesses,
		@JsonProperty("f") int frequency)
	{
		this.frequency = frequency;
		this.accesses = accesses;
	}

	public Sequence() {
		this.frequency = 1;
		this.accesses = new ArrayList<>();
	}

	@JsonProperty("accs")
	public List<T> getAccesses() { return this.accesses; }

	public void setAccesses(List<T> accesses) { this.accesses = accesses; }

	public void addSingleAccess(T access) {
		this.accesses.add(access);
	}

	public void addMultipleAccesses(List<T> accesses) {
		this.accesses.addAll(accesses);
	}

	public void reset() {
		this.frequency = 1;
		this.accesses.clear();
	}

	@JsonProperty("f")
	public int getFrequency() { return this.frequency; }

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	public void increaseFrequency() {
		this.frequency++;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			Utils.print("this sequence == other sequence", Utils.lineno());
			return true;
		}

		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		Sequence that = (Sequence) other;
		boolean isEqual = accesses.equals(that.accesses);
//        Utils.print("EQUAL sequences? " + isEqual, Utils.lineno());
//        Utils.print("TRACE X: " + this, Utils.lineno());
//        Utils.print("TRACE Y: " + other, Utils.lineno());

		return isEqual;
	}

	@Override
	public String toString() {
		return "<Sequence"
			.concat(" frequency=")
			.concat("" + frequency)
			.concat(" accesses=")
			.concat("" + accesses)
			.concat(">");
	}
}
