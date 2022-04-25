package pt.ist.socialsoftware.mono2micro.utils;

import java.util.ArrayList;

public class Acumulator {

    private ArrayList<Double> sum;
    private float count;

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
}
