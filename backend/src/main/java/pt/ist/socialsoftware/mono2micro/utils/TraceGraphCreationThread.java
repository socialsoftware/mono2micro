package pt.ist.socialsoftware.mono2micro.utils;

import java.util.concurrent.Callable;

import pt.ist.socialsoftware.mono2micro.utils.traceGraph.TraceGraph;

public class TraceGraphCreationThread implements Callable<Pair<String, FunctionalityInfo>> {
    private FunctionalityGraphTracesIterator iterator;
    private String functionalityName;

    TraceGraphCreationThread(FunctionalityGraphTracesIterator iterator, String functionalityName) {
        this.iterator = iterator;
        this.functionalityName = functionalityName;
    }

    @Override
    public Pair<String, FunctionalityInfo> call() {
        TraceGraph graph = null;
        FunctionalityInfo functionalityInfo = new FunctionalityInfo();

        try {
            graph = iterator.getFunctionalityTraceGraph(functionalityName);

            functionalityInfo.setLongestPath(FunctionalityGraphTracesIterator.getLongestTrace(graph.getGraph(), functionalityName));
            functionalityInfo.setMostDifferentAccessesPath(FunctionalityGraphTracesIterator.getTraceWithMoreDifferentAccesses(graph.getGraph(), functionalityName));
            functionalityInfo.setMostProbablePath(FunctionalityGraphTracesIterator.getMostProbableTrace(graph.getGraph(), functionalityName));
            //TODO: add all (pairCount and entityFunctionalities)

        } catch (Exception e) {
            System.out.println("JSON error handling functionality graph");
            e.printStackTrace();
        }

        return new Pair<>(this.functionalityName, functionalityInfo);
    }
    
}
