package domain;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import serializers.TraceWithAccessesSerializer;
import utils.*;

import java.util.ArrayList;
import java.util.List;

@JsonSerialize(using = TraceWithAccessesSerializer.class)
public class TraceWithAccesses extends Trace {
    private List<Access> accesses;

    public TraceWithAccesses(int id, int frequency) {
        super(id, frequency);
        this.accesses = new ArrayList<>();
    }

    @JsonCreator
    public TraceWithAccesses(
        @JsonProperty("id") int id,
        @JsonProperty("f") int frequency,
        @JsonProperty("accs") List<Access> accesses)
    {
        super(id, frequency);
        this.accesses = accesses;
    }

    @JsonProperty("accs")
    public List<Access> getAccesses() { return this.accesses; }

    public void addSingleAccess(Access access) {
        this.accesses.add(access);
    }

    public void addMultipleAccesses(List<Access> accesses) {
        this.accesses.addAll(accesses);
    }

    public void setAccesses(List<AccessWithFrequency> accesses) {
        this.accesses.clear();
        this.accesses.addAll(accesses);
    }

    public boolean isSubsequence(TraceWithAccesses that) {
        if (this.getAccesses().size() == 0)
            return true;

        if (that.getAccesses().size() == 0)
            return false;

        int counter = 0;

        int thisTraceAccessesAmount = this.getAccesses().size();
        int thatTraceAccessesAmount = that.getAccesses().size();

        TraceWithAccesses longestTrace;
        TraceWithAccesses smallestTrace;

        if (thisTraceAccessesAmount >= thatTraceAccessesAmount) {
            longestTrace = this;
            smallestTrace = that;
        } else {
            longestTrace = that;
            smallestTrace = this;
        }

        List<Access> compactedLongestTraceAccesses = Utils.compactConsecutiveEqualAccesses(new ArrayList<>(longestTrace.getAccesses()));
        List<Access> compactedSmallestTraceAccesses = Utils.compactConsecutiveEqualAccesses(new ArrayList<>(smallestTrace.getAccesses()));

        for (int i = 0; i < compactedLongestTraceAccesses.size(); i++) {
            if (compactedLongestTraceAccesses.get(i).equals(compactedSmallestTraceAccesses.get(counter))) {
                counter++;

            } else {
                if (counter > 0) {
                    int newIndex = checkLoopSequence(
                        compactedLongestTraceAccesses,
                        counter,
                        i
                    ); // (trace, numberOfElementsThatMightHaveASequenceInsideLoop, lowerLimit)

                    if (newIndex == -1) {
                        counter = 0;

                    } else {
                        if (newIndex == compactedLongestTraceAccesses.size() && longestTrace == this) {
                            return true;
                        }

                        i = newIndex - 1;

//                        Utils.print("otherTraceAccesses size: " + otherTraceAccesses.size(), Utils.lineno());
//                        Utils.print("otherTrace.getAccesses() size: " + otherTrace.getAccesses().size(), Utils.lineno());
                    }
                }
            }

            if (i == compactedLongestTraceAccesses.size() - 1 && longestTrace == this) {
                return true;
            }

            if (counter == compactedSmallestTraceAccesses.size()) {
//                Utils.print("Subsequence found with last index: " + i, Utils.lineno());
                return true;
            }

            if (compactedLongestTraceAccesses.size() - i < compactedSmallestTraceAccesses.size() - counter) {
                return false;
            }
        }

        return false;
    }

    // if a loop is discovered, then it returns the index of the last element of the last sequence found (upperLimit)
    // else returns -1 aka no loop
    public int checkLoopSequence(
        List<Access> otherTraceAccesses,
        int numberOfElementsThatMightHaveASequenceInsideLoop,
        int lowerLimit
    ) {
//        Utils.print("Lower limit: " + lowerLimit, Utils.lineno());
//        Utils.print("numberOfElementsThatMightHaveASequenceInsideLoop: " + numberOfElementsThatMightHaveASequenceInsideLoop, Utils.lineno());
//        Utils.print("Goal: " + goal, Utils.lineno());

        int leftIndex = lowerLimit - 1;
        int rightIndex = lowerLimit;

        while(
            leftIndex >= 0 &&
                leftIndex >= (lowerLimit - numberOfElementsThatMightHaveASequenceInsideLoop) &&
                rightIndex < otherTraceAccesses.size()
        ) {
            int i = leftIndex;

            while (i < lowerLimit) {
                if (otherTraceAccesses.get(i).equals(otherTraceAccesses.get(rightIndex))) {
                    i++;
                    rightIndex++;

                } else {
                    leftIndex--; // expand
                    rightIndex = lowerLimit; // reset
                    break;
                }

                if (rightIndex == otherTraceAccesses.size()) {
                    if (i != lowerLimit) {
                        return -1;
                    } else {
                        return rightIndex;
                    }

                } else {
                    if (i == lowerLimit) {
                        return rightIndex;
                    }
                }
            }
        }

        return -1;
    }

    @Override
    public boolean equals(Object other) {
//        Utils.print("TRACE X: " + this, Utils.lineno());
//        Utils.print("TRACE Y: " + other, Utils.lineno());

        if (other == null || getClass() != other.getClass()) {
//            Utils.print("Different classes", Utils.lineno());
            return false;
        }

//        Utils.print("EQUAL traces?", Utils.lineno());
        boolean isEqual = false;

        TraceWithAccesses that = (TraceWithAccesses) other;
        if (this.getAccesses().size() == that.getAccesses().size())
            isEqual = accesses.equals(that.accesses);

//        Utils.print(isEqual + "", Utils.lineno());
        return isEqual;
    }

    @Override
    public String toString() {
        return "<Trace id="
                .concat(String.valueOf(id))
                .concat(" frequency=")
                .concat("" + frequency)
                .concat(" accesses=")
                .concat("" + accesses)
                .concat(">");
    }
}