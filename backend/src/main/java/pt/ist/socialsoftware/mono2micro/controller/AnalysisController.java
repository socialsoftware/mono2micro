package pt.ist.socialsoftware.mono2micro.controller;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.PYTHON;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.RESOURCES_PATH;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Entity;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.dto.AnalyserDto;
import pt.ist.socialsoftware.mono2micro.dto.AnalyserResultDto;
import pt.ist.socialsoftware.mono2micro.dto.AnalysisDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

@RestController
@RequestMapping(value = "/mono2micro")
public class AnalysisController {

    private static Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    private CodebaseManager codebaseManager = CodebaseManager.getInstance();



	@RequestMapping(value = "/importAnalyserResults", method = RequestMethod.GET)
	public ResponseEntity<String> importAnalyserResults(@RequestParam String codebaseName) {
		logger.debug("importAnalyserResults");
		
		try {
			return new ResponseEntity<>(codebaseManager.getAnalyserResults(codebaseName).toString(), HttpStatus.OK);
		} catch (IOException | JSONException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/codebase/{codebaseName}/analyser", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> analyser(@PathVariable String codebaseName, @RequestBody AnalyserDto analyser) {
		logger.debug("analyser");

		try {

			JSONObject datafileJSON = codebaseManager.getDatafile(codebaseName);
			Codebase codebase = codebaseManager.getCodebase(codebaseName);

			File analyserPath = new File(CODEBASES_PATH + codebaseName + "/analyser/cuts/");
			if (!analyserPath.exists()) {
				analyserPath.mkdirs();
			}

			File analyserResultPath = new File(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json");
			if (!analyserResultPath.exists()) {
				codebaseManager.writeAnalyserResults(codebaseName, new JSONObject());
			}

			createAnalyserSimilarityMatrix(codebaseName, analyser);

			/*Runtime r = Runtime.getRuntime();
			String pythonScriptPath = RESOURCES_PATH + "analyser.py";
			String[] cmd = new String[4];
			cmd[0] = PYTHON;
			cmd[1] = pythonScriptPath;
			cmd[2] = CODEBASES_PATH;
			cmd[3] = codebaseName;
			Process p = r.exec(cmd);
			
			p.waitFor();

			BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while((line = bfr.readLine()) != null) 
			{
				System.out.println(line);
			}*/

			int maxRequests = 1700;
			int requestCount = 0;
			File analyserCutsPath = new File(CODEBASES_PATH + codebaseName + "/analyser/cuts/");
			File[] files = analyserCutsPath.listFiles();
			for (File file : files) {
				String filename = FilenameUtils.getBaseName(file.getName());

				JSONObject analyserJSON = codebaseManager.getAnalyserResults(codebaseName);
				if (analyserJSON.has(filename))
					continue;

				Graph graph = new Graph();

				for (String profile : analyser.getProfiles()) {
					for (String controllerName : codebase.getProfile(profile)) {
						Controller controller = new Controller(controllerName);
						graph.addController(controller);

						JSONArray entities = datafileJSON.getJSONArray(controllerName);
						for (int i = 0; i < entities.length(); i++) {
							JSONArray entityArray = entities.getJSONArray(i);
							String entity = entityArray.getString(0);
							String mode = entityArray.getString(1);
							
							controller.addEntity(entity, mode);
							controller.addEntitySeq(entity, mode);
						}
					}
				}

				JSONObject analyserCut = codebaseManager.getAnalyserCut(codebaseName, filename);
		
				Iterator<String> clusters = analyserCut.getJSONObject("clusters").sortedKeys();
				ArrayList<Integer> clusterIds = new ArrayList<>();
		
				while(clusters.hasNext()) {
					clusterIds.add(Integer.parseInt(clusters.next()));
				}
				Collections.sort(clusterIds);
				for (Integer id : clusterIds) {
					String clusterId = String.valueOf(id);
					JSONArray entities = analyserCut.getJSONObject("clusters").getJSONArray(clusterId);
					Cluster cluster = new Cluster("Cluster" + clusterId);
					for (int i = 0; i < entities.length(); i++) {
						Entity entity = new Entity(entities.getString(i));
						cluster.addEntity(entity);
					}
					graph.addCluster(cluster);
				}

				graph.calculateMetrics();

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

				String[] similarityWeights = filename.split(",");
				analyserResult.setAccessWeight(Float.parseFloat(similarityWeights[0]));
				analyserResult.setWriteWeight(Float.parseFloat(similarityWeights[1]));
				analyserResult.setReadWeight(Float.parseFloat(similarityWeights[2]));
				analyserResult.setSequenceWeight(Float.parseFloat(similarityWeights[3]));
				analyserResult.setNumberClusters(Float.parseFloat(similarityWeights[4]));

				JSONObject analyserResultJSON = new JSONObject(analyserResult);

				analyserJSON = codebaseManager.getAnalyserResults(codebaseName);
				analyserJSON.put(filename, analyserResultJSON);
				codebaseManager.writeAnalyserResults(codebaseName, analyserJSON);

				requestCount++;
				if (requestCount == maxRequests)
					break;
			}


		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	void createAnalyserSimilarityMatrix(String codebaseName, AnalyserDto analyser) throws IOException, JSONException {
		Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();
		Map<String,Integer> e1e2PairCount = new HashMap<>();
		JSONArray similarityMatrix = new JSONArray();
		JSONObject matrixData = new JSONObject();

		JSONObject datafileJSON = codebaseManager.getDatafile(codebaseName);
		Codebase codebase = codebaseManager.getCodebase(codebaseName);

		for (String profile : analyser.getProfiles()) {
			for (String controllerName : codebase.getProfile(profile)) {
				JSONArray entities = datafileJSON.getJSONArray(controllerName);
				for (int i = 0; i < entities.length(); i++) {
					JSONArray entityArray = entities.getJSONArray(i);
					String entity = entityArray.getString(0);
					String mode = entityArray.getString(1);

					if (entityControllers.containsKey(entity)) {
						boolean containsController = false;
						for (Pair<String,String> controllerPair : entityControllers.get(entity)) {
							if (controllerPair.getFirst().equals(controllerName)) {
								containsController = true;
								if (!controllerPair.getSecond().contains(mode))
									controllerPair.setSecond("RW");
								break;
							}
						}
						if (!containsController) {
							entityControllers.get(entity).add(new Pair<String,String>(controllerName,mode));
						}
					} else {
						List<Pair<String,String>> controllersPairs = new ArrayList<>();
						controllersPairs.add(new Pair<String,String>(controllerName,mode));
						entityControllers.put(entity, controllersPairs);
					}

					if (i < entities.length() - 1) {
						JSONArray nextEntityArray = entities.getJSONArray(i+1);
						String nextEntity = nextEntityArray.getString(0);

						if (!entity.equals(nextEntity)) {
							String e1e2 = entity + "->" + nextEntity;
							String e2e1 = nextEntity + "->" + entity;

							int count = e1e2PairCount.containsKey(e1e2) ? e1e2PairCount.get(e1e2) : 0;
							e1e2PairCount.put(e1e2, count + 1);

							count = e1e2PairCount.containsKey(e2e1) ? e1e2PairCount.get(e2e1) : 0;
							e1e2PairCount.put(e2e1, count + 1);
						}
					}
				}
			}
		}

		List<String> entitiesList = new ArrayList<String>(entityControllers.keySet());
		Collections.sort(entitiesList);

		int maxNumberOfPairs = Collections.max(e1e2PairCount.values());

		for (int i = 0; i < entitiesList.size(); i++) {
			String e1 = entitiesList.get(i);
			JSONArray matrixRow = new JSONArray();
			for (int j = 0; j < entitiesList.size(); j++) {
				String e2 = entitiesList.get(j);
				String e1e2 = e1 + "->" + e2;

				if (e1.equals(e2)) {
					JSONArray metric = new JSONArray();
					metric.put(1);
					metric.put(1);
					metric.put(1);
					metric.put(1);
					matrixRow.put(metric);
					continue;
				}

				float inCommon = 0;
				float inCommonW = 0;
				float inCommonR = 0;
				float e1ControllersW = 0;
				float e1ControllersR = 0;
				for (Pair<String,String> e1Controller : entityControllers.get(e1)) {
					for (Pair<String,String> e2Controller : entityControllers.get(e2)) {
						if (e1Controller.getFirst().equals(e2Controller.getFirst()))
							inCommon++;
						if (e1Controller.getFirst().equals(e2Controller.getFirst()) && e1Controller.getSecond().contains("W") && e2Controller.getSecond().contains("W"))
							inCommonW++;
						if (e1Controller.getFirst().equals(e2Controller.getFirst()) && e1Controller.getSecond().contains("R") && e2Controller.getSecond().contains("R"))
							inCommonR++;
					}
					if (e1Controller.getSecond().contains("W"))
						e1ControllersW++;
					if (e1Controller.getSecond().contains("R"))
						e1ControllersR++;
				}

				float accessMetric = inCommon / entityControllers.get(e1).size();
				float writeMetric = e1ControllersW == 0 ? 0 : inCommonW / e1ControllersW;
				float readMetric = e1ControllersR == 0 ? 0 : inCommonR / e1ControllersR;

				float e1e2Count = e1e2PairCount.containsKey(e1e2) ? e1e2PairCount.get(e1e2) : 0;
				float sequenceMetric = e1e2Count / maxNumberOfPairs;

				JSONArray metric = new JSONArray();
				metric.put(accessMetric);
				metric.put(writeMetric);
				metric.put(readMetric);
				metric.put(sequenceMetric);
				
				matrixRow.put(metric);
			}
			similarityMatrix.put(matrixRow);
		}
		matrixData.put("matrix", similarityMatrix);
		matrixData.put("entities", entitiesList);
		matrixData.put("linkageType", "average");

		CodebaseManager.getInstance().writeAnalyserSimilarityMatrix(codebaseName, matrixData);
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

		float accuracy = (float)(truePositive + trueNegative) / (truePositive + trueNegative + falsePositive + falseNegative);
		accuracy = BigDecimal.valueOf(accuracy).setScale(2, RoundingMode.HALF_UP).floatValue();
		float precision = (float)truePositive / (truePositive + falsePositive);
		precision = BigDecimal.valueOf(precision).setScale(2, RoundingMode.HALF_UP).floatValue();
		float recall = (float)truePositive / (truePositive + falseNegative);
		recall = BigDecimal.valueOf(recall).setScale(2, RoundingMode.HALF_UP).floatValue();
		float specificity = (float)trueNegative / (trueNegative + falsePositive);
		specificity = Float.isNaN(specificity) ? -1 : BigDecimal.valueOf(specificity).setScale(2, RoundingMode.HALF_UP).floatValue();
		float fmeasure = 2*precision*recall / (precision + recall);
		fmeasure = BigDecimal.valueOf(fmeasure).setScale(2, RoundingMode.HALF_UP).floatValue();
		analysis.setAccuracy(accuracy);
		analysis.setPrecision(precision);
		analysis.setRecall(recall);
		analysis.setSpecificity(specificity);
        analysis.setFmeasure(fmeasure);
		
		return new ResponseEntity<>(analysis, HttpStatus.OK);
	}
}