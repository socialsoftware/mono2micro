package pt.ist.socialsoftware.mono2micro.dto;

import pt.ist.socialsoftware.mono2micro.domain.Graph;

public class AnalyserDto {
    private String codebaseName;
    private Graph expert;
    private float accessWeight;
    private float writeWeight;
    private float readWeight;
    private float sequence1Weight;
    private float sequence2Weight;
    private float numberClusters;
    private float accuracy;
    private float precision;
    private float recall;
    private float specificity;
    private float fmeasure;

    public float getPrecision() {
        return precision;
    }

    public Graph getExpert() {
        return expert;
    }

    public void setExpert(Graph expert) {
        this.expert = expert;
    }

    public String getCodebaseName() {
        return codebaseName;
    }

    public void setCodebaseName(String codebaseName) {
        this.codebaseName = codebaseName;
    }

    public float getNumberClusters() {
        return numberClusters;
    }

    public void setNumberClusters(float numberClusters) {
        this.numberClusters = numberClusters;
    }

    public float getSequence2Weight() {
        return sequence2Weight;
    }

    public void setSequence2Weight(float sequence2Weight) {
        this.sequence2Weight = sequence2Weight;
    }

    public float getSequence1Weight() {
        return sequence1Weight;
    }

    public void setSequence1Weight(float sequence1Weight) {
        this.sequence1Weight = sequence1Weight;
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

    public float getAccessWeight() {
        return accessWeight;
    }

    public void setAccessWeight(float accessWeight) {
        this.accessWeight = accessWeight;
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