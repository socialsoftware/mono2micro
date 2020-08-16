package pt.ist.socialsoftware.mono2micro.controller;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.PYTHON;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.RESOURCES_PATH;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.dto.*;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.ControllerTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Pair;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

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

			Codebase codebase = CodebaseManager.getInstance().getCodebase(codebaseName);

			List<String> entitiesList;
			HashMap<String, ControllerDto> datafileJSON = null;

			if (codebase.isStatic()) {
				datafileJSON = CodebaseManager.getInstance().getDatafile(codebase);
				// returning entitiesList by convenience
				entitiesList = createStaticAnalyserSimilarityMatrix(codebaseName, analyser, datafileJSON);

			} else {
				entitiesList = createDynamicAnalyserSimilarityMatrix(codebase, analyser);
			}
			// open codebase's datafile Json only once at the beginning of the analyser
			int numberOfEntitiesPresentInCollection = entitiesList.size();

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

				JSONObject analyserCut = codebaseManager.getAnalyserCut(codebaseName, filename);
		
				Iterator<String> clusters = analyserCut.getJSONObject("clusters").keys();

				while(clusters.hasNext()) {
					String clusterId = clusters.next();
					JSONArray entities = analyserCut.getJSONObject("clusters").getJSONArray(clusterId);
					Cluster cluster = new Cluster(clusterId);

					for (int i = 0; i < entities.length(); i++) {
						cluster.addEntity(new Entity(entities.getString(i)));
					}

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
	
	private List<String> createStaticAnalyserSimilarityMatrix(
		String codebaseName,
		AnalyserDto analyser,
		HashMap<String, ControllerDto> datafileJSON
	) throws IOException, JSONException
	{
		Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();
		Map<String,Integer> e1e2PairCount = new HashMap<>();

		Codebase codebase = codebaseManager.getCodebase(codebaseName);

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
			codebaseName,
			getAnalyserMatrixData(
				entitiesList,
				e1e2PairCount,
				entityControllers
			)
		);

		return entitiesList;
	}

	private List<String> createDynamicAnalyserSimilarityMatrix(
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

		return entitiesList;
	}


	@RequestMapping(value = "/analysis", method = RequestMethod.POST)
	public ResponseEntity<AnalysisDto> getAnalysis(@RequestBody AnalysisDto analysis) {
		logger.debug("getAnalysis");
		
		Map<String,List<String>> graph1 = new HashMap<>();
		for (Cluster c : analysis.getGraph1().getClusters()) {
			graph1.put(c.getName(), c.getEntityNames());
		}

		Map<String,List<String>> graph2 = new HashMap<>();
		for (Cluster c : analysis.getGraph2().getClusters()) {
			graph2.put(c.getName(), c.getEntityNames());
		}

		List<String> entities = new ArrayList<>();
		for (List<String> l1 : graph1.values()) {
			for (String e1 : l1) {
				boolean inBoth = false;
				for (List<String> l2 : graph2.values()) {
					if (l2.contains(e1)) {
						inBoth = true;
						break;
					}
				}
				if (inBoth)
					entities.add(e1);
			}				
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

				for (String cluster : graph2.keySet()) {
					if (graph2.get(cluster).contains(e1)) {
						e1ClusterG2 = cluster;
					}
					if (graph2.get(cluster).contains(e2)) {
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
		
		return new ResponseEntity<>(analysis, HttpStatus.OK);
	}
}