package pt.ist.socialsoftware.mono2micro.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class TraceDto {
	protected int id;
	protected int frequency;
	private List<SequenceDto> sequences;

	public TraceDto() {}

	@JsonCreator
	public TraceDto(
		@JsonProperty("id") int id,
		@JsonProperty("f") int frequency,
		@JsonProperty("seqs") List<SequenceDto> sequences
	) {
		this.id = id;
		this.frequency = frequency;
		this.sequences = sequences;
	}

	public int getId() { return this.id; }
	public void setId(int id) { this.id = id; }

	@JsonProperty("f")
	public int getFrequency() { return this.frequency; }
	public void setFrequency(int frequency) { this.frequency = frequency; }

	@JsonProperty("seqs")
	public List<SequenceDto> getSequences() { return sequences; }
	public void setSequences(List<SequenceDto> sequences) { this.sequences = sequences; }

	public List<AccessWithFrequencyDto> getAccesses() {
		List<AccessWithFrequencyDto> accesses = new ArrayList<>();
		sequences.forEach(seq -> accesses.addAll(seq.getAccesses()));

		return accesses;
	}
}
