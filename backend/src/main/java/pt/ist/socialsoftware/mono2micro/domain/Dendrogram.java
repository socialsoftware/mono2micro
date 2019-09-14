package pt.ist.socialsoftware.mono2micro.domain;

import java.io.BufferedReader;
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

import javax.management.openmbean.KeyAlreadyExistsException;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.utils.Pair;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_FOLDER;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.RESOURCES_PATH;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.PYTHON;

public class Dendrogram {
	private String codebaseName;
	private String name;
	private String linkageType;
	private float accessMetricWeight;
	private float writeMetricWeight;
	private float readMetricWeight;
	private float sequenceMetric1Weight;
	private float sequenceMetric2Weight;
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

	public float getSequenceMetric1Weight() {
		return sequenceMetric1Weight;
	}

	public void setSequenceMetric1Weight(float sequenceMetric1Weight) {
		this.sequenceMetric1Weight = sequenceMetric1Weight;
	}

	public float getSequenceMetric2Weight() {
		return sequenceMetric2Weight;
	}

	public void setSequenceMetric2Weight(float sequenceMetric2Weight) {
		this.sequenceMetric2Weight = sequenceMetric2Weight;
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
		graph.calculateMetrics();
	}

	public void deleteGraph(String graphName) {
		for (int i = 0; i < this.graphs.size(); i++) {
			if (this.graphs.get(i).getName().equals(graphName)) {
				this.graphs.remove(i);
				break;
			}
		}
	}

	public void renameGraph(String graphName, String newName) {
		if (this.getGraphNames().contains(newName)) {
			throw new KeyAlreadyExistsException();
		}
		for (int i = 0; i < graphs.size(); i++) {
			if (graphs.get(i).getName().equals(graphName)) {
				graphs.get(i).setName(newName);
				break;
			}
		}
	}

	public void calculateSimilarityMatrix(Map<String,List<String>> profiles) throws IOException, JSONException{
		Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();
		Map<String,Integer> e1e2PairCount = new HashMap<>();
		JSONArray similarityMatrix = new JSONArray();
		JSONObject dendrogramData = new JSONObject();


		//read datafile
		InputStream is = new FileInputStream(CODEBASES_FOLDER + this.codebaseName + ".txt");
		JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();

		for (String profile : this.profiles) {
			for (String controllerName : profiles.get(profile)) {
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
					seq1MatrixAux.put(Float.valueOf(1));
					seq2MatrixAux.put(Float.valueOf(1));
				} else {
					String e1e2 = e1 + "->" + e2;
					String e2e1 = e2 + "->" + e1;
					float e1e2Count = e1e2PairCount.containsKey(e1e2) ? e1e2PairCount.get(e1e2) : 0;
					float e2e1Count = e1e2PairCount.containsKey(e2e1) ? e1e2PairCount.get(e2e1) : 0;

					seq1MatrixAux.put(Float.valueOf(e1e2Count + e2e1Count));
					seq2MatrixAux.put(Float.valueOf(e1e2Count + e2e1Count));
				}
			}

			List<Float> seq2List = new ArrayList<>();
			for (int k = 0; k < seq2MatrixAux.length(); k++) {
				seq2List.add((float)seq2MatrixAux.get(k));
			}

			float seq2Max = Collections.max(seq2List);

			for (int j = 0; j < entitiesList.size(); j++) {
				if (!entitiesList.get(j).equals(e1)) {
					seq1MatrixAux.put(j, Float.valueOf(((float)seq1MatrixAux.get(j)) / maxNumberOfPairs));
					seq2MatrixAux.put(j, Float.valueOf(((float)seq2MatrixAux.get(j)) / seq2Max));
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
				float metric = accessMetric * this.accessMetricWeight / 100 + 
								writeMetric * this.writeMetricWeight / 100 +
								readMetric * this.readMetricWeight / 100 +
								sequence1Metric * this.sequenceMetric1Weight / 100 +
								sequence2Metric * this.sequenceMetric2Weight / 100;
				matrixAux.put(metric);
			}
			similarityMatrix.put(matrixAux);
		}
		dendrogramData.put("matrix", similarityMatrix);
		dendrogramData.put("entities", entitiesList);

		FileWriter file = new FileWriter(CODEBASES_FOLDER + this.codebaseName + "/" + this.name + ".txt");
		file.write(dendrogramData.toString());
		file.close();
	}

	public void cut(Graph graph, Map<String,List<String>> profiles) throws Exception {
		Map<String,List<Pair<String,String>>> entityControllers = new HashMap<>();

		//read datafile
		InputStream is = new FileInputStream(CODEBASES_FOLDER + this.codebaseName + ".txt");
		JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();

		for (String profile : this.profiles) {
			for (String controllerName : profiles.get(profile)) {
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
		cmd[3] = this.codebaseName;
		cmd[4] = this.name;
		cmd[5] = this.linkageType;
		cmd[6] = Float.toString(graph.getCutValue());
		cmd[7] = graph.getCutType();
		Process p = r.exec(cmd);

		p.waitFor();

		BufferedReader bre = new BufferedReader(new InputStreamReader(p.getInputStream()));
		float silhouetteScore = Float.parseFloat(bre.readLine());
		graph.setSilhouetteScore(silhouetteScore);

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

		is = new FileInputStream("temp_clusters.txt");
		JSONObject json = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();

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
		Files.deleteIfExists(Paths.get("temp_clusters.txt"));
		this.addGraph(graph);
	}
}
