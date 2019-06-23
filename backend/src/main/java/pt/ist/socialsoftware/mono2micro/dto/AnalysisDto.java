package pt.ist.socialsoftware.mono2micro.dto;

public class AnalysisDto {
    private String dendrogramName1;
    private String graphName1;
    private String dendrogramName2;
    private String graphName2;
    private int truePositive;
    private int trueNegative;
    private int falsePositive;
    private int falseNegative;
    private float precision;
    private float recall;
    private float fmeasure;

    public float getPrecision() {
        return precision;
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

    public String getGraphName2() {
        return graphName2;
    }

    public void setGraphName2(String graphName2) {
        this.graphName2 = graphName2;
    }

    public String getDendrogramName2() {
        return dendrogramName2;
    }

    public void setDendrogramName2(String dendrogramName2) {
        this.dendrogramName2 = dendrogramName2;
    }

    public String getGraphName1() {
        return graphName1;
    }

    public void setGraphName1(String graphName1) {
        this.graphName1 = graphName1;
    }

    public String getDendrogramName1() {
        return dendrogramName1;
    }

    public void setDendrogramName1(String dendrogramName1) {
        this.dendrogramName1 = dendrogramName1;
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
}