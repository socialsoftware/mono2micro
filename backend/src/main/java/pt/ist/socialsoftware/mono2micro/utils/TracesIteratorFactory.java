package pt.ist.socialsoftware.mono2micro.utils;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesGraphRepresentation.ACCESSES_GRAPH;

public class TracesIteratorFactory {
    public static TracesIterator getIterator(String representationType, String representationName, InputStream accessesFile, int tracesMaxLimit) throws JSONException, IOException {
        switch (representationType) {
            case ACCESSES:
                return new FunctionalityTracesIterator(accessesFile, tracesMaxLimit);
        
            case ACCESSES_GRAPH:
                return new FunctionalityGraphTracesIterator(representationName, accessesFile);

            default:
                break;
        }

        return null;
    }
}
