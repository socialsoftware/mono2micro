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
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Pair;
import pt.ist.socialsoftware.mono2micro.utils.PropertiesManager;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_FOLDER;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.RESOURCES_PATH;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}")
public class DendrogramController {

	private static final String PYTHON = PropertiesManager.getProperties().getProperty("python");

	private static Logger logger = LoggerFactory.getLogger(DendrogramController.class);

	private CodebaseManager codebaseManager = CodebaseManager.getInstance();



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

		try {
			File dendrogramImage = new File(CODEBASES_FOLDER + codebaseName + "/" + dendrogramName + ".png");
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(Files.readAllBytes(dendrogramImage.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteDendrogram(@PathVariable String codebaseName, @PathVariable String dendrogramName) {
		logger.debug("deleteDendrogram");
		
		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.deleteDendrogram(dendrogramName);
			codebaseManager.writeCodebase(codebaseName, codebase);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}


	@RequestMapping(value = "/dendrogram/create", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createDendrogram(@RequestBody Dendrogram dendrogram) {
		logger.debug("createDendrogram");

		Codebase codebase = codebaseManager.getCodebase(dendrogram.getCodebaseName());

		//check that dendrogram name is unique
		for (String dendrogramName : codebase.getDendrogramNames()) {
			if (dendrogram.getName().toUpperCase().equals(dendrogramName.toUpperCase()))
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		try {
			Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();
			Map<String,Integer> e1e2PairCount = new HashMap<>();
			JSONArray similarityMatrix = new JSONArray();
			JSONObject dendrogramData = new JSONObject();


			//read datafile
			InputStream is = new FileInputStream(CODEBASES_FOLDER + codebase.getName() + ".txt");
			JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
			is.close();

			for (String profile : dendrogram.getProfiles()) {
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
					float metric = accessMetric * dendrogram.getAccessMetricWeight() / 100 + 
									writeMetric * dendrogram.getWriteMetricWeight() / 100 +
									readMetric * dendrogram.getReadMetricWeight() / 100 +
									sequence1Metric * dendrogram.getSequenceMetric1Weight() / 100 +
									sequence2Metric * dendrogram.getSequenceMetric2Weight() / 100;
					matrixAux.put(metric);
				}
				similarityMatrix.put(matrixAux);
			}
			dendrogramData.put("matrix", similarityMatrix);
			dendrogramData.put("entities", entitiesList);

			try (FileWriter file = new FileWriter(CODEBASES_FOLDER + codebase.getName() + "/" + dendrogram.getName() + ".txt")){
				file.write(dendrogramData.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			codebase.addDendrogram(dendrogram);
			codebaseManager.writeCodebase(codebase.getName(), codebase);

			// run python script with clustering algorithm
			Runtime r = Runtime.getRuntime();
			String pythonScriptPath = RESOURCES_PATH + "dendrogram.py";
			String[] cmd = new String[6];
			cmd[0] = PYTHON;
			cmd[1] = pythonScriptPath;
			cmd[2] = CODEBASES_FOLDER;
			cmd[3] = codebase.getName();
			cmd[4] = dendrogram.getName();
			cmd[5] = dendrogram.getLinkageType();
			
			Process p = r.exec(cmd);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}/cut", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> cutDendrogram(@PathVariable String codebaseName, @PathVariable String dendrogramName, @RequestBody Graph graph) {
		logger.debug("cutDendrogram");

		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);



			Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();

			//read datafile
			InputStream is = new FileInputStream(CODEBASES_FOLDER + codebase.getName() + ".txt");
			JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
			is.close();

			for (String profile : dendrogram.getProfiles()) {
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
					}
				}
			}




			Runtime r = Runtime.getRuntime();
			String pythonScriptPath = RESOURCES_PATH + "cutDendrogram.py";
			String[] cmd = new String[8];
			cmd[0] = PYTHON;
			cmd[1] = pythonScriptPath;
			cmd[2] = CODEBASES_FOLDER;
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
			if (dendrogram.getGraphNames().contains(graph.getCutType() + cutValue)) {
				int i = 2;
				while (dendrogram.getGraphNames().contains(graph.getCutType() + cutValue + "(" + i + ")")) {
					i++;
				}
				graph.setName(graph.getCutType() + cutValue + "(" + i + ")");
			} else {
				graph.setName(graph.getCutType() + cutValue);
			}

			is = new FileInputStream("temp_clusters.txt");
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
					Entity entity = new Entity(entities.getString(i));

					float immutability = 0;
					for (Pair<String,String> controllerPair : entityControllers.get(entity.getName())) {
						if (controllerPair.getSecond().equals("R"))
							immutability++;
					}
					entity.setImmutability(immutability / entityControllers.get(entity.getName()).size());

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