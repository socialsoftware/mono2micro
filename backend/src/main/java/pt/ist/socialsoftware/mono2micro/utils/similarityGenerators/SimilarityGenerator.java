package pt.ist.socialsoftware.mono2micro.utils.similarityGenerators;

public interface SimilarityGenerator {

    // Pass arguments in the specific implementation's constructor

    // Needed to initialize and calculate similarity matrix distances
    void buildMatrix() throws Exception;

    // Writes the similarity matrix via the codebase manager
    void writeSimilarityMatrix() throws Exception;
}