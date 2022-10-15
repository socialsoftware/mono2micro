package pt.ist.socialsoftware.mono2micro.analysis.dto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

public class AnalysisDtoFactory {
    public static AnalysisDto getAnalysisDto(Decomposition decomposition1, Decomposition decomposition2) {
        switch (decomposition1.getType() + decomposition2.getType()) { // Compare two decompositions based on their type
            default:
                return new DefaultAnalysisDto();
        }
    }
}
