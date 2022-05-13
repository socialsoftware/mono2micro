package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.dto.AnalyserResultDto;
import pt.ist.socialsoftware.mono2micro.dto.CutInfoDto;
import pt.ist.socialsoftware.mono2micro.dto.SimilarityMatrixDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.SCRIPTS_ADDRESS;

public class CommitAnalyserService {
    /* This class exists as a way to condensate all calculations and logic regarding the analyser of commit
    * decompositions.
    *
    * Analysing static decompositions is currently done by a mix of humungous controller methods under `AnalysisController`
    * and random Python scripts that don't adhere to the Unix philosophy of "Do One Thing, And Do It Well". Particularly,
    * the scripts write and throw around JSONs on the filesystem, there are things that could be done in the Java backend
    * that are delegated to the scripts, and there are method calls to various widely spread objects and classes.
    *
    * This class aims to be a centralized, logically organized, and well-commented place to host everything to do with
    * an Analyser made for commit-based decompositions. It could, perhaps, even serve as inspiration to refactor the
    * static Analyser - although I do not envy whoever may be tasked with such efforts.
    * */

    public String codebaseName;

    public CommitAnalyserService(String codebaseName) {
        this.codebaseName = codebaseName;
    }

    public boolean analyse() {
        JsonGenerator jGenerator = getJsonGenerator();
        if (jGenerator == null) return false;

        Codebase codebase = getCodebase();
        if (codebase == null) return false;

        SimilarityMatrixDto similarityMatrixDto = getGenericSimilarityMatrix(this.codebaseName);
        if (similarityMatrixDto == null) return false;

        int numberOfEntities = similarityMatrixDto.getEntities().size();
        List<List<Integer>> combinations = generateCutsCombinations(numberOfEntities);
        for (List<Integer> combination : combinations) {
            try {
                HashMap<String, Set<Short>> cut = performCut(combination, similarityMatrixDto);
                Decomposition newDecomposition = buildDecompositionAndCalculateMetrics(codebase, cut);
                CutInfoDto cutInfo = generateCutInfo(newDecomposition, combination);
                String cutName = combination.stream().map(String::valueOf).collect(Collectors.joining(","));
                jGenerator.writeObjectField(
                        cutName,
                        cutInfo
                );
                jGenerator.flush();
                System.out.println("Complexity obtained: " + newDecomposition.getComplexity());

            } catch (Exception e) {
                System.out.println("Something went wrong performing the cut: " + combination);
                e.printStackTrace();
            }
        }
        try {
            jGenerator.writeEndObject();
            jGenerator.close();
        } catch (IOException e) {
            System.out.println("Error when finishing JSON writing.");
            e.printStackTrace();
            return false;
        }
        System.out.println("Commit analyser ran with success.");
        return true;
    }

    private Codebase getCodebase() {
        Codebase codebase;
        try {
            codebase = CodebaseManager.getInstance().getCodebaseWithFields(
                    this.codebaseName,
                    new HashSet<String>() {{
                        add("name");
                        add("profiles");
                        add("datafilePath");
                    }}
            );
        } catch (IOException e) {
            System.out.println("Error when loading the codebase from disk.");
            return null;
        }
        return codebase;
    }

    private JsonGenerator getJsonGenerator() {
        File analyserPath = new File(CODEBASES_PATH + codebaseName + "/analyser/");
        if (!analyserPath.exists()) {
            analyserPath.mkdirs();
        }
        String analyserResultFilename = "commitAnalyserResult.json";
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonfactory = mapper.getFactory();
        JsonGenerator jGenerator = null;
        try {
            jGenerator = jsonfactory.createGenerator(
                    new FileOutputStream(CODEBASES_PATH + codebaseName + "/analyser/" + analyserResultFilename),
                    JsonEncoding.UTF8
            );

            jGenerator.useDefaultPrettyPrinter();
            jGenerator.writeStartObject();
        } catch (IOException e) {
            System.out.println("Error initializing analyzer result file.");
            return null;
        }
        return jGenerator;
    }

