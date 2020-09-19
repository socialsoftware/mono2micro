package domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import requitur.ReducedTraceElement;
import requitur.content.RuleContent;
import serializers.CompactedTraceSerializer;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@JsonSerialize(using = CompactedTraceSerializer.class)
public class CompactedTrace extends Trace {
	private List<ReducedTraceElement> elements;
	private int uncompressedSize;

	public CompactedTrace(int id, int frequency) {
		this.frequency = frequency;
		this.id = id;
		this.elements = new ArrayList<>();
	}

	public CompactedTrace(
		int id,
		int frequency,
		List<ReducedTraceElement> elements
	) {
		this.frequency = frequency;
		this.id = id;
		this.elements = elements;
	}

	public List<ReducedTraceElement> getElements() { return elements; }

	public void setElements(List<ReducedTraceElement> elements) { this.elements = elements; }

	public int getUncompressedSize() {
		int counter = 0;
		LinkedList<Pair<Integer, ReducedTraceElement>> ll = new LinkedList<>();

		int elementsListSize = elements.size();

		for (int i = 0; i < elementsListSize; i++) {
			while (ll.size() != 0) {
				Pair<Integer, ReducedTraceElement> lastPair = ll.getLast();

				if (lastPair.getFirst() + ((RuleContent) lastPair.getSecond().getValue()).getCount() < i) {
					ll.removeLast();
				}

				else {
					break;

				}
			}

			if (elements.get(i).getValue() instanceof RuleContent)
				ll.add(new Pair<>(i, elements.get(i)));

			else {
				int multiplier = 1;

				for (Pair<Integer, ReducedTraceElement> pair : ll)
					multiplier *= pair.getSecond().getOccurrences();

				counter += elements.get(i).getOccurrences() * multiplier;
			}
		}

		return counter;
	}

	@Override
	public boolean equals(Object other) {
//        Utils.print("TRACE X: " + this, Utils.lineno());
//        Utils.print("TRACE Y: " + other, Utils.lineno());
		if (other instanceof CompactedTrace) {
			CompactedTrace that = (CompactedTrace) other;
			boolean isEqual = false;

			if (elements.size() == that.elements.size())
				isEqual = elements.equals(that.elements);

			// Utils.print(isEqual + "", Utils.lineno());
			return isEqual;
		}

		return false;
	}

	@Override
	public String toString() {
		return "<Trace id="
			.concat(String.valueOf(id))
			.concat(" f=")
			.concat("" + frequency)
			.concat(" a=")
			.concat("" + elements)
			.concat(">");
	}
}
