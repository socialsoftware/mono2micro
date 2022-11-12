package dto;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class NamePrediction {

    private List<String> name;
    private Float probability;

    public NamePrediction() {}

    public NamePrediction(List<String> name, Float probability) {
        this.name = name;
        this.probability = probability;
    }
    
    public NamePrediction(JSONObject jsonObject) {
        this.name = new LinkedList<>();

        JSONArray nameJson = jsonObject.getJSONArray("name");
        for (int i = 0; i < nameJson.length(); i++) {
            this.name.add(nameJson.getString(i));
        }

        this.probability = jsonObject.getFloat("probability");
    }

    public List<String> getName() { return this.name; }
    public void setName(List<String> name) { this.name = name; }

    public void setProbability(Float probability) { this.probability = probability; }
    public Float getProbability() { return this.probability; }

    @Override
    public String toString() {
        return "[" + name.toString() + ',' + probability.toString() + ']';
    }

}
