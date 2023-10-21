package pt.ist.socialsoftware.mono2micro.utils;

import java.util.concurrent.Callable;

import pt.ist.socialsoftware.mono2micro.utils.traceGraph.TraceGraph;

public class TraceGraphCreationThread implements Callable {
    private FunctionalityGraphTracesIterator iterator;
    private String functionalityName;

    TraceGraphCreationThread(FunctionalityGraphTracesIterator iterator, String functionalityName) {
        this.iterator = iterator;
        this.functionalityName = functionalityName;
    }

    @Override
    public Pair<String, TraceGraph> call() {
        TraceGraph graph = null;

        try {
            graph = iterator.getFunctionalityTraceGraph(functionalityName);
        } catch (Exception e) {
            System.out.println("JSON error handling functionality graph");
            e.printStackTrace();
        }

        return new Pair<>(this.functionalityName, graph);
    }
    
}
