package pt.ist.socialsoftware.mono2micro.decomposition.dto.accessesSciPyDtos;

public class RuleDto extends ReducedTraceElementDto {
	private int count;

	public RuleDto() {}

	public int getCount() { return count; }

	public void setCount(final int count) { this.count = count; }

	@Override
	public boolean equals(final Object other) {
		if (other instanceof RuleDto) {
            RuleDto that = (RuleDto) other;
			return count == that.count && occurrences == that.occurrences;
		}

		return false;
	}
	
	@Override
	public String toString() {
        if (occurrences < 2)
            return "[" + count + "]";

        return "[" + count + ',' + occurrences + ']';
    }
}