package pt.ist.socialsoftware.mono2micro.functionality.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

import java.util.*;

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

		if (elements == null) return counter;

		LinkedList<Pair<Integer, RuleDto>> ll = new LinkedList<>();

		int elementsListSize = elements.size();

		for (int i = 0; i < elementsListSize; i++) {
			while (!ll.isEmpty()) {
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

	private List<ReducedTraceElementDto> expand(
		List<ReducedTraceElementDto> elements,
		int from,
		int to,
		int maxOccurrences
	) {
		List<ReducedTraceElementDto> accesses = new ArrayList<>();

		if (elements == null || elements.isEmpty()) return accesses;

		int i = from;

		while (i < to) {
			ReducedTraceElementDto element = elements.get(i);

			List<ReducedTraceElementDto> expandedElements = new ArrayList<>();

			if (element instanceof RuleDto) {
				RuleDto r = (RuleDto) element;

				expandedElements.addAll(
					expand(
						elements,
						i + 1,
						i + 1 + r.getCount(),
						maxOccurrences
					)
				);

				i += 1 + r.getCount();

			} else {
				AccessDto a = (AccessDto) element;

				AccessDto newAccess = new AccessDto();
				newAccess.setOccurrences(0);
				newAccess.setEntityID(a.getEntityID());
				newAccess.setMode(a.getMode());

				expandedElements.add(newAccess);
				i++;
			}

			int max = Math.min(element.getOccurrences(), maxOccurrences);

			for (int j = 0; j < max; j++)
				accesses.addAll(expandedElements);
		}

		return accesses;
	}

	@JsonIgnore
	public List<AccessDto> expand(int maxOccurrences) {
		return (List<AccessDto>) ((List<?>) this.expand(
			elements,
			0,
			elements == null ? 0 : elements.size(),
			maxOccurrences)
		);
	}

	@JsonIgnore
	public List<AccessDto> getAccesses() { // no decompression
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
	public Set<String> getAccessesSet() {
		Set<String> accessesSet = new HashSet<>();

		if (elements != null) {
			elements.forEach(e -> {
				if (e instanceof AccessDto) {
					AccessDto a = (AccessDto) e;

					String accessString = String.join(
						"-",
						String.valueOf(a.getEntityID()),
						String.valueOf(a.getMode())
					);

					accessesSet.add(accessString);
				}
			});
		}

		return accessesSet;
	}
}