    private CutInfoDto generateCutInfo(Decomposition decomposition, List<Integer> cutValues) {
        AnalyserResultDto analyserResult = new AnalyserResultDto();
        analyserResult.setComplexity(decomposition.getComplexity());
        analyserResult.setCohesion(decomposition.getCohesion());
        analyserResult.setCoupling(decomposition.getCoupling());
        analyserResult.setPerformance(decomposition.getPerformance());
        analyserResult.setCommitWeight(cutValues.get(0));
        analyserResult.setAuthorsWeight(cutValues.get(1));
        analyserResult.setNumberClusters(cutValues.get(2));

        CutInfoDto cutInfo = new CutInfoDto();
        cutInfo.setAnalyserResultDto(analyserResult);
        return cutInfo;
    }

    private HashMap performCut(List<Integer> combination, SimilarityMatrixDto matrix) throws Exception {
        int maxTries = 5;
        int currentTry = 1;
        while (currentTry < maxTries) {
            try {
                return WebClient.create(SCRIPTS_ADDRESS)
                        .post()
                        .uri("/scipy/{codebaseName}/{commitMetricValue}/{authorsMetricValue}/{clusters}/cut",
                                codebaseName, String.valueOf(combination.get(0)),
                                String.valueOf(combination.get(1)), String.valueOf(combination.get(2)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromObject(matrix))
                        .exchange()
                        .doOnSuccess(clientResponse -> {
                            if (clientResponse.statusCode() != HttpStatus.OK)
                                throw new RuntimeException("Error Code:" + clientResponse.statusCode());
                        }).block()
                        .bodyToMono(new ParameterizedTypeReference<HashMap < String, Set < Short >> >() {
                        })
                        .block();
            } catch (Exception e) {
                System.out.println("Error when making cut request... Try " + currentTry + "/" + maxTries);
                currentTry += 1;
            }
        }
        System.out.println("Something went wrong with the request to the cut service.");
        throw new Exception();
    }

    private SimilarityMatrixDto getGenericSimilarityMatrix(String codebaseName) {
        /*
         * In the regular research workflow of Creating Codebase -> Creating Dendrogram -> Creating Cut, a similarity
         * matrix is created for each dendrogram with certain weights on the metrics (access, read, etc).
         * When performing an analysis, thousands of dendrograms would be created - which is prohibitively expensive
         * and time wasteful, especially because most of the effort on creating the lists is repeated on all of them.
         * As such, a "generic similarity matrix" can be created. This matrix contains the metrics data, but without
         * adding all of them up pondered. This means that, whenever a new cut must be performed, we can create a new
         * matrix based on the generic one, by just adding the values with the weights of that cut.
         * */

        SimilarityMatrixDto similarityMatrixDto = new SimilarityMatrixDto();
        HashMap<Short, ArrayList<Short>> commitChanges;
        HashMap<Short, ArrayList<String>> authorsChanges;

        try {
            commitChanges = this.fetchCommitChanges();
            authorsChanges = this.fetchAuthorsChanges();
        } catch (IOException e) {
            System.out.println("Error when attempting to read commit changes or authors changes from filesystem. " +
                    "Aborting analyser.");
            return null;
        }

        List<List<List<Float>>> matrix = new ArrayList<>();
        for (Short file1 : commitChanges.keySet()) {

            List<List<Float>> matrixRow = new ArrayList<>();

            for (Short file2 : commitChanges.keySet()) {
                List<Float> metric = new ArrayList<>();
                if (file1.equals(file2)) {
                    metric.add((float) 1); // Commit Metric
                    metric.add((float) 1); // Authors Metric
                    matrixRow.add(metric);
                    continue;
                }
                // Compute number of times file2 appears in file1's list, in commit changes
                metric.add((float) commitChanges.get(file1).stream().filter(f -> f.equals(file2)).count());
                // Compute authors in common between the two lists
                metric.add((float) authorsChanges.get(file1).stream().filter(authorsChanges.get(file2)::contains).count());
                matrixRow.add(metric);
            }
            matrix.add(matrixRow);
        }

        similarityMatrixDto.setMatrix(matrix);
        similarityMatrixDto.setEntities(commitChanges.keySet());
        similarityMatrixDto.setLinkageType("average");
        return similarityMatrixDto;
    }

    private List<List<Integer>> generateCutsCombinations(int numberOfEntities) {
        /*
         * Cuts are made according to certain weights on the similarity matrix metrics. We should vary these weights
         * so that we have all possible combinations of clusters and metrics' weights.
         */
        List<List<Integer>> combinations = new ArrayList<>();
        int interval = 10; // Each metric weight will be modified by 10 units at a time

        int maxClusters;
        if (numberOfEntities > 3 && numberOfEntities < 10) {
            maxClusters = 3;
        } else if (numberOfEntities >= 10 && numberOfEntities < 20) {
            maxClusters = 5;
        } else if (numberOfEntities >= 20) {
            maxClusters = 10;
        } else {
            System.out.println("Too few entities - should be at least 4, but there are " + numberOfEntities);
            return combinations;
        }

        /* This is a naive algorithm to build all combinations.
        *  We test which combinations of numbers in the range 0 to 100, with a step of 10, sum to 100.
        * */

        int[] firstMetricValues = IntStream.iterate(0, n -> n + interval).limit(100).toArray();
        int[] secondMetricValues = IntStream.iterate(0, n -> n + interval).limit(100).toArray();

        for (int first : firstMetricValues) {
            for (int second : secondMetricValues) {
                if (first + second == 100) {
                    IntStream.range(3, maxClusters+1).forEachOrdered(n -> {
                        List<Integer> newCombination = new ArrayList<>();
                        newCombination.add(first);
                        newCombination.add(second);
                        newCombination.add(n);
                        combinations.add(newCombination);

                    });
                }
            }
        }
//        List<Integer> maxComplexityCombination = new ArrayList<>();
//        maxComplexityCombination.add(100);
//        maxComplexityCombination.add(0);
//        maxComplexityCombination.add(numberOfEntities);
//        combinations.add(maxComplexityCombination);
        return combinations;
    }

    private Decomposition buildDecompositionAndCalculateMetrics(
            Codebase codebase,
            HashMap<String, Set<Short>> cut
    )
            throws Exception
    {
        Decomposition decomposition = new Decomposition();
        decomposition.setCodebaseName(codebase.getName());
        Set<String> clusterIDs = cut.keySet();
        decomposition.setNextClusterID(Integer.valueOf(clusterIDs.size()).shortValue());
        for (String clusterName : clusterIDs) {
            Short clusterId = Short.parseShort(clusterName);
            Set<Short> entities = cut.get(clusterName);
            Cluster cluster = new Cluster(clusterId, clusterName, entities);

            for (short entityID : entities)
                decomposition.putEntity(entityID, clusterId);

            decomposition.addCluster(cluster);
        }

        decomposition.setControllers(CodebaseManager.getInstance().getControllersWithCostlyAccesses(
                codebase,
                "Generic",
                decomposition.getEntityIDToClusterID()
        ));

        decomposition.calculateMetrics(
                codebase,
                0,
                Constants.TraceType.ALL,
                true
        );
        return decomposition;
    }

    private HashMap<Short, ArrayList<Short>> fetchCommitChanges() throws IOException {
        File commitChangesPath = new File(CODEBASES_PATH + this.codebaseName + "/" + "commitChanges.json");
        return new ObjectMapper().readValue(commitChangesPath, new TypeReference<Map<Short,ArrayList<Short>>>() {});
    }

    private HashMap<Short, ArrayList<String>>  fetchAuthorsChanges() throws IOException {
        File authorChangesPath = new File(CODEBASES_PATH + this.codebaseName + "/filesAuthors.json");
        return new ObjectMapper().readValue(authorChangesPath, new TypeReference<Map<Short,ArrayList<String>>>() {});
    }
}
