package pt.ist.socialsoftware.mono2micro.recommendation.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendMatrixClassVectorizationDto;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendationRepository;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.SimilarityMatrixClassVectorization;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@Document("recommendation")
public class RecommendMatrixClassVectorization extends Recommendation {

    public static final String RECOMMEND_MATRIX_CLASS_VECTORIZATION = "RECOMMEND_MATRIX_CLASS_VECTORIZATION";

    private String linkageType;

    private Set<String> similarityMatricesNames;

    public RecommendMatrixClassVectorization() {}

    public RecommendMatrixClassVectorization(RecommendMatrixClassVectorizationDto dto) {
        this.linkageType = dto.getLinkageType();
        this.isCompleted = dto.isCompleted();
        this.similarityMatricesNames = new HashSet<>();
    }

    public String getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }

    public Set<String> getSimilarityMatricesNames() {
        return similarityMatricesNames;
    }

    public void setSimilarityMatricesNames(Set<String> similarityMatricesNames) {
        this.similarityMatricesNames = similarityMatricesNames;
    }

    @Override
    public String getType() {
        return RECOMMEND_MATRIX_CLASS_VECTORIZATION;
    }

    @Override
    public void deleteProperties() {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        // gridFsService.deleteFiles(getSimilarityMatricesNames());
    }

    @Override
    public boolean equalsDto(RecommendationDto dto) {
        if (!(dto instanceof RecommendMatrixClassVectorizationDto))
            return false;

        RecommendMatrixClassVectorizationDto recommendMatrixClassVectorizationDto = (RecommendMatrixClassVectorizationDto) dto;

        return recommendMatrixClassVectorizationDto.getLinkageType().equals(this.linkageType);
    }

    @Override
    public void generateRecommendation(RecommendationRepository recommendationRepository) {
        SciPyClustering clusteringAlgorithm = new SciPyClustering();
        clusteringAlgorithm.prepareAutowire();
        // Executes the request in a fork to avoid blocking the user
        ForkJoinPool.commonPool().submit(() -> {
            try {
                GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
                SimilarityMatrixClassVectorization similarityMatrix = new SimilarityMatrixClassVectorization(getName());
                setSimilarityMatricesNames(similarityMatrix.generateMultipleMatrices(gridFsService, this));
                clusteringAlgorithm.generateMultipleDecompositions(this);

                recommendationRepository.save(this);
                System.out.println("recommendation ended");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void createDecompositions(List<String> decompositionNames) throws Exception {

    }
}
