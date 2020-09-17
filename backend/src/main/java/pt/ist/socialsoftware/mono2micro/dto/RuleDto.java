package pt.ist.socialsoftware.mono2micro.dto;

public class Rule extends ReducedTraceElementDto {
	private int count;

	public Rule() {}

	public int getCount() { return count; }

	public void setCount(final int count) { this.count = count; }

	@Override
	public boolean equals(final Object other) {
		if (other instanceof Rule) {
            Rule that = (Rule) other;
			return count == that.counter && occurrences == that.occurrences;
		}

		return false;
	}
	
	@Override
	public String toString() {
        if (frequency < 2)
            return "[" + count + "]";

        return "[" + count + ',' + frequency + ']'
    }
}