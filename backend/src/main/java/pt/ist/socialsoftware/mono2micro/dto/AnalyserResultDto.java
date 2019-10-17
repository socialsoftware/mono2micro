package pt.ist.socialsoftware.mono2micro.dto;

public class AnalyserResultDto {
    private float accessWeight;
    private float writeWeight;
    private float readWeight;
    private float sequenceWeight;
    private float numberClusters;
    private int maxClusterSize;
    private float cohesion;
    private float coupling;
    private float complexity;
    private float accuracy;
    private float precision;
    private float recall;
    private float specificity;
    private float fmeasure;

    public float getAccessWeight() {
        return accessWeight;
    }

    public float getCoupling() {
        return coupling;
    }

    public void setCoupling(float coupling) {
        this.coupling = coupling;
    }

    public float getCohesion() {
        return cohesion;
    }

    public void setCohesion(float cohesion) {
        this.cohesion = cohesion;
    }

    public int getMaxClusterSize() {
        return maxClusterSize;
    }

    public void setMaxClusterSize(int maxClusterSize) {
        this.maxClusterSize = maxClusterSize;
    }

    public float getComplexity() {
        return complexity;
    }

    public void setComplexity(float complexity) {
        this.complexity = complexity;
    }

    public float getFmeasure() {
        return fmeasure;
    }

    public void setFmeasure(float fmeasure) {
        this.fmeasure = fmeasure;
    }

    public float getSpecificity() {
        return specificity;
    }

    public void setSpecificity(float specificity) {
        this.specificity = specificity;
    }

    public float getRecall() {
        return recall;
    }

    public void setRecall(float recall) {
        this.recall = recall;
    }

    public float getPrecision() {
        return precision;
    }

    public void setPrecision(float precision) {
        this.precision = precision;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getNumberClusters() {
        return numberClusters;
    }

    public void setNumberClusters(float numberClusters) {
        this.numberClusters = numberClusters;
    }

    public float getSequenceWeight() {
        return sequenceWeight;
    }

    public void setSequenceWeight(float sequenceWeight) {
        this.sequenceWeight = sequenceWeight;
    }

    public float getReadWeight() {
        return readWeight;
    }

    public void setReadWeight(float readWeight) {
        this.readWeight = readWeight;
    }

    public float getWriteWeight() {
        return writeWeight;
    }

    public void setWriteWeight(float writeWeight) {
        this.writeWeight = writeWeight;
    }

    public void setAccessWeight(float accessWeight) {
        this.accessWeight = accessWeight;
    }
}