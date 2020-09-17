package domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import requitur.ReducedTraceElement;
import serializers.CompactedTraceSerializer;

import java.util.ArrayList;
import java.util.List;

@JsonSerialize(using = CompactedTraceSerializer.class)
public class CompactedTrace extends Trace {
	private List<ReducedTraceElement> elements;

	public CompactedTrace(int id, int frequency) {
		this.frequency = frequency;
		this.id = id;
		this.elements = new ArrayList<>();
	}

	public CompactedTrace(int id, int frequency, List<ReducedTraceElement> elements) {
		this.frequency = frequency;
		this.id = id;
		this.elements = elements;
	}

	public List<ReducedTraceElement> getElements() { return elements; }

	public void setElements(List<ReducedTraceElement> elements) { this.elements = elements; }
}
