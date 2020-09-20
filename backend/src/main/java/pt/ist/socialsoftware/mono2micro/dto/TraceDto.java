package pt.ist.socialsoftware.mono2micro.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class TraceDto {
	protected int id;
	protected int frequency;
	private List<ReducedTraceElementDto> elements; // either Rules or Accesses

	public TraceDto() {}

	@JsonCreator
	public TraceDto(
		@JsonProperty("id") int id,
		@JsonProperty("f") int frequency,
		@JsonProperty("a") List<ReducedTraceElementDto> elements
	) {
		this.id = id;
		this.frequency = frequency;
		this.elements = elements;
	}

	@JsonProperty("id")
	public int getId() { return this.id; }
	public void setId(int id) { this.id = id; }

	@JsonProperty("f")
	public int getFrequency() { return this.frequency; }
	public void setFrequency(int frequency) { this.frequency = frequency; }

	@JsonProperty("a")
	public List<ReducedTraceElementDto> getElements() { return elements; }
	public void setElements(List<ReducedTraceElementDto> elements) { this.elements = elements; }

	@JsonIgnore
	public int getUncompressedSize() {
		int counter = 0;
		LinkedList<Pair<Integer, RuleDto>> ll = new LinkedList<>();

		int elementsListSize = elements.size();

		for (int i = 0; i < elementsListSize; i++) {
			while (ll.size() != 0) {
				Pair<Integer, RuleDto> lastPair = ll.getLast();

				if (lastPair.getFirst() + lastPair.getSecond().getCount() < i) {
					ll.removeLast();
				}

				else {
					break;

				}
			}

			if (elements.get(i) instanceof RuleDto)
				ll.add(new Pair<>(i, (RuleDto) elements.get(i)));

			else {
				int multiplier = 1;

				for (Pair<Integer, RuleDto> pair : ll)
					multiplier *= pair.getSecond().getOccurrences();

				counter += elements.get(i).getOccurrences() * multiplier;
			}
		}

		return counter;
	}

	@JsonIgnore
	public List<AccessDto> getAccesses() {
		List<AccessDto> accesses = new ArrayList<>();

		if (elements != null) {
			elements.forEach(e -> {
				if (e instanceof AccessDto)
					accesses.add((AccessDto) e);
			});
		}

		return accesses;
	}

	@JsonIgnore
	public HashSet<String> getAccessesSet() {
		HashSet<String> accessesSet = new HashSet<>();

		if (elements != null) {
			elements.forEach(e -> {
				if (e instanceof AccessDto) {
					AccessDto a = (AccessDto) e;

					String accessString = String.join("-", a.getEntity(), a.getMode());
					accessesSet.add(accessString);
				}
			});
		}

		return accessesSet;
	}
}
