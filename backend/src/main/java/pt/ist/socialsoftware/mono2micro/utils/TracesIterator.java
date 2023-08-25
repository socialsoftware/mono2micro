package pt.ist.socialsoftware.mono2micro.utils;

import java.util.Iterator;
import java.util.List;

import org.json.JSONException;

import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;

public abstract class TracesIterator {
    
    public abstract void getFunctionalityWithName(String functionalityName) throws JSONException;
    public abstract Iterator<String> getFunctionalitiesNames();

    public abstract List<TraceDto> getTracesByType(Constants.TraceType traceType) throws JSONException;
    public abstract TraceDto getLongestTrace() throws JSONException;
    public abstract TraceDto getTraceWithMoreDifferentAccesses() throws JSONException;
    public abstract List<TraceDto> getAllTraces() throws JSONException;

}
