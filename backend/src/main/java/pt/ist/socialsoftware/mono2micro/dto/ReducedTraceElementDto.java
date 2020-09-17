package pt.ist.socialsoftware.mono2micro.dto;

public abstract class ReducedTraceElementDto {
    protected int occurrences;

    public int getOccurrences() { return occurrences; }
    public void setOccurrences(int occurrences) { this.occurrences = occurrences; }
}
