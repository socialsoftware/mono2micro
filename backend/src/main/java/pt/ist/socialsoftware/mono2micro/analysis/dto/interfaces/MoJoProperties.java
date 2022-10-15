package pt.ist.socialsoftware.mono2micro.analysis.dto.interfaces;

public interface MoJoProperties {
    void setSpecificity(float specificity);
    void setAccuracy(float accuracy);
    void addFalsePair(String[] falsePair);
    void setFalseNegative(int falseNegative);
    void setFalsePositive(int falsePositive);
    void setTrueNegative(int trueNegative);
    void setTruePositive(int truePositive);
    void setPrecision(float precision);
    void setRecall(float recall);
    void setFmeasure(float fmeasure);
    void setMojoCommon(double mojoValue);
    void setMojoBiggest(double mojoBiggest);
    void setMojoNew(double mojoNew);
    void setMojoSingletons(double mojoSingletons);
}
