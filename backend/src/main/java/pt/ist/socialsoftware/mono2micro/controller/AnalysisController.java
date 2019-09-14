package pt.ist.socialsoftware.mono2micro.controller;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.dto.AnalyserDto;
import pt.ist.socialsoftware.mono2micro.dto.AnalysisDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Pair;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_FOLDER;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.RESOURCES_PATH;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.PYTHON;

@RestController
@RequestMapping(value = "/mono2micro")
public class AnalysisController {

    private static Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    private CodebaseManager codebaseManager = CodebaseManager.getInstance();



	@RequestMapping(value = "/analyser", method = RequestMethod.POST)
	public ResponseEntity<AnalyserDto> analyser(@RequestBody AnalyserDto analyser) {
		logger.debug("analyser");

		Codebase codebase = codebaseManager.getCodebase(analyser.getCodebaseName());
		
		try {
			Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();
			Map<String,Integer> e1e2PairCount = new HashMap<>();
			JSONArray similarityMatrix = new JSONArray();
			JSONObject dendrogramData = new JSONObject();


			//read datafile
			InputStream is = new FileInputStream(CODEBASES_FOLDER + codebase.getName() + ".txt");
			JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
			is.close();

			for (String profile : codebase.getProfiles().keySet()) {
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

								if (e1e2PairCount.containsKey(e1e2)) {
									e1e2PairCount.put(e1e2, e1e2PairCount.get(e1e2) + 1);
								} else {
									int count = e1e2PairCount.containsKey(e2e1) ? e1e2PairCount.get(e2e1) : 0;
									e1e2PairCount.put(e2e1, count + 1);
								}
							}
						}
					}
				}
			}

			List<String> entitiesList = new ArrayList<String>(entityControllers.keySet());
			Collections.sort(entitiesList);

			int maxNumberOfPairs = Collections.max(e1e2PairCount.values());

			JSONArray seq1SimilarityMatrix = new JSONArray();
			JSONArray seq2SimilarityMatrix = new JSONArray();
			for (int i = 0; i < entitiesList.size(); i++) {
				String e1 = entitiesList.get(i);
				JSONArray seq1MatrixAux = new JSONArray();
				JSONArray seq2MatrixAux = new JSONArray();
				for (int j = 0; j < entitiesList.size(); j++) {
					String e2 = entitiesList.get(j);
					if (e1.equals(e2)) {
						seq1MatrixAux.put(new Float(1));
						seq2MatrixAux.put(new Float(1));
					} else {
						String e1e2 = e1 + "->" + e2;
						String e2e1 = e2 + "->" + e1;
						float e1e2Count = e1e2PairCount.containsKey(e1e2) ? e1e2PairCount.get(e1e2) : 0;
						float e2e1Count = e1e2PairCount.containsKey(e2e1) ? e1e2PairCount.get(e2e1) : 0;

						seq1MatrixAux.put(new Float(e1e2Count + e2e1Count));
						seq2MatrixAux.put(new Float(e1e2Count + e2e1Count));
					}
				}

				List<Float> seq2List = new ArrayList<>();
				for (int k = 0; k < seq2MatrixAux.length(); k++) {
					seq2List.add((float)seq2MatrixAux.get(k));
				}

				float seq2Max = Collections.max(seq2List);

				for (int j = 0; j < entitiesList.size(); j++) {
					if (!entitiesList.get(j).equals(e1)) {
						seq1MatrixAux.put(j, new Float(((float)seq1MatrixAux.get(j)) / maxNumberOfPairs));
						seq2MatrixAux.put(j, new Float(((float)seq2MatrixAux.get(j)) / seq2Max));
					}
				}

				seq1SimilarityMatrix.put(seq1MatrixAux);
				seq2SimilarityMatrix.put(seq2MatrixAux);
			}

			for (int i = 0; i < entitiesList.size(); i++) {
				String e1 = entitiesList.get(i);
				JSONArray matrixAux = new JSONArray();
				for (int j = 0; j < entitiesList.size(); j++) {
					String e2 = entitiesList.get(j);
					float inCommon = 0;
					float inCommonW = 0;
					float inCommonR = 0;
					float e1ControllersW = 0;
					float e1ControllersR = 0;
					for (Pair<String,String> p1 : entityControllers.get(e1)) {
						for (Pair<String,String> p2 : entityControllers.get(e2)) {
							if (p1.getFirst().equals(p2.getFirst()))
								inCommon++;
							if (p1.getFirst().equals(p2.getFirst()) && p1.getSecond().contains("W") && p2.getSecond().contains("W"))
								inCommonW++;
							if (p1.getFirst().equals(p2.getFirst()) && p1.getSecond().equals("R") && p2.getSecond().equals("R"))
								inCommonR++;
						}
						if (p1.getSecond().contains("W"))
							e1ControllersW++;
						if (p1.getSecond().equals("R"))
							e1ControllersR++;
					}

					float accessMetric = inCommon / entityControllers.get(e1).size();
					float writeMetric = e1ControllersW == 0 ? 0 : inCommonW / e1ControllersW;
					float readMetric = e1ControllersR == 0 ? 0 : inCommonR / e1ControllersR;
					float sequence1Metric = (float) seq1SimilarityMatrix.getJSONArray(i).get(j);
					float sequence2Metric = (float) seq2SimilarityMatrix.getJSONArray(i).get(j);
					float metric = accessMetric * analyser.getAccessWeight() / 100 + 
									writeMetric * analyser.getWriteWeight() / 100 +
									readMetric * analyser.getReadWeight() / 100 +
									sequence1Metric * analyser.getSequence1Weight() / 100 +
									sequence2Metric * analyser.getSequence2Weight() / 100;
					matrixAux.put(metric);
				}
				similarityMatrix.put(matrixAux);
			}
			dendrogramData.put("matrix", similarityMatrix);
			dendrogramData.put("entities", entitiesList);

			String matrixFilename = "matrix" + analyser.getAccessWeight() +
										analyser.getWriteWeight() +
										analyser.getReadWeight() +
										analyser.getSequence1Weight() +
										analyser.getSequence2Weight() +
										analyser.getNumberClusters() + ".json";
			try (FileWriter file = new FileWriter(matrixFilename)){
				file.write(dendrogramData.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			// run python script with clustering algorithm
			Runtime r = Runtime.getRuntime();
			String pythonScriptPath = RESOURCES_PATH + "analyser_dendrogram.py";
			String[] cmd = new String[4];
			cmd[0] = PYTHON;
			cmd[1] = pythonScriptPath;
			cmd[2] = matrixFilename;
			cmd[3] = Float.toString(analyser.getNumberClusters());
			
			Process p = r.exec(cmd);

			p.waitFor();

			String clusterFilename = "cluster" + analyser.getAccessWeight() +
										analyser.getWriteWeight() +
										analyser.getReadWeight() +
										analyser.getSequence1Weight() +
										analyser.getSequence2Weight() +
										analyser.getNumberClusters() + ".txt";
			is = new FileInputStream(clusterFilename);
			JSONObject json = new JSONObject(IOUtils.toString(is, "UTF-8"));

			Iterator<String> clusters = json.keys();
			Map<String,List<String>> graph2 = new HashMap<>();

			while(clusters.hasNext()) {
				String clusterName = clusters.next();
				JSONArray entities = json.getJSONArray(clusterName);
				List<String> clusterEntities = new ArrayList<>();
				for (int i = 0; i < entities.length(); i++) {
					clusterEntities.add(entities.getString(i));
				}
				graph2.put(clusterName, clusterEntities);
			}
			is.close();
			Files.deleteIfExists(Paths.get(matrixFilename));
			Files.deleteIfExists(Paths.get(clusterFilename));

			Map<String,List<String>> graph1 = new HashMap<>();
			for (Cluster c : analyser.getExpert().getClusters()) {
				graph1.put(c.getName(), c.getEntityNames());
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
				}
			}

			float accuracy = (float)(truePositive + trueNegative) / (truePositive + trueNegative + falsePositive + falseNegative);
			float precision = (float)truePositive / (truePositive + falsePositive);
			float recall = (float)truePositive / (truePositive + falseNegative);
			float specificity = (float)trueNegative / (trueNegative + falsePositive);
			float fmeasure = 2*precision*recall / (precision + recall);
			analyser.setAccuracy(accuracy);
			analyser.setPrecision(precision);
			analyser.setRecall(recall);
			analyser.setSpecificity(specificity);
			analyser.setFmeasure(fmeasure);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(analyser, HttpStatus.OK);
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
		float precision = (float)truePositive / (truePositive + falsePositive);
		float recall = (float)truePositive / (truePositive + falseNegative);
		float specificity = (float)trueNegative / (trueNegative + falsePositive);
		float fmeasure = 2*precision*recall / (precision + recall);
		analysis.setAccuracy(accuracy);
		analysis.setPrecision(precision);
		analysis.setRecall(recall);
		analysis.setSpecificity(specificity);
        analysis.setFmeasure(fmeasure);
		
		return new ResponseEntity<>(analysis, HttpStatus.OK);
	}
}