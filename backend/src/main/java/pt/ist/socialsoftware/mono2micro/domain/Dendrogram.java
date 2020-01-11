package pt.ist.socialsoftware.mono2micro.domain;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.PYTHON;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.RESOURCES_PATH;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.management.openmbean.KeyAlreadyExistsException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

public class Dendrogram {
	private String codebaseName;
	private String name;
	private String linkageType;
	private float accessMetricWeight;
	private float writeMetricWeight;
	private float readMetricWeight;
	private float sequenceMetricWeight;
	private List<String> profiles = new ArrayList<>();
	private List<Graph> graphs = new ArrayList<>();

	public Dendrogram() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCodebaseName() {
		return this.codebaseName;
	}

	public void setCodebaseName(String codebaseName) {
		this.codebaseName = codebaseName;
	}

	public String getLinkageType() {
		return this.linkageType;
	}

	public void setLinkageType(String linkageType) {
		this.linkageType = linkageType;
	}

	public float getAccessMetricWeight() {
		return this.accessMetricWeight;
	}

	public void setAccessMetricWeight(float accessMetricWeight) {
		this.accessMetricWeight = accessMetricWeight;
	}

	public float getWriteMetricWeight() {
		return this.writeMetricWeight;
	}

	public void setWriteMetricWeight(float writeMetricWeigh) {
		this.writeMetricWeight = writeMetricWeigh;
	}

	public float getReadMetricWeight() {
		return readMetricWeight;
	}

	public void setReadMetricWeight(float readMetricWeight) {
		this.readMetricWeight = readMetricWeight;
	}

	public float getSequenceMetricWeight() {
		return sequenceMetricWeight;
	}

	public void setSequenceMetricWeight(float sequenceMetricWeight) {
		this.sequenceMetricWeight = sequenceMetricWeight;
	}

