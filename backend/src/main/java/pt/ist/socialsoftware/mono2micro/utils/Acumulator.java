package pt.ist.socialsoftware.mono2micro.utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class Acumulator {

    private static final int VECTOR_SIZE = 384;

    private ArrayList<Double> sum;
    private float count;

    public Acumulator() {
        sum = new ArrayList<>();
        for (int i = 0; i < VECTOR_SIZE; i++) {
            sum.add(0.0);
        }
        count = 0;
    }

    public Acumulator(ArrayList<Double> sum, float count) {
        this.sum = sum;
        this.count = count;
    }

    public ArrayList<Double> getSum() {
        return sum;
    }

    public void setSum(ArrayList<Double> sum) {
        this.sum = sum;
    }

    public float getCount() {
        return count;
    }

    public void setCount(float count) {
        this.count = count;
    }

    public void addVector(JSONArray vector) throws JSONException {
        for (int i = 0; i < VECTOR_SIZE; i++) {
            sum.set(i, sum.get(i) + vector.getDouble(i));
        }
        count += 1;
    }

    public ArrayList<Double> getMeanVector() {
        ArrayList<Double> vector = new ArrayList<>();
        for (int i = 0; i < VECTOR_SIZE; i++) {
            if (count == 0) vector.add(0.0);
            else vector.add(sum.get(i) / count);
        }
        return vector;
    }
}
