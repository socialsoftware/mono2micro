package domain;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import serializers.TraceWithSequencesSerializer;
import utils.*;
import java.util.List;

@JsonSerialize(using = TraceWithSequencesSerializer.class)
public class TraceWithSequences extends Trace {
	private List<Sequence<AccessWithFrequency>> sequences;

	public TraceWithSequences(
		TraceWithAccesses t,
		List<Sequence<AccessWithFrequency>> sequences
	) {
		this.id = t.getId();
		this.frequency = t.getFrequency();
		this.sequences = sequences;
	}

	@JsonCreator
	public TraceWithSequences(
		@JsonProperty("id") int id,
		@JsonProperty("f") int frequency,
		@JsonProperty("seqs") List<Sequence<AccessWithFrequency>> sequences)
	{
		this.id = id;
		this.frequency = frequency;
		this.sequences = sequences;
	}

	@JsonProperty("seqs")
	public List<Sequence<AccessWithFrequency>> getSequences() { return this.sequences; }

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			Utils.print("this trace w/seq == other trace w/seq", Utils.lineno());
			return true;
		}

		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		TraceWithSequences that = (TraceWithSequences) other;
		boolean isEqual = sequences.equals(that.sequences);
//        Utils.print("EQUAL traces w/seq ? " + isEqual, Utils.lineno());
//        Utils.print("TRACE w/seq X: " + this, Utils.lineno());
//        Utils.print("TRACE w/seq Y: " + other, Utils.lineno());

		return isEqual;
	}

	@Override
	public String toString() {
		return "<TraceWithSequences id="
			.concat(String.valueOf(id))
			.concat(" frequency=")
			.concat("" + frequency)
			.concat(" sequences=")
			.concat("" + sequences)
			.concat(">");
	}
}
