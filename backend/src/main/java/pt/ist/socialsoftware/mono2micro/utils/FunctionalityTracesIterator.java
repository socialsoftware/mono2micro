package pt.ist.socialsoftware.mono2micro.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.RuleDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FunctionalityTracesIterator {
    private int limit; // 0 means no limit aka all traces will be parsed
    private int tracesCounter; // #traces
    private final JSONObject codebaseAsJSON;
    private JSONObject requestedFunctionality;

    public FunctionalityTracesIterator(
            InputStream file,
            int limit
    ) throws IOException, JSONException {
        this.limit = limit;
        codebaseAsJSON = new JSONObject(new String(IOUtils.toByteArray(file)));
        file.close();
    }

    public Iterator<String> getFunctionalitiesNames() {
        return codebaseAsJSON.keys();
    }

    public void getFunctionalityWithName(String functionalityName) throws JSONException {
        requestedFunctionality = codebaseAsJSON.getJSONObject(functionalityName);
    }

    public TraceDto getLongestTrace() throws JSONException {
        JSONArray tracesArray = requestedFunctionality.getJSONArray("t");
        TraceDto t1 = nextTrace(tracesArray.getJSONObject(0));
        int t1UncompressedSize = t1.getUncompressedSize();
        for (int i = 1; i < tracesArray.length() && (limit == 0 || tracesCounter < limit); i++) {
            TraceDto t2 = nextTrace(tracesArray.getJSONObject(i));

            int t2UncompressedSize = t2.getUncompressedSize();

            if (t2UncompressedSize > t1UncompressedSize) {
                t1 = t2;
                t1UncompressedSize = t2UncompressedSize;
            }
        }
        return t1;
    }

    public TraceDto getTraceWithMoreDifferentAccesses() throws JSONException {
        JSONArray tracesArray = requestedFunctionality.getJSONArray("t");
        TraceDto t1 = nextTrace(tracesArray.getJSONObject(0));
        int t1AccessesSetSize = t1.getAccessesSet().size();

        for (int i = 1; i < tracesArray.length() && (limit == 0 || tracesCounter < limit); i++) {
            TraceDto t2 = nextTrace(tracesArray.getJSONObject(i));

            int t2AccessesSetSize = t2.getAccessesSet().size();

            if (t2AccessesSetSize > t1AccessesSetSize) {
                t1 = t2;
                t1AccessesSetSize = t2AccessesSetSize;
            }
        }

        return t1;
    }

    public List<TraceDto> getAllTraces() throws JSONException {
        List<TraceDto> traceDtos = new ArrayList<>();
        JSONArray tracesArray = requestedFunctionality.getJSONArray("t");
        for (int i = 0; i < tracesArray.length() && (limit == 0 || tracesCounter < limit); i++)
            traceDtos.add(nextTrace(tracesArray.getJSONObject(i)));
        return traceDtos;
    }

    public List<TraceDto> getTracesByType(Constants.TraceType traceType) throws JSONException {
        List<TraceDto> traceDtos = new ArrayList<>();

        // Get traces according to trace type
        switch(traceType) {
            case LONGEST:
                traceDtos.add(this.getLongestTrace());
                break;
            case WITH_MORE_DIFFERENT_ACCESSES:
                traceDtos.add(this.getTraceWithMoreDifferentAccesses());
                break;
            default:
                traceDtos.addAll(this.getAllTraces());
        }
        if (traceDtos.isEmpty())
            throw new RuntimeException("Functionality does not contain any trace.");

        return traceDtos;
    }

    private TraceDto nextTrace(JSONObject traceJSON) throws JSONException {
        List<ReducedTraceElementDto> elements = new ArrayList<>();
        JSONArray accessesJSON = traceJSON.getJSONArray("a");

        for (int i = 0; i < accessesJSON.length(); i++) {
            JSONArray reducedTraceElementJSON = accessesJSON.getJSONArray(i);
            Object elementType = reducedTraceElementJSON.get(0);

            if (elementType instanceof String) { // Is an AccessDto
                AccessDto accessDto = new AccessDto();
                accessDto.setMode((byte) (reducedTraceElementJSON.getString(0).equals("R") ? 1 : 2));
                accessDto.setEntityID((short) reducedTraceElementJSON.getInt(1));
                if (reducedTraceElementJSON.length() == 3)
                    accessDto.setOccurrences(reducedTraceElementJSON.getInt(2));
                else accessDto.setOccurrences(1);
                elements.add(accessDto);
            }
            else { // Is a RuleDto
                RuleDto ruleDto = new RuleDto();
                ruleDto.setCount(reducedTraceElementJSON.getInt(0));
                if (reducedTraceElementJSON.length() == 2)
                    ruleDto.setOccurrences(reducedTraceElementJSON.getInt(1));
                else ruleDto.setOccurrences(1);
                elements.add(ruleDto);
            }
        }

        if (!elements.isEmpty())
            tracesCounter++;

        return new TraceDto(traceJSON.getInt("id"), traceJSON.has("f")? traceJSON.getInt("f") : 1, elements);
    }
}