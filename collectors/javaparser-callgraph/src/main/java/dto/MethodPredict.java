package dto;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class MethodPredict {

    public String name;
    public List<NamePrediction> namePredictions;
    public List<AttentionPath> attentionPaths;
    public List<Float> code_vector;

    public MethodPredict() {}

    public MethodPredict(
        String name,
        List<NamePrediction> namePredictions,
        List<AttentionPath> attentionPaths,
        List<Float> code_vector
    ) {
        this.name = name;
        this.namePredictions = namePredictions;
        this.attentionPaths = attentionPaths;
        this.code_vector = code_vector;
    }

    public MethodPredict(JSONObject json) {
        this.name = json.getString("name");
        this.namePredictions = new LinkedList<>();
        this.attentionPaths = new LinkedList<>();
        this.code_vector = new LinkedList<>();

        JSONArray namePredictionsJson = json.getJSONArray("namePredictions");
        for (int i = 0; i < namePredictionsJson.length(); i++) {
            this.namePredictions.add(new NamePrediction(namePredictionsJson.getJSONObject(i)));
        }

        JSONArray attentionPathsJson = json.getJSONArray("attentionPaths");
        for (int i = 0; i < attentionPathsJson.length(); i++) {
            this.attentionPaths.add(new AttentionPath(attentionPathsJson.getJSONObject(i)));
        }

        JSONArray codeVectorJson = json.getJSONArray("code_vector");
        for (int i = 0; i < codeVectorJson.length(); i++) {
            this.code_vector.add(codeVectorJson.getFloat(i));
        }

    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<NamePrediction> getNamePredictions() { return namePredictions; }
    public void setNamePredictions(List<NamePrediction> namePredictions) { this.namePredictions = namePredictions; }

    public List<AttentionPath> getAttentionPaths() { return attentionPaths; }
    public void setAttentionPaths(List<AttentionPath> attentionPaths) { this.attentionPaths = attentionPaths; }

    public List<Float> getCodeVector() { return code_vector; }
    public void setCodeVector(List<Float> code_vector) { this.code_vector = code_vector; }

    @Override
    public String toString() {
        return "[" + name + ',' + code_vector.toString() + ']';
    }

}
