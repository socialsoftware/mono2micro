package domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Trace {
	protected int id;
	protected int frequency;

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
