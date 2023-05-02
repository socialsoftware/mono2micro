package pt.ist.socialsoftware.mono2micro.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;

/**
 * FunctionalityGraphTracesIterator
 */
public class FunctionalityGraphTracesIterator {
    private final JSONObject _codebaseAsJSON;
    private Map<String, TraceGraph> _traceGraphs;

    public FunctionalityGraphTracesIterator(InputStream file) throws IOException, JSONException {
        _codebaseAsJSON = new JSONObject(new String(IOUtils.toByteArray(file)));
        file.close();

        // create graph representation
        _traceGraphs = new HashMap<String, TraceGraph>();

        Iterator<String> functionalities = getFunctionalitiesNames();
        while (functionalities.hasNext()) {
            String functionality = functionalities.next();

            _traceGraphs.put(functionality, getFunctionalityTraceGraph(getFunctionalityWithName(functionality)));
        }
    }

    public Iterator<String> getFunctionalitiesNames() {
        return _codebaseAsJSON.keys();
    }

    public JSONObject getFunctionalityWithName(String functionalityName) throws JSONException {
        return _codebaseAsJSON.getJSONObject(functionalityName);
    }

    public TraceDto getLongestTrace() throws JSONException {
        return null;
    }

    public TraceDto getTraceWithMoreDifferentAccesses() throws JSONException {
        return null;
    }

    public TraceDto getMostProbableTrace() throws JSONException {
        return null;
    }

    public List<TraceDto> getAllTraces() throws JSONException {
        return null;
    }

    public List<TraceDto> getTracesByType(Constants.TraceType traceType) throws JSONException {
        return null;
    }

    class TraceGraphNode {
        Map<Access, Float> nextAccessProbabilities;
        boolean visited;

        public Map<Access, Float> getNextAccessProbabilities() {
            return nextAccessProbabilities;
        }

        public boolean getVisited() {
            return visited;
        }

        public void setVisited(boolean visited) {
            this.visited = visited;
        }
    }

    class Access extends TraceGraphNode {
        String mode;
        int entityAccessedId;

        public String getMode() {
            return mode;
        }

        public int getEntityAccessedId() {
            return entityAccessedId;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public void setEntityAccessedId(int entityAccessedId) {
            this.entityAccessedId = entityAccessedId;
        }

    }

    class Label extends TraceGraphNode {
        String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    class If extends TraceGraphNode {
        List<TraceGraphNode> condition;
        List<TraceGraphNode> thenBody;
        List<TraceGraphNode> elseBody;

        public List<TraceGraphNode> getCondition() {
            return condition;
        }

        public List<TraceGraphNode> getThenBody() {
            return thenBody;
        }

        public List<TraceGraphNode> getElseBody() {
            return elseBody;
        }

        public void setCondition(List<TraceGraphNode> condition) {
            this.condition = condition;
        }

        public void setThenBody(List<TraceGraphNode> thenBody) {
            this.thenBody = thenBody;
        }

        public void setElseBody(List<TraceGraphNode> elseBody) {
            this.elseBody = elseBody;
        }
    }

    class Loop extends TraceGraphNode {
        List<TraceGraphNode> expression;
        List<TraceGraphNode> body;

        public List<TraceGraphNode> getExpression() {
            return expression;
        }

        public List<TraceGraphNode> getBody() {
            return body;
        }

        public void setExpression(List<TraceGraphNode> expression) {
            this.expression = expression;
        }

        public void setBody(List<TraceGraphNode> body) {
            this.body = body;
        }
    }

    class Call extends TraceGraphNode {
        List<TraceGraphNode> body;

        public List<TraceGraphNode> getBody() {
            return body;
        }

        public void setBody(List<TraceGraphNode> body) {
            this.body = body;
        }
    }

    class TraceGraph {
        Access firstAccess;
        List<Access> allAccesses;

        public Access getFirstAccess() {
            return firstAccess;
        }

        public List<Access> getAllAccesses() {
            return allAccesses;
        }

