package pt.ist.socialsoftware.mono2micro.similarity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.AccessesSciPySimilarity;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;

import java.util.List;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

@Service
public class SimilarityService {
    @Autowired
    SimilarityRepository similarityRepository;

    @Autowired
    AccessesSciPySimilarityService accessesSciPySimilarityService;

    @Autowired
    DecompositionService decompositionService;

    public void deleteSingleSimilarity(String similarityName) {
        Similarity similarity = similarityRepository.findByName(similarityName);
        for (Decomposition decomposition: similarity.getDecompositions())
            decompositionService.deleteDecomposition(decomposition);
        removeSpecificSimilarityProperties(similarity);
        similarityRepository.deleteByName(similarityName);
    }

    public void deleteSimilarity(Similarity similarity) { // Used when strategy is deleted
        removeSpecificSimilarityProperties(similarity);
        similarityRepository.deleteByName(similarity.getName());
    }

    private void removeSpecificSimilarityProperties(Similarity similarity) {
        switch (similarity.getType()) {
            case ACCESSES_SCIPY:
                accessesSciPySimilarityService.deleteSimilarityProperties((AccessesSciPySimilarity) similarity);
                break;
            default:
                throw new RuntimeException("No know type " + similarity.getType() + ".");
        }
    }

    public List<Decomposition> getDecompositions(String similarityName) {
        Similarity similarity = similarityRepository.findByName(similarityName);
        return similarity.getDecompositions();
    }

    public Similarity getSimilarity(String similarityName) {
        return similarityRepository.findByName(similarityName);
    }
}
