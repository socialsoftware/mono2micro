package pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm;

public interface Dendrogram {
    String getName();
    String getLinkageType();
    String getSimilarityMatrixName();
    String getDendrogramName();
    void setDendrogramName(String dendrogramName);
    String getCopheneticDistanceName();
    void setCopheneticDistanceName(String copheneticDistanceName);
}