        public void setFirstAccess(Access firstAccess) {
            this.firstAccess = firstAccess;
        }

        public void setAllAccesses(List<Access> allAccesses) {
            this.allAccesses = allAccesses;
        }
    }

    TraceGraph getFunctionalityTraceGraph(JSONObject object) throws JSONException {
        JSONObject mainTrace = object.getJSONArray("t").getJSONObject(0);

        List<TraceGraphNode> preProcessedTraces = translateSubTrace(object, mainTrace);


        return null;
    }

    List<TraceGraphNode> translateSubTrace(JSONObject totalTrace, JSONObject subTrace) throws JSONException {
        JSONArray totalTraceArray = totalTrace.getJSONArray("t");
        JSONArray subTraceArray = subTrace.getJSONArray("a");

        List<TraceGraphNode> translatedTrace = new ArrayList<>(); 

        for (int i = 0; i < subTraceArray.length(); i++) {
            JSONArray traceElementJSON = subTraceArray.getJSONArray(i);
            Object elementType = traceElementJSON.get(0);

            String type = (String)elementType;
            if (elementType instanceof String) {
                switch (type.substring(0, 1)) {
                    case "&": // Is a TraceReferenceDto
                        String description = traceElementJSON.getString(0);
                        JSONArray referenceElements = totalTraceArray.getJSONObject(traceElementJSON.getInt(1)).getJSONArray("a");

                        if(description.contains("if")) {
                            If newIf  = new If();
                            
                            JSONArray condition = getRoleInSubTrace("condition", referenceElements);
                            if(condition != null) {
                                newIf.setCondition(translateSubTrace(totalTrace, totalTraceArray.getJSONObject(condition.getInt(1))));
                            }

                            JSONArray thenBody = getRoleInSubTrace("then", referenceElements);
                            if(thenBody != null) {
                                newIf.setThenBody(translateSubTrace(totalTrace, totalTraceArray.getJSONObject(thenBody.getInt(1))));
                            }

                            JSONArray elseBody = getRoleInSubTrace("else", referenceElements);
                            if(elseBody != null) {
                                newIf.setElseBody(translateSubTrace(totalTrace, totalTraceArray.getJSONObject(elseBody.getInt(1))));
                            }

                            translatedTrace.add(newIf);

                        } else if(description.contains("loop")) {
                            Loop newLoop  = new Loop();
                            
                            JSONArray expression = getRoleInSubTrace("expression", referenceElements);
                            if(expression != null) {
                                newLoop.setExpression(translateSubTrace(totalTrace, totalTraceArray.getJSONObject(expression.getInt(1))));
                            }

                            JSONArray body = getRoleInSubTrace("body", referenceElements);
                            if(body != null) {
                                newLoop.setBody(translateSubTrace(totalTrace, totalTraceArray.getJSONObject(body.getInt(1))));
                            }

                            translatedTrace.add(newLoop);

                        } else if(description.contains("call")) {
                            Call newCall  = new Call();

                            newCall.setBody(translateSubTrace(totalTrace, totalTraceArray.getJSONObject(traceElementJSON.getInt(1))));

                            translatedTrace.add(newCall);

                        }
                        
                        break;
                    case "#": // Is a TraceLabelDto

                        Label label = new Label();
                        label.setContent(traceElementJSON.getString(0));
                        translatedTrace.add(label);

                        break;
                    default: // Is an AccessDto

                        Access access = new Access();
                        access.setMode(traceElementJSON.getString(0));
                        access.setEntityAccessedId(traceElementJSON.getInt(1));
                        translatedTrace.add(access);

                        break;
                }
            }
        }
        
        return translatedTrace;
    }

    JSONArray getRoleInSubTrace(String role, JSONArray traceArray) {
        for (int i = 0; i < traceArray.length(); i++) {
            try {
                if(traceArray.getJSONArray(i).getString(0).contains(role)) {
                    return traceArray.getJSONArray(i);
                }                
            } catch (JSONException e) {
                // continue
            }
        }
        return null;
    }
    
}