	public List<String> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<String> profiles) {
		this.profiles = profiles;
	}

	public List<Graph> getGraphs() {
		return this.graphs;
	}

	public List<String> getGraphNames() {
		List<String> graphNames = new ArrayList<>();
		for (Graph graph : this.graphs)
			graphNames.add(graph.getName());
		return graphNames;
	}

	public Graph getGraph(String graphName) {
		for (Graph graph : this.graphs) {
			if (graph.getName().equals(graphName))
				return graph;
		}
		return null;
	}

	public void addGraph(Graph graph) {
		this.graphs.add(graph);
	}

	public void deleteGraph(String graphName) throws IOException {
		for (int i = 0; i < this.graphs.size(); i++) {
			if (this.graphs.get(i).getName().equals(graphName)) {
				this.graphs.remove(i);
				break;
			}
		}
		FileUtils.deleteDirectory(new File(CODEBASES_PATH + this.codebaseName + "/" + this.name + "/" + graphName));
	}

	public void createExpertCut(String expertName, Optional<MultipartFile> expertFile) throws IOException, JSONException {
		if (this.getGraphNames().contains(expertName))
			throw new KeyAlreadyExistsException();

		Graph expert = new Graph();
		expert.setExpert(true);
		expert.setCodebaseName(this.codebaseName);
		expert.setDendrogramName(this.name);
		expert.setName(expertName);
		if (expertFile.isPresent()) {
			InputStream is = new BufferedInputStream(expertFile.get().getInputStream());
			JSONObject expertCut = new JSONObject(IOUtils.toString(is, "UTF-8"));
			is.close();

			Iterator<String> clusters = expertCut.getJSONObject("clusters").keys();

			while (clusters.hasNext()) {
				String clusterId = clusters.next();
				JSONArray entities = expertCut.getJSONObject("clusters").getJSONArray(clusterId);
				Cluster cluster = new Cluster(clusterId);
				for (int i = 0; i < entities.length(); i++) {
					cluster.addEntity(new Entity(entities.getString(i)));
				}
				expert.addCluster(cluster);
			}
		} else {
			Cluster cluster = new Cluster("Generic");

			JSONObject similarityMatrixData = CodebaseManager.getInstance().getSimilarityMatrix(this.codebaseName, this.name);
			JSONArray entities = similarityMatrixData.getJSONArray("entities");
			for (int i = 0; i < entities.length(); i++) {
				cluster.addEntity(new Entity(entities.getString(i)));
			}

			expert.addCluster(cluster);
		}

		this.addGraph(expert);
		expert.calculateMetrics();
	}

	public void calculateSimilarityMatrix() throws IOException, JSONException {
		Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();
		Map<String,Integer> e1e2PairCount = new HashMap<>();
		JSONArray similarityMatrix = new JSONArray();
		JSONObject matrixData = new JSONObject();

		JSONObject datafileJSON = CodebaseManager.getInstance().getDatafile(this.codebaseName);
		Codebase codebase = CodebaseManager.getInstance().getCodebase(this.codebaseName);

		for (String profile : this.profiles) {
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
					matrixRow.put(1);
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

				float metric = accessMetric * this.accessMetricWeight / 100 +
								writeMetric * this.writeMetricWeight / 100 +
								readMetric * this.readMetricWeight / 100 +
								sequenceMetric * this.sequenceMetricWeight / 100;
				
				matrixRow.put(metric);
			}
			similarityMatrix.put(matrixRow);
		}
		matrixData.put("matrix", similarityMatrix);
		matrixData.put("entities", entitiesList);
		matrixData.put("linkageType", this.linkageType);

		CodebaseManager.getInstance().writeSimilarityMatrix(this.codebaseName, this.name, matrixData);
	}

	public void cut(Graph graph) throws Exception {

		String cutValue = Float.valueOf(graph.getCutValue()).toString().replaceAll("\\.?0*$", "");
		if (this.getGraphNames().contains(graph.getCutType() + cutValue)) {
			int i = 2;
			while (this.getGraphNames().contains(graph.getCutType() + cutValue + "(" + i + ")")) {
				i++;
			}
			graph.setName(graph.getCutType() + cutValue + "(" + i + ")");
		} else {
			graph.setName(graph.getCutType() + cutValue);
		}

		File graphPath = new File(CODEBASES_PATH + this.codebaseName + "/" + this.name + "/" + graph.getName());
		if (!graphPath.exists()) {
			graphPath.mkdir();
		}

		Runtime r = Runtime.getRuntime();
		String pythonScriptPath = RESOURCES_PATH + "cutDendrogram.py";
		String[] cmd = new String[8];
		cmd[0] = PYTHON;
		cmd[1] = pythonScriptPath;
		cmd[2] = CODEBASES_PATH;
		cmd[3] = this.codebaseName;
		cmd[4] = this.name;
		cmd[5] = graph.getName();
		cmd[6] = graph.getCutType();
		cmd[7] = Float.toString(graph.getCutValue());
		Process p = r.exec(cmd);
		
		p.waitFor();

		JSONObject clustersJSON = CodebaseManager.getInstance().getClusters(this.codebaseName, this.name, graph.getName());

		graph.setSilhouetteScore((float) clustersJSON.getDouble("silhouetteScore"));

		Iterator<String> clusters = clustersJSON.getJSONObject("clusters").sortedKeys();
		ArrayList<Integer> clusterIds = new ArrayList<>();

		while(clusters.hasNext()) {
			clusterIds.add(Integer.parseInt(clusters.next()));
		}
		Collections.sort(clusterIds);
		for (Integer id : clusterIds) {
			String clusterId = String.valueOf(id);
			JSONArray entities = clustersJSON.getJSONObject("clusters").getJSONArray(clusterId);
			Cluster cluster = new Cluster("Cluster" + clusterId);
			for (int i = 0; i < entities.length(); i++) {
				Entity entity = new Entity(entities.getString(i));
				cluster.addEntity(entity);
			}
			graph.addCluster(cluster);
		}

		this.addGraph(graph);
		graph.calculateMetrics();
	}
}
