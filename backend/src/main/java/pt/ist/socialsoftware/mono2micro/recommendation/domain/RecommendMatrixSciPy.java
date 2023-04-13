package pt.ist.socialsoftware.mono2micro.recommendation.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.SciPyRequestDto;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendationRepository;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.*;
import pt.ist.socialsoftware.mono2micro.similarity.domain.dendrogram.Dendrogram;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityScipyAccessesAndRepositoryDto;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.WeightsFactory;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendationsType.*;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

@Document("recommendation")
public class RecommendMatrixSciPy extends Recommendation {

    public String type;
    private String profile;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;
    private String linkageType;
    private List<Weights> weightsList;
    private Set<String> similarityMatricesNames;

    public RecommendMatrixSciPy() {}

    public RecommendMatrixSciPy(RecommendMatrixSciPyDto dto) {
        this.type = dto.getType();
        this.profile = dto.getProfile();
        this.tracesMaxLimit = dto.getTracesMaxLimit();
        this.traceType = dto.getTraceType();
        this.linkageType = dto.getLinkageType();
        this.isCompleted = dto.isCompleted();
        this.weightsList = dto.getWeightsList();
        this.similarityMatricesNames = new HashSet<>();
    }

    public String getType() {
        return this.type;
    }

    public boolean requiresClusterTypeConversion() {
        switch (getType()) {
            case RECOMMEND_MATRIX_CLASS_VECTORIZATION:
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH:
            case RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES:
                return true;
            default:
                return false;
        }
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public int getTracesMaxLimit() {
        return tracesMaxLimit;
    }

    public void setTracesMaxLimit(int tracesMaxLimit) {
        this.tracesMaxLimit = tracesMaxLimit;
    }

    public Constants.TraceType getTraceType() {
        return traceType;
    }

    public void setTraceType(Constants.TraceType traceType) {
        this.traceType = traceType;
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

    public List<Weights> getWeightsList() {
        return weightsList;
    }

    public void setWeightsList(List<Weights> weightsList) {
        this.weightsList = weightsList;
    }

    public List<String> getWeightsNames() {
        List<String> weightsNames = new ArrayList<>();
        for (Weights weights : getWeightsList())
            weightsNames.addAll(weights.getWeightsNames());
        return weightsNames;
    }

    @Override
    public void deleteProperties() {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        gridFsService.deleteFiles(getSimilarityMatricesNames());
    }

    @Override
    public boolean equalsDto(RecommendationDto dto) {
        if (!(dto instanceof RecommendMatrixSciPyDto))
            return false;

        RecommendMatrixSciPyDto recommendMatrixSciPyDto = (RecommendMatrixSciPyDto) dto;

        return recommendMatrixSciPyDto.getProfile().equals(this.profile) &&
                (recommendMatrixSciPyDto.getLinkageType().equals(this.linkageType)) &&
                (recommendMatrixSciPyDto.getTraceType() == this.traceType) &&
                (recommendMatrixSciPyDto.getTracesMaxLimit() == this.tracesMaxLimit);
    }

    public Set<Short> fillElements(GridFsService gridFsService) throws IOException {
        IDToEntityRepresentation idToEntityRepresentation = (IDToEntityRepresentation) getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        Map<Short, String> idToEntity = new ObjectMapper().readValue(gridFsService.getFileAsString(idToEntityRepresentation.getName()), new TypeReference<Map<Short, String>>() {});
        return new TreeSet<>(idToEntity.keySet());
    }

    private int getTotalNumberOfWeights() {
        return getWeightsList().stream().reduce(0, (totalNumberOfWeights, weights) -> totalNumberOfWeights + weights.getNumberOfWeights(), Integer::sum);
    }

    private List<String> getAllWeightsTypes() {
        return getWeightsList().stream().map(Weights::getType).collect(Collectors.toList());
    }

    public Map<Short, String> getIDToEntityName(GridFsService gridFsService) throws IOException {
        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) getStrategy().getCodebase().getRepresentationByFileType(ID_TO_ENTITY);
        return new ObjectMapper().readValue(gridFsService.getFileAsString(idToEntity.getName()), new TypeReference<Map<Short, String>>() {});
    }

    @Override
    public void generateRecommendation(RecommendationRepository recommendationRepository) {
        SciPyClustering clusteringAlgorithm = new SciPyClustering();
        clusteringAlgorithm.prepareAutowire();
        // Executes the request in a fork to avoid blocking the user
        ForkJoinPool.commonPool().submit(() -> {
            try {
                GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
                SimilarityScipy similarityScipy = SimilarityScipyFactory.getSimilarityScipy(this);
                setSimilarityMatricesNames(similarityScipy.generateMultipleMatrices(gridFsService, this, fillElements(gridFsService), getTotalNumberOfWeights()));
                clusteringAlgorithm.generateMultipleDecompositions(this);

                recommendationRepository.save(this);
                System.out.println("recommendation ended");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getDecompositionPropertiesForRecommendation(Decomposition decomposition) throws Exception {
        SimilarityScipy similarity = SimilarityScipyFactory.getSimilarityScipy(this);
        decomposition.setSimilarity(similarity);
        decomposition.setup();
        decomposition.calculateMetrics();
    }

    @Override
    public void createDecompositions(List<String> decompositionNames) throws Exception {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        DecompositionService decompositionService = ContextManager.get().getBean(DecompositionService.class);

        List<Similarity> similarities = getStrategy().getSimilarities();
        SciPyClustering clusteringAlgorithm = new SciPyClustering();

        for (String name : decompositionNames) {
            String[] properties = name.split(",");

            if (properties.length < 2)
                continue;

            System.out.println("Creating decomposition with name: " + name);

            List<Weights> localWeightsList = WeightsFactory.getWeightsList(getAllWeightsTypes());

            int i = 1;
            for (Weights weights : localWeightsList) {
                float[] w = new float[weights.getNumberOfWeights()];
                for (int j = 0; j < w.length; j++)
                    w[j] = Float.parseFloat(properties[i++]);

                weights.setWeightsFromArray(w);
            }

            SimilarityDto similarityInformation = new SimilarityScipyAccessesAndRepositoryDto(this, localWeightsList);

            // Get or create the decomposition's strategy
            SimilarityScipy similarity = (SimilarityScipy) similarities.stream().filter(possibleSimilarity ->
                    possibleSimilarity.equalsDto(similarityInformation)).findFirst().orElse(null);

            if (similarity == null) {
                similarity = SimilarityFactory.getSimilarity(getStrategy(), similarityInformation);

                InputStream inputStream = gridFsService.getFile(name.substring(0, name.lastIndexOf(","))); // Gets the previously produced similarity matrix
                gridFsService.saveFile(inputStream, similarity.getName()); // And saves it with a different name

                // generate dendrogram image
                similarity.setDendrogram(new Dendrogram(similarity.getName(), similarity.getName(), similarity.getLinkageType()));
            }

            Decomposition decomposition = clusteringAlgorithm.generateDecomposition(similarity, new SciPyRequestDto(similarity.getName(), "N", Float.parseFloat(properties[properties.length - 1])));
            decompositionService.setupDecomposition(decomposition);
        }
    }
}