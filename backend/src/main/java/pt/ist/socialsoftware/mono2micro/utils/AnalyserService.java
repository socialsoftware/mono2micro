package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.dto.*;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.SCRIPTS_ADDRESS;

public class AnalyserService {
    /* This class takes care of necessary computations and management regarding the analyser.
    *  Previously, the whole analyser ran in the controller, which is a very confusing and messy way of dealing with
    * it. Hopefully it gets simpler, more understandable, and faster by everything being in the same place.
    * This class/algorithm was also designed as parallel-first, avoiding concurrent data accesses as much as possible.
    */

    private final Codebase codebase;
    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();
    private String codeInfosDirectory;

    private void initializePaths(String codebaseName) throws IOException {
        File analyserPath = new File(CODEBASES_PATH + codebaseName + "/analyser/cuts/");
        File cutsInfosPath = new File(CODEBASES_PATH + codebaseName + "/analyser/cutsInfos/");
        this.codeInfosDirectory = CODEBASES_PATH + codebaseName + "/analyser/cutsInfos/";
        if (!analyserPath.exists()) {
            if (analyserPath.mkdirs()) {
                System.out.println("Analyser directories created.");
            } else {
                throw new IOException("Error creating analyser directories.");
            }
        }

        if (!cutsInfosPath.exists()) {
            if (cutsInfosPath.mkdirs()) {
                System.out.println("Cuts Infos directory created.");
            } else {
                throw new IOException("Error creating uts infos directory .");
            }
        }
    }

    public AnalyserService(String codebaseName) throws IOException {
        initializePaths(codebaseName);

        this.codebase = CodebaseManager.getInstance().getCodebaseWithFields(
                codebaseName,
                new HashSet<String>() {{
                    add("name");
                    add("profiles");
                    add("datafilePath");
                }}
        );

    }

