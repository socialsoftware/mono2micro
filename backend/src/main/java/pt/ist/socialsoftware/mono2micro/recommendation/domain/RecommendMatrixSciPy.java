package pt.ist.socialsoftware.mono2micro.recommendation.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.DecompositionFactory;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.SciPyRequestDto;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.algorithm.RecommendationForSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.similarityGenerator.SimilarityMatrices;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendationRepository;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityFactory;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.SimilarityMatrixGenerator;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.AccessesWeights;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.WeightsFactory;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition.ACCESSES_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition.REPOSITORY_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

@Document("recommendation")
public class RecommendMatrixSciPy extends Recommendation implements SimilarityMatrices, RecommendationForSciPy {
    public static final String RECOMMEND_MATRIX_SCIPY = "RECOMMEND_MATRIX_SCIPY";
    private String profile;
    private int tracesMaxLimit;
    private Constants.TraceType traceType;
    private String linkageType;
    private List<Weights> weightsList;
    private Set<String> similarityMatricesNames;

    @Transient
    private byte[] representationBytes;

    private static final List<String> implementationTypes = new ArrayList<String>() {{
        add(SIMILARITY_MATRICES);
        add(RECOMMENDATION_FOR_SCIPY);
    }};

    public RecommendMatrixSciPy() {}

    public RecommendMatrixSciPy(RecommendMatrixSciPyDto dto) {
        this.decompositionType = dto.getDecompositionType();
        this.profile = dto.getProfile();
        this.tracesMaxLimit = dto.getTracesMaxLimit();
        this.traceType = dto.getTraceType();
        this.linkageType = dto.getLinkageType();
        this.isCompleted = dto.isCompleted();
        this.weightsList = dto.getWeightsList();
        this.similarityMatricesNames = new HashSet<>();
    }

    public String getType() {
        return RECOMMEND_MATRIX_SCIPY;
    }

    public List<String> getImplementations() {
        return implementationTypes;
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

    @Override
    public List<Weights> getWeightsList() {
        return weightsList;
    }

    public void setWeightsList(List<Weights> weightsList) {
        this.weightsList = weightsList;
    }

    public byte[] getRepresentationBytes() {
        return representationBytes;
    }

    public void setRepresentationBytes(byte[] representationBytes) {
        this.representationBytes = representationBytes;
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

    @Override
    public Set<Short> fillElements(GridFsService gridFsService) throws IOException, JSONException {
        Set<Short> elements = new TreeSet<>();
        AccessesRepresentation accesses = (AccessesRepresentation) getStrategy().getCodebase().getRepresentationByType(ACCESSES);
        AccessesWeights.fillDataStructures( // TODO THIS METHOD SHOULD BE REFACTORED TO ONLY OBTAIN ELEMENTS FROM ACCESSES FILE
                elements, new HashMap<>(), new HashMap<>(),
                new FunctionalityTracesIterator(gridFsService.getFile(accesses.getName()), getTracesMaxLimit()),
                accesses.getProfile(getProfile()),
                getTraceType());
        return elements;
    }

    @Override
    public Map<Short, String> getIDToEntityName(GridFsService gridFsService) throws IOException {
        IDToEntityRepresentation idToEntity = (IDToEntityRepresentation) getStrategy().getCodebase().getRepresentationByType(ID_TO_ENTITY);
        return new ObjectMapper().readValue(gridFsService.getFileAsString(idToEntity.getName()), new TypeReference<Map<Short, String>>() {});
    }

    @Override
    public void generateRecommendation(RecommendationRepository recommendationRepository, GridFsService gridFsService) {
        SciPyClustering clusteringAlgorithm = new SciPyClustering(gridFsService);
        clusteringAlgorithm.prepareAutowire();
        // Executes the request in a fork to avoid blocking the user
        ForkJoinPool.commonPool().submit(() -> {
            try {
                SimilarityMatrixGenerator generator = new SimilarityMatrixGenerator(gridFsService);
                generator.createSimilarityMatrices(this);
                clusteringAlgorithm.generateMultipleDecompositions(this);

                recommendationRepository.save(this);
                System.out.println("recommendation ended");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getDecompositionPropertiesForRecommendation(GridFsService gridFsService, Decomposition decomposition) throws Exception {
        if (decomposition.containsImplementation(ACCESSES_DECOMPOSITION)) {
            AccessesRepresentation accesses = (AccessesRepresentation) getStrategy().getCodebase().getRepresentationByType(ACCESSES);
            byte[] representationBytes = getRepresentationBytes();
            if (representationBytes == null) {
                representationBytes = IOUtils.toByteArray(gridFsService.getFile(accesses.getName()));
                setRepresentationBytes(representationBytes);
            }
            ((AccessesDecomposition)decomposition).setupFunctionalities(
                    null, null, new ByteArrayInputStream(representationBytes),
                    getProfile(), getTracesMaxLimit(), getTraceType());
        }
        if (decomposition.containsImplementation(REPOSITORY_DECOMPOSITION)) {
            ((RepositoryDecomposition) decomposition).setupAuthorsAndCommits(gridFsService);
        }
        // FILL DECOMPOSITION WITH SPECIFIC PROPERTIES HERE

        decomposition.calculateMetrics();
    }

    public void createDecompositions(
            DecompositionService decompositionService,
            GridFsService gridFsService,
            List<String> decompositionNames
    )
            throws Exception
    {
        List<Similarity> similarities = getStrategy().getSimilarities();
        SciPyClustering clusteringAlgorithm = new SciPyClustering(gridFsService);

        for (String name : decompositionNames) {
            String[] properties = name.split(",");

            if (properties.length < 2)
                continue;

            System.out.println("Creating decomposition with name: " + name);

            List<Weights> weightsList = WeightsFactory.getWeightsList(this.getAllWeightsTypes());

            int i = 1;
            for (Weights weights : weightsList) {
                float[] w = new float[weights.getNumberOfWeights()];
                for (int j = 0; j < w.length; j++)
                    w[j] = Float.parseFloat(properties[i++]);

                weights.setWeightsFromArray(w);
            }

            SimilarityMatrixSciPyDto similarityInformation = new SimilarityMatrixSciPyDto(this, weightsList);

            // Get or create the decomposition's strategy
            SimilarityMatrixSciPy similarity = (SimilarityMatrixSciPy) similarities.stream().filter(possibleSimilarity ->
                    possibleSimilarity.equalsDto(similarityInformation)).findFirst().orElse(null);
            if (similarity == null) {
                similarity = (SimilarityMatrixSciPy) SimilarityFactory.getSimilarity(getStrategy(), similarityInformation);

                similarity.setSimilarityMatrixName(similarity.getName() + "_similarityMatrix");
                InputStream inputStream = gridFsService.getFile(name.substring(0, name.lastIndexOf(","))); // Gets the previously produced similarity matrix
                gridFsService.saveFile(inputStream, similarity.getSimilarityMatrixName()); // And saves it with a different name

                // generate dendrogram image
                clusteringAlgorithm.createDendrogramImage(similarity);
            }

            Decomposition decomposition = DecompositionFactory.getDecomposition(similarity.getDecompositionType());
            decomposition.setSimilarity(similarity);
            similarity.addDecomposition(decomposition);
            decomposition.setStrategy(similarity.getStrategy());
            similarity.getStrategy().addDecomposition(decomposition);

            clusteringAlgorithm.generateClusters(decomposition, new SciPyRequestDto(similarity.getName(), "N", Float.parseFloat(properties[properties.length - 1])));
            decompositionService.setupDecomposition(decomposition);
        }
    }
}