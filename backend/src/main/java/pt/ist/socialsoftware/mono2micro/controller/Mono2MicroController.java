package pt.ist.socialsoftware.mono2micro.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.DendrogramManager;
import pt.ist.socialsoftware.mono2micro.domain.Entity;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.domain.Pair;
import pt.ist.socialsoftware.mono2micro.utils.PropertiesManager;

@RestController
@RequestMapping(value = "/mono2micro/")
public class Mono2MicroController {
	private static final String PYTHON = PropertiesManager.getProperties().getProperty("python");

	private static Logger logger = LoggerFactory.getLogger(Mono2MicroController.class);

	private String resourcesPath = "src/main/resources/";

	private String dendrogramsFolder = "src/main/resources/dendrograms/";

	private DendrogramManager dendrogramManager = new DendrogramManager();

	@RequestMapping(value = "/dendrogramNames", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getDendrogramNames() {
		logger.debug("getDendrogramNames");

		return new ResponseEntity<>(dendrogramManager.getDendrogramNames(), HttpStatus.OK);
	}

	@RequestMapping(value = "/dendrograms", method = RequestMethod.GET)
	public ResponseEntity<List<Dendrogram>> getDendrograms() {
		logger.debug("getDendrograms");

		return new ResponseEntity<>(dendrogramManager.getDendrograms(), HttpStatus.OK);
	}

	@RequestMapping(value = "/deleteDendrogram", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteDendrogram(@RequestParam("dendrogramName") String dendrogramName) {
		logger.debug("deleteDendrogram");

		dendrogramManager.deleteDendrogram(dendrogramName);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/deleteGraph", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteGraph(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName) {
		logger.debug("deleteGraph");

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		boolean success = dend.deleteGraph(graphName);
		if (success) {
			dendrogramManager.writeDendrogram(dendrogramName, dend);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/graphs", method = RequestMethod.GET)
	public ResponseEntity<List<Graph>> getGraphs(@RequestParam("dendrogramName") String dendrogramName) {
		logger.debug("getGraphs");

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		return new ResponseEntity<>(dend.getGraphs(), HttpStatus.OK);
	}

	@RequestMapping(value = "/graph/", method = RequestMethod.GET)
	public ResponseEntity<Graph> getGraph(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName) {
		logger.debug("getGraph: {}", graphName);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		List<Graph> graphs = dend.getGraphs();
		for (Graph graph : graphs) {
			if (graph.getName().equals(graphName)) {
				return new ResponseEntity<>(graph, HttpStatus.OK);
			}
		}

		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/createDendrogram", method = RequestMethod.POST)
	public ResponseEntity<Dendrogram> createDendrogram(@RequestParam("dendrogramName") String dendrogramName,
			@RequestParam("file") MultipartFile datafile, @RequestParam("linkageType") String linkageType, @RequestParam("accessMetricWeight") String accessMetricWeight, @RequestParam("readWriteMetricWeight") String readWriteMetricWeight, @RequestParam("sequenceMetricWeight") String sequenceMetricWeight) {
		logger.debug("createDendrogram filename: {}", datafile.getOriginalFilename());

		long startTime = System.currentTimeMillis();

		File directory = new File(dendrogramsFolder);
		if (!directory.exists())
			directory.mkdir();

		for (String name : dendrogramManager.getDendrogramNames()) {
			if (name.toUpperCase().equals(dendrogramName.toUpperCase()))
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		Dendrogram dend = new Dendrogram(dendrogramName);
		dend.setLinkageType(linkageType);
		dend.setClusteringMetricWeight(accessMetricWeight, readWriteMetricWeight, sequenceMetricWeight);
		
		try {
			// save data to persistent dendrogram
			Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();
			Map<String,Integer> sequenceCount = new HashMap<>();
			int totalSequenceCount = 0;
			List<String> entitiesList = new ArrayList<>();
			JSONArray matrix = new JSONArray();
			JSONObject dendrogramData = new JSONObject();

			InputStream is =  new BufferedInputStream(datafile.getInputStream());
			JSONObject json = new JSONObject(IOUtils.toString(is, "UTF-8"));
			is.close();

			Iterator<String> controllers = json.sortedKeys();

			while(controllers.hasNext()) {
				String controller = controllers.next();

				JSONArray entities = json.getJSONArray(controller);
				for (int i = 0; i < entities.length(); i++) {
					JSONArray entityArray = entities.getJSONArray(i);
					String entity = entityArray.getString(0);
					String mode = entityArray.getString(1);
					if (!dend.containsController(controller))
						dend.addController(new Controller(controller));
					if (!dend.getController(controller).containsEntity(entity))
						dend.getController(controller).addEntity(entity);
					dend.getController(controller).addEntityRW(entity, mode);
					dend.getController(controller).addEntityRWseq(entity, mode);

					if (entityControllers.containsKey(entity)) {
						boolean containsController = false;
						for (Pair<String,String> p : entityControllers.get(entity)) {
							if (p.getFirst().equals(controller)) {
								containsController = true;
								if (p.getSecond().equals("R") && mode.equals("W")) p.setSecond("RW");
								if (p.getSecond().equals("W") && mode.equals("R")) p.setSecond("RW");
							}
						}
						if (!containsController) {
							entityControllers.get(entity).add(new Pair<String,String>(controller,mode));
						}
					} else {
						List<Pair<String,String>> controllersPairs = new ArrayList<>();
						controllersPairs.add(new Pair<String,String>(controller,mode));
						entityControllers.put(entity, controllersPairs);
					}

					if (!entitiesList.contains(entity)) entitiesList.add(entity);

					if (i < entities.length() - 1) {
						JSONArray nextEntityArray = entities.getJSONArray(i+1);
						String nextEntity = nextEntityArray.getString(0);
						String e1e2 = entity + "->" + nextEntity;
						
						int count = sequenceCount.containsKey(e1e2) ? sequenceCount.get(e1e2) : 0;
						sequenceCount.put(e1e2, count + 1);
					}
				}
				totalSequenceCount += entities.length() - 1;
			}

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
						float e1e2Count = sequenceCount.containsKey(e1e2) ? sequenceCount.get(e1e2) : 0;
						float e2e1Count = sequenceCount.containsKey(e2e1) ? sequenceCount.get(e2e1) : 0;

						float accessMetric = inCommon / entityControllers.get(e1).size();
						float readWriteMetric = inCommonW / entityControllers.get(e1).size();
						float sequenceMetric = (e1e2Count + e2e1Count) / totalSequenceCount;
						float metric = accessMetric * Float.parseFloat(accessMetricWeight) + 
									   readWriteMetric * Float.parseFloat(readWriteMetricWeight) +
									   sequenceMetric * Float.parseFloat(sequenceMetricWeight);
						matrixAux.put(metric);
					}
				}
				matrix.put(matrixAux);
			}
			dendrogramData.put("matrix", matrix);
			dendrogramData.put("entities", entitiesList);

			try (FileWriter file = new FileWriter(dendrogramsFolder + dendrogramName + ".txt")){
				file.write(dendrogramData.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			// run python script with clustering algorithm
			Runtime r = Runtime.getRuntime();
			String pythonScriptPath = resourcesPath + "dendrogram.py";
			String[] cmd = new String[5];
			cmd[0] = PYTHON;
			cmd[1] = pythonScriptPath;
			cmd[2] = dendrogramsFolder;
			cmd[3] = dendrogramName;
			cmd[4] = linkageType;
			
			Process p = r.exec(cmd);

			p.waitFor();

			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = bre.readLine()) != null) {
				System.out.println("Inside Elapsed time: " + line + " seconds");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		dendrogramManager.writeDendrogram(dendrogramName, dend);

		long elapsedTimeMillis = System.currentTimeMillis() - startTime;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("Complete. Elapsed time: " + elapsedTimeSec + " seconds");

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@RequestMapping(value = "/loadDendrogram", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> loadDendrogram(@RequestParam("dendrogramName") String dendrogramName) {
		logger.debug("loadDendrogram");

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);

		return new ResponseEntity<Dendrogram>(dend, HttpStatus.OK);
	}

	@RequestMapping(value = "/dendrogram-image", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getImage(@RequestParam("dendrogramName") String dendrogramName) {
		logger.debug("load dendrogram image");

		File f = new File(dendrogramsFolder + dendrogramName + ".png");
		try {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(Files.readAllBytes(f.toPath()));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/cutDendrogram", method = RequestMethod.GET)
	public ResponseEntity<Graph> getCutDendrogram(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("cutValue") String cutValue) {
		logger.debug("cutDendrogram with value: {}", cutValue);

		try {
			Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);

			Runtime r = Runtime.getRuntime();
			String pythonScriptPath = resourcesPath + "cutDendrogram.py";
			String[] cmd = new String[6];
			cmd[0] = PYTHON;
			cmd[1] = pythonScriptPath;
			cmd[2] = dendrogramsFolder;
			cmd[3] = dendrogramName;
			cmd[4] = dend.getLinkageType();
			cmd[5] = cutValue;
			Process p = r.exec(cmd);

			p.waitFor();

			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getInputStream()));
			float silhouetteScore = Float.parseFloat(bre.readLine());

			Graph graph;
			if (dend.getGraphsNames().contains("Graph_" + cutValue)) {
				int i = 2;
				while (dend.getGraphsNames().contains("Graph_" + cutValue + "(" + i + ")")) {
					i++;
				}
				graph = new Graph("Graph_" + cutValue + "(" + i + ")", cutValue, silhouetteScore, dendrogramName);
			} else {
				graph = new Graph("Graph_" + cutValue, cutValue, silhouetteScore, dendrogramName);
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
			dend.addGraph(graph);

			dendrogramManager.writeDendrogram(dendrogramName, dend);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/mergeClusters", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> mergeClusters(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName,
			@RequestParam("cluster1") String cluster1, @RequestParam("cluster2") String cluster2,
			@RequestParam("newName") String newName) {
		logger.debug("mergeClusters {} with {}", cluster1, cluster2);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		dend.mergeClusters(graphName, cluster1, cluster2, newName);
		dendrogramManager.writeDendrogram(dendrogramName, dend);
		return new ResponseEntity<Dendrogram>(dend, HttpStatus.OK);
	}

	@RequestMapping(value = "/renameCluster", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> renameCluster(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName,
			@RequestParam("clusterName") String clusterName, @RequestParam("newName") String newName) {
		logger.debug("renameCluster {}", clusterName);
		
		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		boolean success = dend.renameCluster(graphName, clusterName, newName);
		if (success) {
			dendrogramManager.writeDendrogram(dendrogramName, dend);
			return new ResponseEntity<Dendrogram>(dend, HttpStatus.OK);
		} else {
			return new ResponseEntity<Dendrogram>(dend, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/renameGraph", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> renameGraph(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName,
			@RequestParam("newName") String newName) {
		logger.debug("renameGraph {}", graphName);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		boolean success = dend.renameGraph(graphName, newName);
		if (success) {
			dendrogramManager.writeDendrogram(dendrogramName, dend);
			return new ResponseEntity<Dendrogram>(dend, HttpStatus.OK);
		} else {
			return new ResponseEntity<Dendrogram>(dend, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/splitCluster", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> splitCluster(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName,
			@RequestParam("clusterName") String clusterName, @RequestParam("newName") String newName, 
			@RequestParam("entities") String entities) {
		logger.debug("splitCluster: {}", clusterName);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		dend.splitCluster(graphName, clusterName, newName, entities.split(","));
		dendrogramManager.writeDendrogram(dendrogramName, dend);
		return new ResponseEntity<Dendrogram>(dend, HttpStatus.OK);
	}

	@RequestMapping(value = "/getControllerClusters", method = RequestMethod.GET)
	public ResponseEntity<Map<String,List<Cluster>>> getControllerClusters(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName) {
		logger.debug("getControllerClusters: in graph {}", graphName);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		return new ResponseEntity<Map<String,List<Cluster>>>(dend.getControllerClusters(graphName), HttpStatus.OK);
	}

	@RequestMapping(value = "/getClusterControllers", method = RequestMethod.GET)
	public ResponseEntity<Map<String,List<Controller>>> getClusterControllers(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName) {
		logger.debug("getClusterControllers: in graph {}", graphName);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		return new ResponseEntity<Map<String,List<Controller>>>(dend.getClusterControllers(graphName), HttpStatus.OK);
	}

	@RequestMapping(value = "/getControllers", method = RequestMethod.GET)
	public ResponseEntity<List<Controller>> getControllers(@RequestParam("dendrogramName") String dendrogramName) {
		logger.debug("getControllers");
		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		return new ResponseEntity<List<Controller>>(dend.getControllers(), HttpStatus.OK);
	}

	@RequestMapping(value = "/getController", method = RequestMethod.GET)
	public ResponseEntity<Controller> getController(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("controllerName") String controllerName) {
		logger.debug("getController");
		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		return new ResponseEntity<Controller>(dend.getController(controllerName), HttpStatus.OK);
	}

	@RequestMapping(value = "/transferEntities", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> transferEntities(@RequestParam("dendrogramName") String dendrogramName, @RequestParam("graphName") String graphName,
			@RequestParam("fromCluster") String fromCluster, @RequestParam("toCluster") String toCluster, 
			@RequestParam("entities") String entities) {
		logger.debug("transferEntities: {}", fromCluster);

		Dendrogram dend = dendrogramManager.getDendrogram(dendrogramName);
		dend.transferEntities(graphName, fromCluster, toCluster, entities.split(","));
		dendrogramManager.writeDendrogram(dendrogramName, dend);
		return new ResponseEntity<Dendrogram>(dend, HttpStatus.OK);
	}
}