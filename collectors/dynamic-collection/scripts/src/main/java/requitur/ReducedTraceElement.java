package requitur;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import requitur.content.Content;
import serializers.ReducedTraceElementSerializer;

@JsonSerialize(using = ReducedTraceElementSerializer.class)
public class ReducedTraceElement {
	private final Content value;
	private final int occurrences;

	public ReducedTraceElement(final Content value, final int occurences) {
		super();
		this.value = value;
		this.occurrences = occurences;
	}

	public Content getValue() {
		return value;
	}

	public int getOccurrences() {
		return occurrences;
	}

	@Override
	public String toString() {
		return "(" + value + ")x" + occurrences;
	}
}