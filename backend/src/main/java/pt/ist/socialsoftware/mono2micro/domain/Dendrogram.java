package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.ControllerTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Pair;
import pt.ist.socialsoftware.mono2micro.utils.Utils;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.DendrogramDeserializer;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.*;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonDeserialize(using = DendrogramDeserializer.class)
public class Dendrogram {
	private String codebaseName;
	private String name;
	private String linkageType;
	private float accessMetricWeight;
	private float writeMetricWeight;
	private float readMetricWeight;
	private float sequenceMetricWeight;
	private String profile;
	private List<Graph> graphs = new ArrayList<>(); // Might not be necessary if the folders structure gets better organized
	private int tracesMaxLimit = 0;
	private TraceType traceType = TraceType.ALL;

	public Dendrogram() {}

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

	public String getProfile() { return profile; }

	public void setProfile(String profile) { this.profile = profile; }

	public List<Graph> getGraphs() {
		return this.graphs;
	}

	public void setGraphs(List<Graph> graphs) { this.graphs = graphs; }

	public int getTracesMaxLimit() { return tracesMaxLimit; }

	public void setTracesMaxLimit(int tracesMaxLimit) { this.tracesMaxLimit = tracesMaxLimit; }

	public TraceType getTypeOfTraces() { return traceType; }

	public void setTypeOfTraces(TraceType traceType) { this.traceType = traceType; }

	@JsonIgnore
	public List<String> getGraphNames() { return this.graphs.stream().map(Graph::getName).collect(Collectors.toList()); }

	@JsonIgnore
	public Graph getGraph(String graphName) {
		return this.graphs.stream().filter(graph -> graph.getName().equals(graphName)).findAny().orElse(null);
	}

	public void addGraph(Graph graph) {
		this.graphs.add(graph);
	}

	public void deleteGraph(
		String graphName
	)
		throws IOException
	{
		for (int i = 0; i < this.graphs.size(); i++) {
			if (this.graphs.get(i).getName().equals(graphName)) {
				this.graphs.remove(i);
				break;
			}
		}

		FileUtils.deleteDirectory(new File(CODEBASES_PATH + this.codebaseName + "/" + this.name + "/" + graphName));
	}

	public Graph createExpertCut(
		String expertName,
		Optional<MultipartFile> expertFile
	)
		throws Exception
	{
		if (this.getGraphNames().contains(expertName))
			throw new KeyAlreadyExistsException();

		Graph expertGraph = new Graph();
		expertGraph.setExpert(true);
		expertGraph.setCodebaseName(this.codebaseName);
		expertGraph.setDendrogramName(this.name);
		expertGraph.setName(expertName);

		if (expertFile.isPresent()) {
			InputStream is = new BufferedInputStream(expertFile.get().getInputStream());
			JSONObject expertCut = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
			is.close();

			Iterator<String> clusters = expertCut.getJSONObject("clusters").keys();

			while (clusters.hasNext()) {
				String clusterId = clusters.next();
				JSONArray entities = expertCut.getJSONObject("clusters").getJSONArray(clusterId);
				Cluster cluster = new Cluster(clusterId);

				for (int i = 0; i < entities.length(); i++) {
					short entityID = (short) entities.getInt(i);

					cluster.addEntity(entityID);
					expertGraph.putEntity(entityID, clusterId);
				}

				expertGraph.addCluster(cluster);
			}
		} else {
			Cluster cluster = new Cluster("Generic");

			JSONObject similarityMatrixData = CodebaseManager.getInstance().getSimilarityMatrix(
				this.codebaseName,
				this.name
			);

			JSONArray entities = similarityMatrixData.getJSONArray("entities");

			for (int i = 0; i < entities.length(); i++) {
				short entityID = (short) entities.getInt(i);

				cluster.addEntity(entityID);
				expertGraph.putEntity(entityID, "Generic");
			}

			expertGraph.addCluster(cluster);
		}

		return expertGraph;
	}

	public JSONObject getMatrixData(
		Set<Short> entityIDs,
		Map<String, Integer> e1e2PairCount,
		Map<Short, List<Pair<String, Byte>>> entityControllers
	)
		throws JSONException
	{
		JSONArray similarityMatrix = new JSONArray();
		JSONObject matrixData = new JSONObject();

		int maxNumberOfPairs = Utils.getMaxNumberOfPairs(e1e2PairCount);

		for (short e1ID : entityIDs) {
			JSONArray matrixRow = new JSONArray();

			for (short e2ID : entityIDs) {
				if (e1ID == e2ID) {
					matrixRow.put(1);
					continue;
				}

				float[] metrics = Utils.calculateSimilarityMatrixMetrics(
					entityControllers,
					e1e2PairCount,
					e1ID,
					e2ID,
					maxNumberOfPairs
				);

				float metric = metrics[0] * this.accessMetricWeight / 100 +
					metrics[1] * this.writeMetricWeight / 100 +
					metrics[2] * this.readMetricWeight / 100 +
					metrics[3] * this.sequenceMetricWeight / 100;

				matrixRow.put(metric);
			}
			similarityMatrix.put(matrixRow);
		}
		matrixData.put("matrix", similarityMatrix);
		matrixData.put("entities", entityIDs);
		matrixData.put("linkageType", this.linkageType);

		return matrixData;
	}

	public Graph cut(Graph graph) throws Exception {

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

		JSONObject clustersJSON = CodebaseManager.getInstance().getClusters(
			this.codebaseName,
			this.name,
			graph.getName()
		);

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
			Cluster cluster = new Cluster(clusterId);

			for (int i = 0; i < entities.length(); i++) {
				short entityID = (short) entities.getInt(i);

				cluster.addEntity(entityID);
				graph.putEntity(entityID, clusterId);
			}

			graph.addCluster(cluster);
		}

		return graph;
	}
}
