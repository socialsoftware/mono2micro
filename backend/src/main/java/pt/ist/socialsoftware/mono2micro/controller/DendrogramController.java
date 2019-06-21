package pt.ist.socialsoftware.mono2micro.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.Entity;
import pt.ist.socialsoftware.mono2micro.domain.Expert;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.dto.AnalysisDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.manager.DendrogramManager;
import pt.ist.socialsoftware.mono2micro.manager.ExpertManager;
import pt.ist.socialsoftware.mono2micro.utils.Pair;
import pt.ist.socialsoftware.mono2micro.utils.PropertiesManager;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}")
public class DendrogramController {

	private static final String PYTHON = PropertiesManager.getProperties().getProperty("python");

	private static Logger logger = LoggerFactory.getLogger(DendrogramController.class);

	private String resourcesPath = "src/main/resources/";

	private String codebaseFolder = "src/main/resources/codebases/";

	private CodebaseManager codebaseManager = new CodebaseManager();


	@RequestMapping(value = "/analysis", method = RequestMethod.POST)
	public ResponseEntity<AnalysisDto> getAnalysis(@RequestBody AnalysisDto analysis) {
		logger.debug("getAnalysis");

		Codebase codebase = codebaseManager.getCodebase(analysis.getCodebaseName());

		Map<String,List<String>> graph1 = new HashMap<>();
		if (analysis.getDendrogramName1() == null) {
			Expert expert = codebase.getExpert(analysis.getGraphName1());
			graph1 = expert.getClusters();
		} else {
			Dendrogram dendrogram = codebase.getDendrogram(analysis.getDendrogramName1());
			Graph graph = dendrogram.getGraph(analysis.getGraphName1());
			for (Cluster c : graph.getClusters()) {
				graph1.put(c.getName(), c.getEntities());
			}
		}

		Map<String,List<String>> graph2 = new HashMap<>();
		if (analysis.getDendrogramName2() == null) {
			Expert expert = codebase.getExpert(analysis.getGraphName2());
			graph2 = expert.getClusters();
		} else {
			Dendrogram dendrogram = codebase.getDendrogram(analysis.getDendrogramName2());
			Graph graph = dendrogram.getGraph(analysis.getGraphName2());
			for (Cluster c : graph.getClusters()) {
				graph2.put(c.getName(), c.getEntities());
			}
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
				String entity1 = entities.get(i);
				String entity2 = entities.get(j);
				
				boolean sameClusterInGraph1 = false;
				for (List<String> l : graph1.values()) {
					if (l.contains(entity1) && l.contains(entity2))
						sameClusterInGraph1 = true;
				}

				boolean sameClusterInGraph2 = false;
				for (List<String> l : graph2.values()) {
					if (l.contains(entity1) && l.contains(entity2))
						sameClusterInGraph2 = true;
				}

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

		analysis.setTruePositive(truePositive);
		analysis.setTrueNegative(trueNegative);
		analysis.setFalsePositive(falsePositive);
		analysis.setFalseNegative(falseNegative);

		float precision = (float)truePositive / (truePositive + falsePositive);
		float recall = (float)truePositive / (truePositive + falseNegative);
		float fmeasure = 2*precision*recall / (precision + recall);
		analysis.setPrecision(precision);
		analysis.setRecall(recall);
		analysis.setFmeasure(fmeasure);
		
		return new ResponseEntity<>(analysis, HttpStatus.OK);
	}


	@RequestMapping(value = "/dendrogramNames", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getDendrogramNames(@PathVariable String codebaseName) {
		logger.debug("getDendrogramNames");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getDendrogramNames(), HttpStatus.OK);
	}


	@RequestMapping(value = "/dendrograms", method = RequestMethod.GET)
	public ResponseEntity<List<Dendrogram>> getDendrograms(@PathVariable String codebaseName) {
		logger.debug("getDendrograms");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getDendrograms(), HttpStatus.OK);
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> getDendrogram(@PathVariable String codebaseName, @PathVariable String dendrogramName) {
		logger.debug("getDendrogram");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getDendrogram(dendrogramName), HttpStatus.OK);
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}/image", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getDendrogramImage(@PathVariable String codebaseName, @PathVariable String dendrogramName) {
		logger.debug("getDendrogramImage");

