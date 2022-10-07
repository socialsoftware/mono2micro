package pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm;

import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

public interface AccessesSimilarity {
    String ACCESSES_SIMILARITY = "ACCESSES_SIMILARITY";
    String getName();
    Strategy getStrategy();
    String getProfile();
    Constants.TraceType getTraceType();
    int getTracesMaxLimit();
}
