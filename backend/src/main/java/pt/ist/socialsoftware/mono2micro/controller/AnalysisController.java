package pt.ist.socialsoftware.mono2micro.controller;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.dto.*;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.ControllerTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Pair;
import pt.ist.socialsoftware.mono2micro.utils.Utils;
import pt.ist.socialsoftware.mono2micro.utils.mojoCalculator.src.main.java.MoJo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.*;

@RestController
@RequestMapping(value = "/mono2micro")
public class AnalysisController {

    private static Logger logger = LoggerFactory.getLogger(AnalysisController.class);
    private CodebaseManager codebaseManager = CodebaseManager.getInstance();

	@RequestMapping(value = "/codebase/{codebaseName}/analyser", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> analyser(
		@PathVariable String codebaseName,
		@RequestBody AnalyserDto analyser
	) {
		logger.debug("analyser");

		try {
			File analyserPath = new File(CODEBASES_PATH + codebaseName + "/analyser/cuts/");
			if (!analyserPath.exists()) {
				analyserPath.mkdirs();
			}

			Codebase codebase = CodebaseManager.getInstance().getCodebaseWithFields(
				codebaseName,
				new HashSet<String>() {{ add("analysisType"); add("name"); add("profiles"); add("datafilePath"); }}
			);

			HashMap<String, ControllerDto> datafileJSON = null;

			int numberOfEntitiesPresentInCollection = getOrCreateSimilarityMatrix(
				codebase,
				datafileJSON, // yes but its content may change if codebase is static but CHECK!
				analyser
			);

			System.out.println("Codebase: " + codebaseName + " has " + numberOfEntitiesPresentInCollection + " entities");

			executeCreateCutsPythonScript(
				codebaseName,
				numberOfEntitiesPresentInCollection
			);

			File analyserCutsPath = new File(CODEBASES_PATH + codebaseName + "/analyser/cuts/");
			File[] files = analyserCutsPath.listFiles();
			int totalNumberOfFiles = files.length;

			ObjectMapper mapper = new ObjectMapper();
			JsonFactory jsonfactory = mapper.getFactory();

			boolean analyserResultFileAlreadyExists = codebaseManager.analyserResultFileAlreadyExists(codebaseName);

			String analyserResultFilename =  analyserResultFileAlreadyExists ?
				"newAnalyserResult.json" :
				"analyserResult.json";

			JsonGenerator jGenerator = jsonfactory.createGenerator(
				new FileOutputStream(CODEBASES_PATH + codebaseName + "/analyser/" + analyserResultFilename),
				JsonEncoding.UTF8
			);

		 	jGenerator.useDefaultPrettyPrinter();
			jGenerator.writeStartObject();

			Set<String> cutInfoNames = new HashSet<>();

			if (analyserResultFileAlreadyExists) {

				File existentAnalyserResultFile = new File(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json");

				cutInfoNames = Utils.getJsonFileKeys(existentAnalyserResultFile);

				if (cutInfoNames.size() == totalNumberOfFiles) {
					System.out.println("Analyser Complete");
					return new ResponseEntity<>(HttpStatus.OK);
				}

				JsonParser jsonParser = jsonfactory.createParser(existentAnalyserResultFile);
				jsonParser.nextValue();

				if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
					System.err.println("Json must start with a left curly brace");
					System.exit(-1);
				}

				jsonParser.nextValue();

				while (jsonParser.getCurrentToken() != JsonToken.END_OBJECT) {
					if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
						Utils.print("Cut name: " + jsonParser.getCurrentName(), Utils.lineno());
						cutInfoNames.add(jsonParser.currentName());

						CutInfoDto cutInfo = jsonParser.readValueAs(CutInfoDto.class);

						jGenerator.writeObjectField(jsonParser.getCurrentName(), cutInfo);

						jsonParser.nextValue();
					}
				}

				jGenerator.flush();

				existentAnalyserResultFile.delete();
			}

			int maxRequests = analyser.getRequestLimit();
			short newRequestsCount = 0;
			short count = 0;

			for (File file : files) {

				String filename = FilenameUtils.getBaseName(file.getName());

				count++;

                if (cutInfoNames.contains(filename)) {
                    System.out.println(filename + " already analysed. " + count + "/" + totalNumberOfFiles);
                    continue;
                }

				Graph graph = buildGraphAndCalculateMetrics(
					analyser,
					codebase,
					filename,
					datafileJSON
				);

                CutInfoDto cutInfo = assembleCutInformation(
                	analyser,
					graph,
					filename
				);

				jGenerator.writeObjectField(
					filename,
					cutInfo
				);

				jGenerator.flush();

				newRequestsCount++;

				System.out.println("NEW: " + filename + " : " + count + "/" + totalNumberOfFiles);
				if (newRequestsCount == maxRequests)
					break;

			}

			jGenerator.writeEndObject();
			jGenerator.close();

			if (analyserResultFileAlreadyExists) {
				File fileToBeRenamed = new File(CODEBASES_PATH + codebaseName + "/analyser/" + analyserResultFilename);
				File fileRenamed = new File(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json");

				fileToBeRenamed.renameTo(fileRenamed);
			}

//            codebaseManager.writeAnalyserResults(codebaseName, analyserJSON);
			System.out.println("Analyser Complete");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	public int getOrCreateSimilarityMatrix(
		Codebase codebase,
		HashMap<String, ControllerDto> datafileJSON,
		AnalyserDto analyser

	)
		throws IOException
	{

		if (!codebaseManager.analyserSimilarityMatrixFileAlreadyExists(codebase.getName())) {
			System.out.println("Creating similarity matrix...");

			if (codebase.isStatic()) {
				datafileJSON = CodebaseManager.getInstance().getDatafile(codebase);

				return createStaticAnalyserSimilarityMatrix(
					codebase,
					analyser,
					datafileJSON
				);
			}

			return createDynamicAnalyserSimilarityMatrix(codebase, analyser);
		}

		System.out.println("Similarity matrix already exists...");

		SimilarityMatrixDto similarityMatrixDto = CodebaseManager.getInstance().getSimilarityMatrixDtoWithFields(
			codebase.getName(),
			new HashSet<String>() {{ add("entities"); }}
		);

		return similarityMatrixDto.getEntities().size();
	}

	private void executeCreateCutsPythonScript(
		String codebaseName,
		int numberOfEntitiesPresentInCollection
	)
		throws InterruptedException, IOException
	{
		System.out.println("Executing analyser.py to create cuts...");

		Runtime r = Runtime.getRuntime();
		String pythonScriptPath = RESOURCES_PATH + "analyser.py";
		String[] cmd = new String[5];
		cmd[0] = PYTHON;
		cmd[1] = pythonScriptPath;
		cmd[2] = CODEBASES_PATH;
		cmd[3] = codebaseName;
		cmd[4] = String.valueOf(numberOfEntitiesPresentInCollection);
		Process p = r.exec(cmd);
		p.waitFor();

		System.out.println("script execution has ended");

	}

	private Graph buildGraphAndCalculateMetrics(
		AnalyserDto analyser,
		Codebase codebase,
		String filename,
		HashMap<String, ControllerDto> datafileJSON
	)
		throws IOException
	{
		Graph graph = new Graph();
		graph.setCodebaseName(codebase.getName());

		HashMap<String, HashMap<String, Set<Short>>> analyserCut = codebaseManager.getAnalyserCut(
			codebase.getName(),
			filename
		);

		Set<String> clusterIDs = analyserCut.get("clusters").keySet();

		for (String clusterId : clusterIDs) {
			Set<Short> entities = analyserCut.get("clusters").get(clusterId);
			Cluster cluster = new Cluster(Short.parseShort(clusterId), entities);

			for (short entityID : entities)
				graph.putEntity(entityID, clusterId);

			graph.addCluster(cluster);
		}

		if (codebase.isStatic()) {
			graph.calculateMetricsAnalyser(
				analyser.getProfiles(),
				datafileJSON
			);

		} else {
			graph.calculateDynamicMetricsAnalyser(
				analyser.getProfiles(),
				analyser.getTracesMaxLimit(),
				analyser.getTypeOfTraces()
			);
		}

		return graph;
	}

	private CutInfoDto assembleCutInformation(
		AnalyserDto analyser,
		Graph graph,
		String filename
	)
		throws IOException
	{
		AnalysisDto analysisDto = new AnalysisDto();
		analysisDto.setGraph1(analyser.getExpert());
		analysisDto.setGraph2(graph);

		analysisDto = getAnalysis(analysisDto).getBody();

		AnalyserResultDto analyserResult = new AnalyserResultDto();
		analyserResult.setAccuracy(analysisDto.getAccuracy());
		analyserResult.setPrecision(analysisDto.getPrecision());
		analyserResult.setRecall(analysisDto.getRecall());
		analyserResult.setSpecificity(analysisDto.getSpecificity());
		analyserResult.setFmeasure(analysisDto.getFmeasure());
		analyserResult.setMojoBiggest(analysisDto.getMojoBiggest());
		analyserResult.setMojoCommon(analysisDto.getMojoCommon());
		analyserResult.setMojoSingletons(analysisDto.getMojoSingletons());
		analyserResult.setMojoNew(analysisDto.getMojoNew());

		analyserResult.setComplexity(graph.getComplexity());
		analyserResult.setCohesion(graph.getCohesion());
		analyserResult.setCoupling(graph.getCoupling());
		analyserResult.setPerformance(graph.getPerformance());

		analyserResult.setMaxClusterSize(graph.maxClusterSize());

		String[] similarityWeights = filename.split(",");
		analyserResult.setAccessWeight(Float.parseFloat(similarityWeights[0]));
		analyserResult.setWriteWeight(Float.parseFloat(similarityWeights[1]));
		analyserResult.setReadWeight(Float.parseFloat(similarityWeights[2]));
		analyserResult.setSequenceWeight(Float.parseFloat(similarityWeights[3]));
		analyserResult.setNumberClusters(Float.parseFloat(similarityWeights[4]));

		CutInfoDto cutInfo = new CutInfoDto();
		cutInfo.setAnalyserResultDto(analyserResult);

		HashMap<String, HashMap<String, Float>> controllerSpecs = new HashMap<>();
		for (Controller controller : graph.getControllers()) {
			controllerSpecs.put(
				controller.getName(),
				new HashMap<String, Float>() {{
					put("complexity", controller.getComplexity());
					put("performance", (float) controller.getPerformance());
				}}
			);
		}

		cutInfo.setControllerSpecs(controllerSpecs);

		return cutInfo;
	}

	private SimilarityMatrixDto getAnalyserMatrixData(
		Set<Short> entityIDs,
		Map<String,Integer> e1e2PairCount,
		Map<Short,List<Pair<String,String>>> entityControllers
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

				metric.add(metrics[0]);
				metric.add(metrics[1]);
				metric.add(metrics[2]);
				metric.add(metrics[3]);

				matrixRow.add(metric);
			}
			similarityMatrix.add(matrixRow);
		}
		matrixData.setMatrix(similarityMatrix);
		matrixData.setEntities(entityIDs);
		matrixData.setLinkageType("average");

		return matrixData;
	}
	
	private int createStaticAnalyserSimilarityMatrix(
		Codebase codebase,
		AnalyserDto analyser,
		HashMap<String, ControllerDto> datafileJSON
	)
		throws IOException
	{
		Map<Short,List<Pair<String,String>>> entityControllers = new HashMap<>();
		Map<String,Integer> e1e2PairCount = new HashMap<>();

		for (String profile : analyser.getProfiles()) {
			for (String controllerName : codebase.getProfile(profile)) {
				ControllerDto controllerDto = datafileJSON.get(controllerName);
				List<AccessDto> controllerAccesses = controllerDto.getControllerAccesses();

				Utils.fillEntityDataStructures(
					entityControllers,
					e1e2PairCount,
					controllerAccesses,
					controllerName
				);
			}
		}

		Set<Short> entities = new TreeSet<>(entityControllers.keySet());

		CodebaseManager.getInstance().writeAnalyserSimilarityMatrix(
			codebase.getName(),
			getAnalyserMatrixData(
				entities,
				e1e2PairCount,
				entityControllers
			)
		);

		return entities.size();
	}

	private int createDynamicAnalyserSimilarityMatrix(
		Codebase codebase,
		AnalyserDto analyser
	)
		throws IOException
	{
		Map<Short,List<Pair<String,String>>> entityControllers = new HashMap<>();
		Map<String,Integer> e1e2PairCount = new HashMap<>();

		ControllerTracesIterator iter = new ControllerTracesIterator(
			codebase.getDatafilePath(),
			analyser.getTracesMaxLimit()
		);

		TraceDto t;

		for (String profile : analyser.getProfiles()) {
			for (String controllerName : codebase.getProfile(profile)) {
				iter.nextController(controllerName);

				switch (analyser.getTypeOfTraces()) {
					case LONGEST:
						// FIXME return accesses of longest trace instead of the trace itself
						t = iter.getLongestTrace();

						if (t != null) {
							Utils.fillEntityDataStructures(
								entityControllers,
								e1e2PairCount,
								t.expand(2),
								controllerName
							);
						}

						break;

					case WITH_MORE_DIFFERENT_ACCESSES:
						t = iter.getTraceWithMoreDifferentAccesses();

						if (t != null) {
							Utils.fillEntityDataStructures(
								entityControllers,
								e1e2PairCount,
								t.expand(2),
								controllerName
							);
						}

						break;

					case REPRESENTATIVE:
						Set<String> tracesIds = iter.getRepresentativeTraces();
						// FIXME probably here we create a second controllerTracesIterator
						iter.reset();

						while (iter.hasMoreTraces()) {
							t = iter.nextTrace();

							if (tracesIds.contains(String.valueOf(t.getId()))) {
								Utils.fillEntityDataStructures(
									entityControllers,
									e1e2PairCount,
									t.expand(2),
									controllerName
								);
							}
						}

						break;

					default:
						while (iter.hasMoreTraces()) {
							t = iter.nextTrace();

							Utils.fillEntityDataStructures(
								entityControllers,
								e1e2PairCount,
								t.expand(2),
								controllerName
							);
						}
				}

				t = null; // release memory
			}
		}

		iter = null; // release memory

		Set<Short> entities = new TreeSet<>(entityControllers.keySet());

		CodebaseManager.getInstance().writeAnalyserSimilarityMatrix(
			codebase.getName(),
			getAnalyserMatrixData(
				entities,
				e1e2PairCount,
				entityControllers
			)
		);

		return entities.size();
	}


	@RequestMapping(value = "/analysis", method = RequestMethod.POST)
	public ResponseEntity<AnalysisDto> getAnalysis(@RequestBody AnalysisDto analysis) throws IOException {
		logger.debug("getAnalysis");

		if (analysis.getGraph1().getCodebaseName() == null) { // no expert cut from frontend
			return new ResponseEntity<>(analysis, HttpStatus.OK);
		}

		Map<String, Set<Short>> graph1 = new HashMap<>();
		for (Cluster c : analysis.getGraph1().getClusters()) {
			graph1.put(c.getName(), c.getEntities());
		}

		Map<String, Set<Short>> graph2_CommonEntitiesOnly = new HashMap<>();
		for (Cluster c : analysis.getGraph2().getClusters()) {
			graph2_CommonEntitiesOnly.put(c.getName(), c.getEntities());
		}

		List<Short> entities = new ArrayList<>();
		List<Short> notSharedEntities = new ArrayList<>();

		for (Set<Short> l1 : graph1.values()) {
			for (short e1ID : l1) {
				boolean inBoth = false;

				for (Set<Short> l2 : graph2_CommonEntitiesOnly.values()) {
					if (l2.contains(e1ID)) {
						inBoth = true;
						break;
					}
				}

				if (inBoth)
					entities.add(e1ID);
				else {
					notSharedEntities.add(e1ID);
				}
			}				
		}

		// ------------------------------------------------------------------------------------------
		Map<String, Set<Short>> graph2_UnassignedInBigger = graphCopyOf(graph2_CommonEntitiesOnly);
		Map.Entry<String, Set<Short>> biggerClusterEntry = null;

		for (Map.Entry<String, Set<Short>> clusterEntry : graph2_UnassignedInBigger.entrySet()) {
			if (biggerClusterEntry == null)
				biggerClusterEntry = clusterEntry;

			else if (clusterEntry.getValue().size() > biggerClusterEntry.getValue().size())
				biggerClusterEntry = clusterEntry;
		}

		biggerClusterEntry.getValue().addAll(notSharedEntities);

		// ------------------------------------------------------------------------------------------
		Map<String, Set<Short>> graph2_UnassignedInNew = graphCopyOf(graph2_CommonEntitiesOnly);
		Set<Short> newClusterForUnassignedEntities = new HashSet<>(notSharedEntities);
		graph2_UnassignedInNew.put("newClusterForUnnasignedEntities", newClusterForUnassignedEntities);

		// ------------------------------------------------------------------------------------------
		Map<String, Set<Short>> graph2_UnassignedInSingletons = graphCopyOf(graph2_CommonEntitiesOnly);
		for (int i = 0; i < notSharedEntities.size(); i++) {
			Set<Short> clusterSingletonEntity = new HashSet<>();
			clusterSingletonEntity.add(notSharedEntities.get(i));
			graph2_UnassignedInSingletons.put("singletonCluster" + i, clusterSingletonEntity);
		}

		int truePositive = 0;
		int falsePositive = 0;
		int trueNegative = 0;
		int falseNegative = 0;

		for (int i = 0; i < entities.size(); i++) {
			for (int j = i+1; j < entities.size(); j++) {
				short e1ID = entities.get(i);
				short e2ID = entities.get(j);

				String e1ClusterG1 = "";
				String e2ClusterG1 = "";
				String e1ClusterG2 = "";
				String e2ClusterG2 = "";

				for (String cluster : graph1.keySet()) {
					if (graph1.get(cluster).contains(e1ID)) {
						e1ClusterG1 = cluster;
					}
					if (graph1.get(cluster).contains(e2ID)) {
						e2ClusterG1 = cluster;
					}
				}

				for (String cluster : graph2_CommonEntitiesOnly.keySet()) {
					if (graph2_CommonEntitiesOnly.get(cluster).contains(e1ID)) {
						e1ClusterG2 = cluster;
					}
					if (graph2_CommonEntitiesOnly.get(cluster).contains(e2ID)) {
						e2ClusterG2 = cluster;
					}
				}

				boolean sameClusterInGraph1 = false;
				if (e1ClusterG1.equals(e2ClusterG1))
					sameClusterInGraph1 = true;
				
				boolean sameClusterInGraph2 = false;
				if (e1ClusterG2.equals(e2ClusterG2))
					sameClusterInGraph2 = true;

				if (sameClusterInGraph1 && sameClusterInGraph2)
					truePositive++;
				if (sameClusterInGraph1 && !sameClusterInGraph2)
					falseNegative++;
				if (!sameClusterInGraph1 && sameClusterInGraph2)
					falsePositive++;
				if (!sameClusterInGraph1 && !sameClusterInGraph2)
					trueNegative++;

				if (sameClusterInGraph1 != sameClusterInGraph2) {
					String[] falsePair = new String[6];
					falsePair[0] = String.valueOf(e1ID);
					falsePair[1] = e1ClusterG1;
					falsePair[2] = e1ClusterG2;
					falsePair[3] = String.valueOf(e2ID);
					falsePair[4] = e2ClusterG1;
					falsePair[5] = e2ClusterG2;

					analysis.addFalsePair(falsePair);
				}
			}
		}

		analysis.setTruePositive(truePositive);
		analysis.setTrueNegative(trueNegative);
		analysis.setFalsePositive(falsePositive);
		analysis.setFalseNegative(falseNegative);

		float accuracy;
		float precision;
		float recall;
		float specificity;
		float fmeasure;

		if (truePositive == 0 && trueNegative == 0 && falsePositive == 0 && falseNegative == 0) { // no ExpertCut submitted
			accuracy = 0;
			precision = 0;
			recall = 0;
			specificity = 0;
			fmeasure = 0;
		}
		else {
			accuracy = (float)(truePositive + trueNegative) / (truePositive + trueNegative + falsePositive + falseNegative);
			accuracy = BigDecimal.valueOf(accuracy).setScale(2, RoundingMode.HALF_UP).floatValue();

			precision = (float)truePositive / (truePositive + falsePositive);
			precision = BigDecimal.valueOf(precision).setScale(2, RoundingMode.HALF_UP).floatValue();

			recall = (float)truePositive / (truePositive + falseNegative);
			recall = BigDecimal.valueOf(recall).setScale(2, RoundingMode.HALF_UP).floatValue();

			specificity = (float)trueNegative / (trueNegative + falsePositive);
			specificity = Float.isNaN(specificity) ? -1 : BigDecimal.valueOf(specificity).setScale(2, RoundingMode.HALF_UP).floatValue();

			fmeasure = 2*precision*recall / (precision + recall);
			fmeasure = BigDecimal.valueOf(fmeasure).setScale(2, RoundingMode.HALF_UP).floatValue();
		}

		analysis.setAccuracy(accuracy);
		analysis.setPrecision(precision);
		analysis.setRecall(recall);
		analysis.setSpecificity(specificity);
        analysis.setFmeasure(fmeasure);

        /*
        *******************************************
        ************ CALCULATE MOJO ***************
        *******************************************
        */
		double mojoValueCommonOnly = getMojoValue(
				graph2_CommonEntitiesOnly,
				graph1,
				graph2_CommonEntitiesOnly.values().stream().flatMap(Collection::stream).collect(Collectors.toSet())
		);
		double mojoValueUnassignedInBiggest = getMojoValue(
				graph2_UnassignedInBigger,
				graph1,
				graph2_UnassignedInBigger.values().stream().flatMap(Collection::stream).collect(Collectors.toSet())
		);
		double mojoValueUnassignedInNew = getMojoValue(
				graph2_UnassignedInNew,
				graph1,
				graph2_UnassignedInNew.values().stream().flatMap(Collection::stream).collect(Collectors.toSet())
		);
		double mojoValueUnassignedInSingletons = getMojoValue(
				graph2_UnassignedInSingletons,
				graph1,
				graph2_UnassignedInSingletons.values().stream().flatMap(Collection::stream).collect(Collectors.toSet())
		);

		analysis.setMojoCommon(mojoValueCommonOnly);
		analysis.setMojoBiggest(mojoValueUnassignedInBiggest);
		analysis.setMojoNew(mojoValueUnassignedInNew);
		analysis.setMojoSingletons(mojoValueUnassignedInSingletons);
		return new ResponseEntity<>(analysis, HttpStatus.OK);
	}

	private Map<String, Set<Short>> graphCopyOf(Map<String, Set<Short>> graph) {
		HashMap<String, Set<Short>> copy = new HashMap<>();
		for (Map.Entry<String, Set<Short>> entry : graph.entrySet()) {
			copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
		}

		return copy;
	}

	private double getMojoValue(
			Map<String, Set<Short>> graph1,
			Map<String, Set<Short>> graph2,
			Set<Short> entities
	) throws IOException
	{
		StringBuilder sbSource = new StringBuilder();
		for (Map.Entry<String, Set<Short>> clusterEntry : graph1.entrySet()) {
			String clusterName = clusterEntry.getKey();
			Set<Short> clusterEntities = clusterEntry.getValue();

			for (short entityID : clusterEntities) {
				if (entities.contains(entityID)) { // entity present in both graphs
					sbSource.append("contain " + clusterName + " " + entityID + "\n");
				}
			}
		}

		StringBuilder sbTarget = new StringBuilder();
		for (Map.Entry<String, Set<Short>> clusterEntry : graph2.entrySet()) {
			String clusterName = clusterEntry.getKey();
			Set<Short> clusterEntities = clusterEntry.getValue();
			for (short entityID : clusterEntities) {
				if (entities.contains(entityID)) { // entity present in both graphs
					sbTarget.append("contain " + clusterName + " " + entityID + "\n");
				}
			}
		}

		String distrSrcPath = MOJO_RESOURCES_PATH + "distrSrc.rsf";
		String distrTargetPath = MOJO_RESOURCES_PATH + "distrTarget.rsf";

		FileWriter srcFileWriter = new FileWriter(new File(distrSrcPath));
		srcFileWriter.write(sbSource.toString());
		srcFileWriter.close();

		FileWriter targetFileWriter = new FileWriter(new File(distrTargetPath));
		targetFileWriter.write(sbTarget.toString());
		targetFileWriter.close();

		return new MoJo().executeMojo(new String[]{
				distrSrcPath,
				distrTargetPath,
				"-fm"
		});
	}
}