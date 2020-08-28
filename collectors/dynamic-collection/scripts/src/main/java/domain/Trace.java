package domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Trace {
	protected int id;
	protected int frequency;

	@JsonCreator
	public Trace(
		@JsonProperty("id") int id,
		@JsonProperty("f") int frequency
	) {
		this.id = id;
		this.frequency = frequency;
	}

	@JsonProperty("id")
	public int getId() {
		return this.id;
	}

	@JsonProperty("f")
	public int getFrequency() { return this.frequency; }

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public void increaseFrequency() {
		this.frequency += 1;
	}
}