    public void runAnalyser(AnalyserDto analyserSettings) throws IOException {
        int numberOfEntitiesPresentInCollection = getOrCreateSimilarityMatrix(
                codebase,
                analyserSettings
        ).getEntities().size();
        System.out.println("Codebase: " + this.codebase.getName() + " has " + numberOfEntitiesPresentInCollection + " entities");

        executeCreateCuts(
                this.codebase.getName(),
                numberOfEntitiesPresentInCollection
        );

        AtomicReference<Short> count = new AtomicReference<>((short) 0);
        List<File> cutsFiles = loadCuts(-1, true);
        Map<Cut, Decomposition> cutsToDecomposition = Collections.synchronizedMap(new HashMap<>());
        StaticCollection collection = getStaticCollectionData();
        int totalNumberOfFiles = cutsFiles.size();

        cutsFiles.parallelStream().forEach(file -> {
            CutInfoDto cutInfo = getCutInfo(analyserSettings, cutsToDecomposition, collection, file);
            String filename = FilenameUtils.getBaseName(file.getName());
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(new File(codeInfosDirectory + filename + ".json"), cutInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
            count.getAndSet((short) (count.get() + 1));
            System.out.println("NEW: " + filename + " : " + count.get() + "/" + totalNumberOfFiles);
        });

        mergeCuts();
        System.out.println("Analyser finished.");
        File cutInfosPath = new File(CODEBASES_PATH + codebase.getName() + "/analyser/cutsInfos/");
        File[] cutInfos = cutInfosPath.listFiles();
        assert cutInfos != null;
        Arrays.stream(cutInfos).forEach(File::delete);

    }

    private void mergeCuts() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonfactory = mapper.getFactory();
        JsonGenerator jGenerator = jsonfactory.createGenerator(
            new FileOutputStream(CODEBASES_PATH + codebase.getName() + "/analyser/analyserResult.json"),
            JsonEncoding.UTF8
        );
        jGenerator.useDefaultPrettyPrinter();
        jGenerator.writeStartObject();
        File cutInfosPath = new File(CODEBASES_PATH + codebase.getName() + "/analyser/cutsInfos/");
        File[] cutInfos = cutInfosPath.listFiles();
        assert cutInfos != null;
        Arrays.stream(cutInfos).forEach(file -> {
            InputStream fis = null;
            try {
                fis = new FileInputStream(file);
                ObjectMapper oM = new ObjectMapper();
                oM.enable(SerializationFeature.INDENT_OUTPUT);

                jGenerator.writeObjectField(
                        FilenameUtils.getBaseName(file.getName()),
                        oM.readerFor(CutInfoDto.class).readValue(fis)
                );
                jGenerator.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        jGenerator.writeEndObject();
		jGenerator.close();
    }

    private CutInfoDto getCutInfo(AnalyserDto analyserSettings, Map<Cut, Decomposition> cutsToDecomposition, StaticCollection collection, File file) {
        String filename = FilenameUtils.getBaseName(file.getName());
        Cut cut = null;
        try {
            InputStream fis = new FileInputStream(file);
            ObjectMapper oM = new ObjectMapper();
            oM.enable(SerializationFeature.INDENT_OUTPUT);
            cut = oM.readerFor(Cut.class).readValue(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Decomposition previousDecomposition = cutsToDecomposition.get(cut);
        CutInfoDto cutInfo = null;
        if (previousDecomposition != null) {
            try {
                cutInfo = assembleCutInformation(
                        previousDecomposition,
                        filename
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Decomposition decomposition;
            try {
                decomposition = buildDecompositionAndCalculateMetrics(
                        analyserSettings,
                        codebase,
                        filename,
                        collection
                );
                cutInfo = assembleCutInformation(
                        decomposition,
                        filename
                );
                cutsToDecomposition.put(cut, decomposition);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cutInfo;
    }

    private StaticCollection getStaticCollectionData() throws IOException {
        System.out.println("Objectifying static collection data");
        InputStream is = new FileInputStream(codebase.getDatafilePath());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        StaticCollection collection = objectMapper.readerFor(StaticCollection.class).readValue(is);
        is.close();
        return collection;
    }

    private Decomposition buildDecompositionAndCalculateMetrics(
            AnalyserDto analyser,
            Codebase codebase, // requirements: name, profiles, datafilePath
            String filename,
            StaticCollection collection
    )
            throws Exception
    {
        Decomposition decomposition = new Decomposition();
        decomposition.setCodebaseName(codebase.getName());

        HashMap<String, HashMap<String, Set<Short>>> analyserCut = codebaseManager.getAnalyserCut(
                codebase.getName(),
                filename
        );

        Set<String> clusterIDs = analyserCut.get("clusters").keySet();

        decomposition.setNextClusterID(Integer.valueOf(clusterIDs.size()).shortValue());

        for (String clusterName : clusterIDs) {
            Short clusterId = Short.parseShort(clusterName);
            Set<Short> entities = analyserCut.get("clusters").get(clusterName);
            Cluster cluster = new Cluster(clusterId, clusterName, entities);

            for (short entityID : entities)
                decomposition.putEntity(entityID, clusterId);

            decomposition.addCluster(cluster);
        }
        decomposition.setControllers(codebaseManager.getControllersWithCostlyAccesses(
                codebase,
                analyser.getProfile(),
                decomposition.getEntityIDToClusterID()
        ));
        decomposition.calculateMetrics(
                codebase,
                analyser.getTracesMaxLimit(),
                analyser.getTraceType(),
                true,
                collection
        );

        return decomposition;
    }

    private CutInfoDto assembleCutInformation(
            Decomposition decomposition,
            String filename
    )
            throws IOException
    {
        AnalyserResultDto analyserResult = new AnalyserResultDto();
        analyserResult.setComplexity(decomposition.getComplexity());
        analyserResult.setCohesion(decomposition.getCohesion());
        analyserResult.setCoupling(decomposition.getCoupling());
        analyserResult.setMaxClusterSize(decomposition.maxClusterSize());

        String[] similarityWeights = filename.split(",");
        analyserResult.setAccessWeight(Float.parseFloat(similarityWeights[0]));
        analyserResult.setWriteWeight(Float.parseFloat(similarityWeights[1]));
        analyserResult.setReadWeight(Float.parseFloat(similarityWeights[2]));
        analyserResult.setSequenceWeight(Float.parseFloat(similarityWeights[3]));
        analyserResult.setCommitWeight(Integer.parseInt(similarityWeights[4]));
        analyserResult.setAuthorsWeight(Integer.parseInt(similarityWeights[5]));
        analyserResult.setNumberClusters(Float.parseFloat(similarityWeights[6]));

        CutInfoDto cutInfo = new CutInfoDto();
        cutInfo.setAnalyserResultDto(analyserResult);

        return cutInfo;
    }

    private List<File> loadCuts(int limit, boolean recoverPreviousCutInfos) {
        File analyserCutsPath = new File(CODEBASES_PATH + this.codebase.getName() + "/analyser/cuts/");
        File[] files = analyserCutsPath.listFiles();
        assert files != null;
        if (!recoverPreviousCutInfos) { /* Load the desired ones, any previous if they exist will be replaced */
            if (limit != -1)
                return Arrays.stream(files).limit(limit).collect(Collectors.toList());
            else
                return Arrays.stream(files).collect(Collectors.toList());
        }
        /* We want to load previous cutInfos - in other words, we only want new cuts, that have not been processed
        * yet */
        File cutInfosPath = new File(CODEBASES_PATH + codebase.getName() + "/analyser/cutsInfos/");
        File[] cutInfos = cutInfosPath.listFiles();
        assert cutInfos != null;
        ArrayList<String> cutInfosList = new ArrayList<>();
        for (File file : cutInfos) {
            cutInfosList.add(FilenameUtils.getBaseName(file.getName()));
        }
        if (limit != -1)
            return Arrays.stream(files)
                    .filter(file -> !cutInfosList.contains(FilenameUtils.getBaseName(file.getName())))
                    .limit(limit)
                    .collect(Collectors.toList());
        else
            return Arrays.stream(files)
                    .filter(file -> !cutInfosList.contains(FilenameUtils.getBaseName(file.getName())))
                    .collect(Collectors.toList());



//        int totalNumberOfFiles = files.length;
//
//        HashMap<String, Cut> cuts = new HashMap<>();
//        System.out.print("Loading cuts from storage... " + 0 + "/" + totalNumberOfFiles + "\r");
//        Arrays.stream(files).forEach(file -> {
//            try {
//                InputStream fis = new FileInputStream(file);
//                ObjectMapper oM = new ObjectMapper();
//                oM.enable(SerializationFeature.INDENT_OUTPUT);
//                Cut cut = oM.readerFor(Cut.class).readValue(fis);
//                cuts.put(FilenameUtils.getBaseName(file.getName()), cut);
//                System.out.print("Loading cuts from storage... " + cuts.size() + "/" + totalNumberOfFiles + "\r");
//                fis.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        return filenames;
    }

    private void executeCreateCuts(
            String codebaseName,
            int numberOfEntitiesPresentInCollection
    )
    {
        System.out.println("Creating cuts...");
        WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{codebaseName}/{totalNumberOfEntities}/analyser",
                        codebaseName, String.valueOf(numberOfEntitiesPresentInCollection))
                .exchange()
                .doOnSuccess(clientResponse -> {
                    if (clientResponse.statusCode() != HttpStatus.OK)
                        throw new RuntimeException("Error Code:" + clientResponse.statusCode());
                }).block();
        System.out.println("Done.");
    }

    private SimilarityMatrixDto getOrCreateSimilarityMatrix(
            Codebase codebase,
            AnalyserDto analyser
    )
            throws IOException
    {
        SimilarityMatrixDto similarityMatrixDto;

        if (!codebaseManager.analyserSimilarityMatrixFileAlreadyExists(codebase.getName())) {

            Utils.GetDataToBuildSimilarityMatrixResult result = Utils.getDataToBuildSimilarityMatrix(
                    codebase,
                    analyser.getProfile(),
                    analyser.getTracesMaxLimit(),
                    analyser.getTraceType()
            );

            // Unfortunately, the getMatrixData method differs from the one used in the Dendrogram
            similarityMatrixDto = getMatrixData(
                    result.entities,
                    result.e1e2PairCount,
                    result.entityControllers,
                    fetchCommitChanges(codebase.getName()),
                    fetchAuthorsChanges(codebase.getName())
            );

            CodebaseManager.getInstance().writeAnalyserSimilarityMatrix(
                    codebase.getName(),
                    similarityMatrixDto
            );

        } else {
            System.out.println("Similarity matrix already exists...");

            similarityMatrixDto = CodebaseManager.getInstance().getSimilarityMatrixDtoWithFields(
                    codebase.getName(),
                    new HashSet<String>() {{ add("entities"); }}
            );
        }

        return similarityMatrixDto;
    }

    private static SimilarityMatrixDto getMatrixData(
            Set<Short> entityIDs,
            Map<String,Integer> e1e2PairCount,
            Map<Short, List<Pair<String, Byte>>> entityControllers,
            HashMap<String, Map<String, Integer>> commitChanges,
            HashMap<Short, ArrayList<String>> authorChanges
    ) {
        SimilarityMatrixDto matrixData = new SimilarityMatrixDto();

        List<List<List<Float>>> similarityMatrix = new ArrayList<>();

        int maxNumberOfPairs = Utils.getMaxNumberOfPairs(e1e2PairCount);

        for (short e1ID : entityIDs) {
            List<List<Float>> matrixRow = new ArrayList<>();

            for (short e2ID : entityIDs) {
                List<Float> metric = new ArrayList<>();

                if (e1ID == e2ID) {
                    metric.add((float) 1);
                    metric.add((float) 1);
                    metric.add((float) 1);
                    metric.add((float) 1);
                    metric.add((float) 1); // commit
                    metric.add((float) 1); // author

                    matrixRow.add(metric);
                    continue;
                }

                float[] metrics = Utils.calculateSimilarityMatrixMetrics(
                        entityControllers,
                        e1e2PairCount,
                        e1ID,
                        e2ID,
                        maxNumberOfPairs
                );

                float[] commitMetrics = Utils.calculateSimilarityMatrixCommitMetrics(e1ID, e2ID, commitChanges, authorChanges);

                metric.add(metrics[0]);
                metric.add(metrics[1]);
                metric.add(metrics[2]);
                metric.add(metrics[3]);
                metric.add(commitMetrics[0]);
                metric.add(commitMetrics[1]);

                matrixRow.add(metric);
            }
            similarityMatrix.add(matrixRow);
        }
        matrixData.setMatrix(similarityMatrix);
        matrixData.setEntities(entityIDs);
        matrixData.setLinkageType("average");

        return matrixData;
    }

    private HashMap<String, Map<String, Integer>> fetchCommitChanges(String codebaseName) throws IOException {
        File commitChangesPath = new File(CODEBASES_PATH + codebaseName + "/" + "commitChanges.json");
        return new ObjectMapper().readValue(commitChangesPath, new TypeReference<Map<String, Map<String, Integer>>>() {});
    }

    private HashMap<Short, ArrayList<String>>  fetchAuthorsChanges(String codebaseName) throws IOException {
        File authorChangesPath = new File(CODEBASES_PATH + codebaseName + "/filesAuthors.json");
        return new ObjectMapper().readValue(authorChangesPath, new TypeReference<Map<Short,ArrayList<String>>>() {});
    }

}