		File f = new File(codebaseFolder + codebaseName + "/" + dendrogramName + ".png");
		try {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(Files.readAllBytes(f.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteDendrogram(@PathVariable String codebaseName, @PathVariable String dendrogramName) {
		logger.debug("deleteDendrogram");

		Codebase codebase = codebaseManager.getCodebase(codebaseName);
		boolean deleted = codebase.deleteDendrogram(dendrogramName);
		if (deleted) {
            codebaseManager.writeCodebase(codebaseName, codebase);
			return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}/controllers", method = RequestMethod.GET)
	public ResponseEntity<List<Controller>> getControllers(@PathVariable String codebaseName, @PathVariable String dendrogramName) {
		logger.debug("getControllers");
		
		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getDendrogram(dendrogramName).getControllers(), HttpStatus.OK);
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}/controller/{controllerName}", method = RequestMethod.GET)
	public ResponseEntity<Controller> getController(@PathVariable String codebaseName, @PathVariable String dendrogramName, @PathVariable String controllerName) {
		logger.debug("getController");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getDendrogram(dendrogramName).getController(controllerName), HttpStatus.OK);
	}


	@RequestMapping(value = "/dendrogram/create", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createDendrogram(@RequestBody Dendrogram dendrogram) {

		logger.debug("createDendrogram");

		long startTime = System.currentTimeMillis();

		Codebase codebase = codebaseManager.getCodebase(dendrogram.getCodebaseName());

		for (String dendrogramName : codebase.getDendrogramNames()) {
			if (dendrogram.getName().toUpperCase().equals(dendrogramName.toUpperCase()))
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		try {
			Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();
			Map<String,Integer> e1e2PairCount = new HashMap<>();
			int totalSequencePairsCount = 0;
			JSONArray similarityMatrix = new JSONArray();
			JSONObject dendrogramData = new JSONObject();


			//read datafile
			InputStream is = new FileInputStream(codebaseFolder + codebase.getName() + "/" + codebase.getName() + ".txt");
			JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
			is.close();

			for (String profile : dendrogram.getProfiles()) {
				for (String controllerName : codebase.getProfile(profile)) {
					Controller controller = new Controller(controllerName);
					dendrogram.addController(controller);

					JSONArray entities = datafileJSON.getJSONArray(controllerName);
					for (int i = 0; i < entities.length(); i++) {
						JSONArray entityArray = entities.getJSONArray(i);
						String entity = entityArray.getString(0);
						String mode = entityArray.getString(1);
						
						controller.addEntity(entity, mode);
						controller.addEntitySeq(entity, mode);

						if (!dendrogram.containsEntity(entity))
							dendrogram.addEntity(new Entity(entity));

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
							String e1e2 = entity + "->" + nextEntity;
							
							int count = e1e2PairCount.containsKey(e1e2) ? e1e2PairCount.get(e1e2) : 0;
							e1e2PairCount.put(e1e2, count + 1);
						}
					}
					totalSequencePairsCount += entities.length() - 1;
				}
			}

			List<String> entitiesList = new ArrayList<String>(entityControllers.keySet());
			Collections.sort(entitiesList);

			for (String e1 : entitiesList) {
				JSONArray matrixAux = new JSONArray();
				for (String e2 : entitiesList) {
					if (e1.equals(e2)) {
						matrixAux.put(1);
					} else {
						float inCommon = 0;
						float inCommonW = 0;
						for (Pair<String,String> p1 : entityControllers.get(e1)) {
							for (Pair<String,String> p2 : entityControllers.get(e2)) {
								if (p1.getFirst().equals(p2.getFirst()))
									inCommon++;
								if (p1.getFirst().equals(p2.getFirst()) && p1.getSecond().contains("W") && p2.getSecond().contains("W"))
									inCommonW++;
							}
						}

						String e1e2 = e1 + "->" + e2;
						String e2e1 = e2 + "->" + e1;
						float e1e2Count = e1e2PairCount.containsKey(e1e2) ? e1e2PairCount.get(e1e2) : 0;
						float e2e1Count = e1e2PairCount.containsKey(e2e1) ? e1e2PairCount.get(e2e1) : 0;

						float accessMetric = inCommon / entityControllers.get(e1).size();
						float readWriteMetric = inCommonW / entityControllers.get(e1).size();
						float sequenceMetric = (e1e2Count + e2e1Count) / totalSequencePairsCount;
						float metric = accessMetric * dendrogram.getAccessMetricWeight() / 100 + 
									   readWriteMetric * dendrogram.getReadWriteMetricWeight() / 100 +
									   sequenceMetric * dendrogram.getSequenceMetricWeight() / 100;
						matrixAux.put(metric);
					}
				}
				similarityMatrix.put(matrixAux);

				float immutability = 0;
				for (Pair<String,String> controllerPair : entityControllers.get(e1)) {
					if (controllerPair.getSecond().equals("R"))
						immutability++;
				}
				dendrogram.getEntity(e1).setImmutability(immutability / entityControllers.get(e1).size());
			}
			dendrogramData.put("matrix", similarityMatrix);
			dendrogramData.put("entities", entitiesList);

			try (FileWriter file = new FileWriter(codebaseFolder + codebase.getName() + "/" + dendrogram.getName() + ".txt")){
				file.write(dendrogramData.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			// run python script with clustering algorithm
			Runtime r = Runtime.getRuntime();
			String pythonScriptPath = resourcesPath + "dendrogram.py";
			String[] cmd = new String[6];
			cmd[0] = PYTHON;
			cmd[1] = pythonScriptPath;
			cmd[2] = codebaseFolder;
			cmd[3] = codebase.getName();
			cmd[4] = dendrogram.getName();
			cmd[5] = dendrogram.getLinkageType();
			
			Process p = r.exec(cmd);

			p.waitFor();

			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = bre.readLine()) != null) {
				System.out.println("Inside Elapsed time: " + line + " seconds");
			}

			codebase.addDendrogram(dendrogram);
			codebaseManager.writeCodebase(codebase.getName(), codebase);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		long elapsedTimeMillis = System.currentTimeMillis() - startTime;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("Complete. Elapsed time: " + elapsedTimeSec + " seconds");

		return new ResponseEntity<>(HttpStatus.CREATED);
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}/cut", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> cutDendrogram(@PathVariable String codebaseName, @PathVariable String dendrogramName, @RequestBody Graph graph) {
		logger.debug("cutDendrogram");

		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);

			Runtime r = Runtime.getRuntime();
			String pythonScriptPath = resourcesPath + "cutDendrogram.py";
			String[] cmd = new String[8];
			cmd[0] = PYTHON;
			cmd[1] = pythonScriptPath;
			cmd[2] = codebaseFolder;
			cmd[3] = codebaseName;
			cmd[4] = dendrogramName;
			cmd[5] = dendrogram.getLinkageType();
			cmd[6] = Float.toString(graph.getCutValue());
			cmd[7] = graph.getCutType();
			Process p = r.exec(cmd);

			p.waitFor();

			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getInputStream()));
			float silhouetteScore = Float.parseFloat(bre.readLine());
			graph.setSilhouetteScore(silhouetteScore);

			String cutValue = new Float(graph.getCutValue()).toString().replaceAll("\\.?0*$", "");
			if (dendrogram.getGraphsNames().contains("Graph_" + graph.getCutType() + "_" + cutValue)) {
				int i = 2;
				while (dendrogram.getGraphsNames().contains("Graph_" + graph.getCutType() + "_" + cutValue + "(" + i + ")")) {
					i++;
				}
				graph.setName("Graph_" + graph.getCutType() + "_" + cutValue + "(" + i + ")");
			} else {
				graph.setName("Graph_" + graph.getCutType() + "_" + cutValue);
			}

			InputStream is = new FileInputStream("temp_clusters.txt");
			JSONObject json = new JSONObject(IOUtils.toString(is, "UTF-8"));

			Iterator<String> clusters = json.sortedKeys();
			ArrayList<Integer> clusterIds = new ArrayList<>();

			while(clusters.hasNext()) {
				clusterIds.add(Integer.parseInt(clusters.next()));
			}
			Collections.sort(clusterIds);
			for (Integer id : clusterIds) {
				String clusterId = String.valueOf(id);
				JSONArray entities = json.getJSONArray(clusterId);
				Cluster cluster = new Cluster("Cluster" + clusterId);
				for (int i = 0; i < entities.length(); i++) {
					String entity = entities.getString(i);
					cluster.addEntity(entity);
				}
				graph.addCluster(cluster);
			}
			is.close();
			Files.deleteIfExists(Paths.get("temp_clusters.txt"));
			dendrogram.addGraph(graph);

			codebaseManager.writeCodebase(codebaseName, codebase);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}
}