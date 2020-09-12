package domain;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import serializers.FunctionalitySerializer;

import java.util.List;
import java.util.ArrayList;

@JsonSerialize(using = FunctionalitySerializer.class)
public class Functionality<T extends Trace> {
    private String label;
    private int frequency;
    private List<T> traces;
    private int numberOfSubsequencesFound = 0; // statistics

    public Functionality(String label, int frequency) {
        this.label = label;
        this.frequency = frequency;
        this.traces = new ArrayList<>();
    }

    @JsonCreator
    public Functionality(
        @JsonProperty("f") int frequency,
        @JsonProperty("traces") List<T> traces
    ) {
        this.frequency = frequency;
        this.traces = traces;
    }

    public Functionality(String label) {
        this.label = label;
        this.frequency = 0;
        this.traces = new ArrayList<>();
    }

    public String getLabel() { return this.label; }

    @JsonProperty("f")
    public int getFrequency() { return this.frequency; }

    public List<T> getTraces() { return this.traces; }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void increaseFrequency() {
        this.frequency += 1;
    }

    public int getNumberOfSubsequencesFound() { return this.numberOfSubsequencesFound; }

    public void addTrace(T newTrace) {
//        Utils.print("Adding trace: " + newTrace.getLabel(), Utils.lineno());
        this.increaseFrequency();

        // I think that both of these loops can be merged together into one but it's probably faster to do this way
        // instead of calculating the subsequence between each pair of traces until we discover that actually
        // the trace was already in the list

        for (T savedTrace : traces) {
            if(savedTrace.equals(newTrace)){
//                Utils.print("Trace already exists, raising its frequency...", Utils.lineno());
                savedTrace.increaseFrequency(); // TODO FIXME calculating frequency only makes sense if the sub-sequence calculation isn't applied
                return;
            }
        }

        // FIXME The code below introduces a huge performance overhead making the json generation too slow/unfeasible
        // FIXME In fact this sub-sequence algorithm discovers a lot of them but it's not worth the overhead
        // FIXME Please try to find a better way to not introduce such a delay in the processing
        // FIXME Finding a subsequence/Getting only representatives is a nice to have feature to discard unnecessary traces
//        int currentTraceAccessesAmount = ((TraceWithAccesses) newTrace).getAccesses().size();
//
//        for(int i = traces.size() - 1; i >= 0; i--) {
//            TraceWithAccesses savedTrace = (TraceWithAccesses) traces.get(i);
//
//            if (((TraceWithAccesses) newTrace).isSubsequence(savedTrace)) {
////                Utils.print("New trace " + newTrace.getId() + " is a subsequence of savedTrace " + savedTrace.getId(), Utils.lineno());
//                numberOfSubsequencesFound++;
//                return;
//            }
//
//            if (savedTrace.isSubsequence(((TraceWithAccesses) newTrace))) {
////                Utils.print("Existent trace " + savedTrace.getId() + " is a subsequence of the new trace " + newTrace.getId(), Utils.lineno());
//                traces.remove(i);
//                numberOfSubsequencesFound++;
//            }
//        }

        traces.add(newTrace);
    }

    @Override
    public String toString() {
        return "<Functionality label="
            .concat(label != null ? label : "<no-label>")
            .concat(" frequency=")
            .concat("" + frequency)
            .concat(" traces=")
            .concat("" + traces)
            .concat(">");
    }
}