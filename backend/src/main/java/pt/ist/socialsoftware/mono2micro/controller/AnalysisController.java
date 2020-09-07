package pt.ist.socialsoftware.mono2micro.controller;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

			File analyserResultPath = new File(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json");
			if (!analyserResultPath.exists()) {
				codebaseManager.writeAnalyserResults(codebaseName, new HashMap());
			}

			Codebase codebase = CodebaseManager.getInstance().getCodebaseWithFields(
				codebaseName,
				new HashSet<String>() {{ add("analysisType"); add("name"); add("profiles"); add("datafilePath"); }}
			);

			int numberOfEntitiesPresentInCollection;
			HashMap<String, ControllerDto> datafileJSON = null;

			if (codebase.isStatic()) {
				datafileJSON = CodebaseManager.getInstance().getDatafile(codebase);

				numberOfEntitiesPresentInCollection = createStaticAnalyserSimilarityMatrix(
					codebase,
					analyser,
					datafileJSON
				);

			} else {
				numberOfEntitiesPresentInCollection = createDynamicAnalyserSimilarityMatrix(codebase, analyser);
			}

			System.out.println(codebaseName + ": " + numberOfEntitiesPresentInCollection);

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

			int maxRequests = analyser.getRequestLimit();
			int newRequestsCount = 0;
			int count = 0;
			int bufferedDataCount = 0;
			HashMap<String, CutInfoDto> analyserJSON = codebaseManager.getAnalyserResults(codebaseName);
			File analyserCutsPath = new File(CODEBASES_PATH + codebaseName + "/analyser/cuts/");
			File[] files = analyserCutsPath.listFiles();
			int total = files.length;

			for (File file : files) {

				String filename = FilenameUtils.getBaseName(file.getName());

				count++;

                if (analyserJSON.containsKey(filename)) {
                    System.out.println(filename + " already analysed. " + count + "/" + total);
                    continue;
                }

				Graph graph = new Graph();
				graph.setCodebaseName(codebaseName);

				HashMap<String, HashMap<String, Set<String>>> analyserCut = codebaseManager.getAnalyserCut(codebaseName, filename);

				Set<String> clusterIDs = analyserCut.get("clusters").keySet();

				for (String clusterId : clusterIDs) {
					Set<String> entities = analyserCut.get("clusters").get(clusterId);
					Cluster cluster = new Cluster(clusterId, entities);

					graph.addCluster(cluster);
				}

				if (codebase.isStatic()) {
					graph.calculateMetricsAnalyser(analyser.getProfiles(), datafileJSON);

				} else {
					graph.calculateDynamicMetricsAnalyser(
						analyser.getProfiles(),
						analyser.getTracesMaxLimit(),
						analyser.getTypeOfTraces()
					);
				}

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
				
				analyserResult.setComplexity(graph.getComplexity());
				analyserResult.setCohesion(graph.getCohesion());
				analyserResult.setCoupling(graph.getCoupling());

				analyserResult.setMaxClusterSize(graph.maxClusterSize());

				String[] similarityWeights = filename.split(",");
				analyserResult.setAccessWeight(Float.parseFloat(similarityWeights[0]));
				analyserResult.setWriteWeight(Float.parseFloat(similarityWeights[1]));
				analyserResult.setReadWeight(Float.parseFloat(similarityWeights[2]));
				analyserResult.setSequenceWeight(Float.parseFloat(similarityWeights[3]));
				analyserResult.setNumberClusters(Float.parseFloat(similarityWeights[4]));

				CutInfoDto analyserResultJSON = new CutInfoDto();
				analyserResultJSON.setAnalyserResultDto(analyserResult);

				HashMap<String, Float> controllerComplexities = new HashMap<>();
				for (Controller controller : graph.getControllers()) {
					controllerComplexities.put(controller.getName(), controller.getComplexity());
				}
				analyserResultJSON.setControllerComplexities(controllerComplexities);

				analyserJSON.put(filename, analyserResultJSON);

				newRequestsCount++;
				bufferedDataCount++;

				System.out.println("NEW: " + filename + " : " + count + "/" + total);
				if (newRequestsCount == maxRequests)
					break;

				if (bufferedDataCount >= 100) {
					// save buffered data (replace whole file, not appending)
					codebaseManager.writeAnalyserResults(codebaseName, analyserJSON);
					bufferedDataCount = 0;
				}
			}

            codebaseManager.writeAnalyserResults(codebaseName, analyserJSON);
			System.out.println("Analyser Complete");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private JSONObject getAnalyserMatrixData(
		List<String> entitiesList,
		Map<String,Integer> e1e2PairCount,
		Map<String,List<Pair<String,String>>> entityControllers
	) throws JSONException {

		JSONArray similarityMatrix = new JSONArray();
		JSONObject matrixData = new JSONObject();

		int maxNumberOfPairs = Utils.getMaxNumberOfPairs(e1e2PairCount);

		for (int i = 0; i < entitiesList.size(); i++) {
			String e1 = entitiesList.get(i);
			JSONArray matrixRow = new JSONArray();

			for (int j = 0; j < entitiesList.size(); j++) {
				String e2 = entitiesList.get(j);

				if (e1.equals(e2)) {
					JSONArray metric = new JSONArray();
					metric.put(1);
					metric.put(1);
					metric.put(1);
					metric.put(1);
					matrixRow.put(metric);
					continue;
				}

				float[] metrics = Utils.calculateSimilarityMatrixMetrics(
					entityControllers,
					e1e2PairCount,
					e1,
					e2,
					maxNumberOfPairs
				);

				JSONArray metric = new JSONArray();
				metric.put(metrics[0]);
				metric.put(metrics[1]);
				metric.put(metrics[2]);
				metric.put(metrics[3]);

				matrixRow.put(metric);
			}
			similarityMatrix.put(matrixRow);
		}
		matrixData.put("matrix", similarityMatrix);
		matrixData.put("entities", entitiesList);
		matrixData.put("linkageType", "average");

		return matrixData;
	}
	
	private int createStaticAnalyserSimilarityMatrix(
		Codebase codebase,
		AnalyserDto analyser,
		HashMap<String, ControllerDto> datafileJSON
	) throws IOException, JSONException
	{
		Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();
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

		List<String> entitiesList = new ArrayList<>(entityControllers.keySet());
		Collections.sort(entitiesList);

		CodebaseManager.getInstance().writeAnalyserSimilarityMatrix(
			codebase.getName(),
			getAnalyserMatrixData(
				entitiesList,
				e1e2PairCount,
				entityControllers
			)
		);

		return entitiesList.size();
	}

	private int createDynamicAnalyserSimilarityMatrix(
		Codebase codebase,
		AnalyserDto analyser
	) throws IOException, JSONException
	{
		Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();
		Map<String,Integer> e1e2PairCount = new HashMap<>();

		ControllerTracesIterator iter;
		TraceDto t;

		for (String profile : analyser.getProfiles()) {
			for (String controllerName : codebase.getProfile(profile)) {
				iter = new ControllerTracesIterator(
					codebase.getDatafilePath(),
					controllerName,
					analyser.getTracesMaxLimit()
				);

				switch (analyser.getTypeOfTraces()) {
					case LONGEST:
						// FIXME return accesses of longest trace instead of the trace itself
						t = iter.getLongestTrace();

						if (t != null) {
							Utils.fillEntityDataStructures(
								entityControllers,
								e1e2PairCount,
								t.getAccesses(),
								controllerName
							);
						}

						break;

					case WITH_MORE_DIFFERENT_ACCESSES:
						// FIXME return accesses of longest trace instead of the trace itself
						t = iter.getTraceWithMoreDifferentAccesses();

						if (t != null) {
							Utils.fillEntityDataStructures(
								entityControllers,
								e1e2PairCount,
								t.getAccesses(),
								controllerName
							);
						}

						break;

					case REPRESENTATIVE:
						Set<String> tracesIds = iter.getRepresentativeTraces();
						iter.reset();

						while (iter.hasMoreTraces()) {
							t = iter.nextTrace();

							if (tracesIds.contains(String.valueOf(t.getId()))) {
								Utils.fillEntityDataStructures(
									entityControllers,
									e1e2PairCount,
									t.getAccesses(),
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
								t.getAccesses(),
								controllerName
							);
						}
				}

				t = null; // release memory
			}
		}

		iter = null; // release memory

		List<String> entitiesList = new ArrayList<>(entityControllers.keySet());
		Collections.sort(entitiesList);

		CodebaseManager.getInstance().writeAnalyserSimilarityMatrix(
			codebase.getName(),
			getAnalyserMatrixData(
				entitiesList,
				e1e2PairCount,
				entityControllers
			)
		);

		return entitiesList.size();
	}


	@RequestMapping(value = "/analysis", method = RequestMethod.POST)
	public ResponseEntity<AnalysisDto> getAnalysis(@RequestBody AnalysisDto analysis) throws IOException {
		logger.debug("getAnalysis");

		if (analysis.getGraph1().getCodebaseName() == null) { // expert cut from frontend
			analysis.setAccuracy(0);
			analysis.setPrecision(0);
			analysis.setRecall(0);
			analysis.setSpecificity(0);
			analysis.setFmeasure(0);
			return new ResponseEntity<>(analysis, HttpStatus.OK);
		}

		Map<String, Set<String>> graph1 = new HashMap<>();
		for (Cluster c : analysis.getGraph1().getClusters()) {
			graph1.put(c.getName(), c.getEntities());
		}

		Map<String, Set<String>> graph2_CommonEntitiesOnly = new HashMap<>();
		for (Cluster c : analysis.getGraph2().getClusters()) {
			graph2_CommonEntitiesOnly.put(c.getName(), c.getEntities());
		}

		List<String> entities = new ArrayList<>();
		List<String> notSharedEntities = new ArrayList<>();

		for (Set<String> l1 : graph1.values()) {
			for (String e1 : l1) {
				boolean inBoth = false;

				for (Set<String> l2 : graph2_CommonEntitiesOnly.values()) {
					if (l2.contains(e1)) {
						inBoth = true;
						break;
					}
				}

				if (inBoth)
					entities.add(e1);
				else {
					notSharedEntities.add(e1);
				}
			}				
		}

		// ------------------------------------------------------------------------------------------
		Map<String, Set<String>> graph2_UnassignedInBigger = graphCopyOf(graph2_CommonEntitiesOnly);
		Map.Entry<String, Set<String>> biggerClusterEntry = null;
		for (Map.Entry<String, Set<String>> clusterEntry : graph2_UnassignedInBigger.entrySet()) {
			if (biggerClusterEntry == null)
				biggerClusterEntry = clusterEntry;

			else if (clusterEntry.getValue().size() > biggerClusterEntry.getValue().size())
				biggerClusterEntry = clusterEntry;
		}
		biggerClusterEntry.getValue().addAll(notSharedEntities);

		// ------------------------------------------------------------------------------------------
		Map<String, Set<String>> graph2_UnassignedInNew = graphCopyOf(graph2_CommonEntitiesOnly);
		Set<String> newClusterForUnassignedEntities = new HashSet<>(notSharedEntities);
		graph2_UnassignedInNew.put("newClusterForUnnasignedEntities", newClusterForUnassignedEntities);

		// ------------------------------------------------------------------------------------------
		Map<String, Set<String>> graph2_UnassignedInSingletons = graphCopyOf(graph2_CommonEntitiesOnly);
		for (int i = 0; i < notSharedEntities.size(); i++) {
			Set<String> clusterSingletonEntity = new HashSet<>();
			clusterSingletonEntity.add(notSharedEntities.get(i));
			graph2_UnassignedInSingletons.put("singletonCluster" + i, clusterSingletonEntity);
		}

		int truePositive = 0;
		int falsePositive = 0;
		int trueNegative = 0;
		int falseNegative = 0;

		for (int i = 0; i < entities.size(); i++) {
			for (int j = i+1; j < entities.size(); j++) {
				String e1 = entities.get(i);
				String e2 = entities.get(j);

				String e1ClusterG1 = "";
				String e2ClusterG1 = "";
				String e1ClusterG2 = "";
				String e2ClusterG2 = "";

				for (String cluster : graph1.keySet()) {
					if (graph1.get(cluster).contains(e1)) {
						e1ClusterG1 = cluster;
					}
					if (graph1.get(cluster).contains(e2)) {
						e2ClusterG1 = cluster;
					}
				}

				for (String cluster : graph2_CommonEntitiesOnly.keySet()) {
					if (graph2_CommonEntitiesOnly.get(cluster).contains(e1)) {
						e1ClusterG2 = cluster;
					}
					if (graph2_CommonEntitiesOnly.get(cluster).contains(e2)) {
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
					falsePair[0] = e1;
					falsePair[1] = e1ClusterG1;
					falsePair[2] = e1ClusterG2;
					falsePair[3] = e2;
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

	private Map<String, Set<String>> graphCopyOf(Map<String, Set<String>> graph) {
		HashMap<String, Set<String>> copy = new HashMap<>();
		for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
			copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
		}

		return copy;
	}

	private double getMojoValue(
			Map<String, Set<String>> graph1,
			Map<String, Set<String>> graph2,
			Set<String> entities
	) throws IOException
	{
		StringBuilder sbSource = new StringBuilder();
		for (Map.Entry<String, Set<String>> clusterEntry : graph1.entrySet()) {
			String clusterName = clusterEntry.getKey();
			Set<String> clusterEntities = clusterEntry.getValue();
			for (String entity : clusterEntities) {
				if (entities.contains(entity)) { // entity present in both graphs
					sbSource.append("contain " + clusterName + " " + entity + "\n");
				}
			}
		}

		StringBuilder sbTarget = new StringBuilder();
		for (Map.Entry<String, Set<String>> clusterEntry : graph2.entrySet()) {
			String clusterName = clusterEntry.getKey();
			Set<String> clusterEntities = clusterEntry.getValue();
			for (String entity : clusterEntities) {
				if (entities.contains(entity)) { // entity present in both graphs
					sbTarget.append("contain " + clusterName + " " + entity + "\n");
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