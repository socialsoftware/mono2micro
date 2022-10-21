package pt.ist.socialsoftware.mono2micro.comparisonTool.domain.results;

import java.util.ArrayList;
import java.util.List;

public class MoJoFMResults extends Results {
    public static final String DEFAULT_COMPARISON_TOOL_DTO = "MOJO_RESULTS";
    private int truePositive;
    private int trueNegative;
    private int falsePositive;
    private int falseNegative;
    private List<String[]> falsePairs = new ArrayList<>();
    private float accuracy;
    private float precision;
    private float recall;
    private float specificity;
    private float fmeasure;
    private double mojoCommon;
    private double mojoBiggest;
    private double mojoNew;
    private double mojoSingletons;

    @Override
    public String getType() {
        return DEFAULT_COMPARISON_TOOL_DTO;
    }

    public float getPrecision() {
        return precision;
    }

    public float getSpecificity() {
        return specificity;
    }

    public void setSpecificity(float specificity) {
        this.specificity = specificity;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public List<String[]> getFalsePairs() {
        return falsePairs;
    }

    public void setFalsePairs(List<String[]> falsePairs) {
        this.falsePairs = falsePairs;
    }

    public void addFalsePair(String[] falsePair) {
        this.falsePairs.add(falsePair);
    }

    public int getFalseNegative() {
        return falseNegative;
    }

    public void setFalseNegative(int falseNegative) {
        this.falseNegative = falseNegative;
    }

    public int getFalsePositive() {
        return falsePositive;
    }

    public void setFalsePositive(int falsePositive) {
        this.falsePositive = falsePositive;
    }

    public int getTrueNegative() {
        return trueNegative;
    }

    public void setTrueNegative(int trueNegative) {
        this.trueNegative = trueNegative;
    }

    public int getTruePositive() {
        return truePositive;
    }

    public void setTruePositive(int truePositive) {
        this.truePositive = truePositive;
    }

    public void setPrecision(float precision) {
        this.precision = precision;
    }

    public float getRecall() {
        return recall;
    }

    public void setRecall(float recall) {
        this.recall = recall;
    }

    public float getFmeasure() {
        return fmeasure;
    }

    public void setFmeasure(float fmeasure) {
        this.fmeasure = fmeasure;
    }

    public double getMojoCommon() {
        return mojoCommon;
    }

    public void setMojoCommon(double mojoValue) {
        this.mojoCommon = mojoValue;
    }

    public double getMojoBiggest() {
        return mojoBiggest;
    }

    public void setMojoBiggest(double mojoBiggest) {
        this.mojoBiggest = mojoBiggest;
    }

    public double getMojoNew() {
        return mojoNew;
    }

    public void setMojoNew(double mojoNew) {
        this.mojoNew = mojoNew;
    }

    public double getMojoSingletons() {
        return mojoSingletons;
    }

    public void setMojoSingletons(double mojoSingletons) {
        this.mojoSingletons = mojoSingletons;
    }
}
