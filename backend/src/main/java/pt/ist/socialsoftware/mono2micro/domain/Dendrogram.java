package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
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
	private String name;
	private String codebaseName;
	private String base; // Describes if the dendrogram should be created based on commit data or static data.
	private String linkageType;
	private float accessMetricWeight;
	private float writeMetricWeight;
	private float readMetricWeight;
	private float sequenceMetricWeight;
	private float commitMetricWeight;
	private float authorMetricWeight;
	private String profile;
	private List<Decomposition> decompositions = new ArrayList<>(); // Might not be necessary if the folders structure gets better organized
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

	public void setCommitMetricWeight(float commitMetricWeight) { this.commitMetricWeight = commitMetricWeight; }

	public void setAuthorMetricWeight(float authorMetricWeight) { this.authorMetricWeight = authorMetricWeight; }

	public String getProfile() { return profile; }

	public void setProfile(String profile) { this.profile = profile; }

	public List<Decomposition> getDecompositions() {
		return this.decompositions;
	}

	public void setDecompositions(List<Decomposition> decompositions) { this.decompositions = decompositions; }

	public int getTracesMaxLimit() { return tracesMaxLimit; }

	public void setTracesMaxLimit(int tracesMaxLimit) { this.tracesMaxLimit = tracesMaxLimit; }

	public TraceType getTraceType() { return traceType; }

	public void setTraceType(TraceType traceType) { this.traceType = traceType; }

	@JsonIgnore
	public List<String> getDecompositionNames() { return this.decompositions.stream().map(Decomposition::getName).collect(Collectors.toList()); }

	@JsonIgnore
	public Decomposition getDecomposition(String decompositionName) {
		return this.decompositions.stream()
			.filter(decomposition -> decomposition.getName().equals(decompositionName))
			.findAny()
			.orElse(null);
	}

	public void addDecomposition(Decomposition decomposition) {
		this.decompositions.add(decomposition);
	}

	public void deleteDecomposition(
		String decompositionName
	)
		throws IOException
	{
		for (int i = 0; i < this.decompositions.size(); i++) {
			if (this.decompositions.get(i).getName().equals(decompositionName)) {
				this.decompositions.remove(i);
				break;
			}
		}

		FileUtils.deleteDirectory(
			new File(CODEBASES_PATH + this.codebaseName + "/" + this.name + "/" + decompositionName)
		);
	}

	public Decomposition createExpertCut(
		String expertName,
		Optional<MultipartFile> expertFile
	)
		throws Exception
	{
		if (this.getDecompositionNames().contains(expertName))
			throw new KeyAlreadyExistsException();

		Decomposition expertDecomposition = new Decomposition();
		expertDecomposition.setExpert(true);
		expertDecomposition.setCodebaseName(this.codebaseName);
		expertDecomposition.setDendrogramName(this.name);
		expertDecomposition.setName(expertName);

		if (expertFile.isPresent()) {
			InputStream is = new BufferedInputStream(expertFile.get().getInputStream());
			JSONObject expertCut = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
			is.close();

			Iterator<String> clusters = expertCut.getJSONObject("clusters").keys();

			short clusterID = 0;
			while (clusters.hasNext()) {
				String clusterName = clusters.next();
				JSONArray entities = expertCut.getJSONObject("clusters").getJSONArray(clusterName);
				Cluster cluster = new Cluster(clusterID++, clusterName);

				for (int i = 0; i < entities.length(); i++) {
					short entityID = (short) entities.getInt(i);

					cluster.addEntity(entityID);
					expertDecomposition.putEntity(entityID, cluster.getID());
				}

				expertDecomposition.addCluster(cluster);
			}

			expertDecomposition.setNextClusterID(clusterID);

		} else {
			Cluster cluster = new Cluster((short) 0, "Generic");

			expertDecomposition.setNextClusterID((short) 1);

			JSONObject similarityMatrixData = CodebaseManager.getInstance().getSimilarityMatrix(
				this.codebaseName,
				this.name
			);

			JSONArray entities = similarityMatrixData.getJSONArray("entities");

			for (int i = 0; i < entities.length(); i++) {
				short entityID = (short) entities.getInt(i);

				cluster.addEntity(entityID);
				expertDecomposition.putEntity(entityID, cluster.getID());
			}

			expertDecomposition.addCluster(cluster);
		}

		return expertDecomposition;
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

	public JSONObject getCommitMatrix(HashMap<String,ArrayList<Integer>> commitChanges, HashMap<String,ArrayList<String>> authorChanges) throws JSONException {
		JSONArray similarityMatrix = new JSONArray();
		JSONObject matrixData = new JSONObject();

		for (String file1 : commitChanges.keySet()) {
			JSONArray matrixRow = new JSONArray();

			for (String file2 : commitChanges.keySet()) {
				if (file1.equals(file2)) {
					matrixRow.put(1);
					continue;
				}
				// Metric is given by the number of times file2 appears in file1's array. We don't need to check the
				// number of times file1 appears in file2's array, because it should be the same value.
				int metric = 0;
//				metric += (int) authorChanges.get(file1).stream().filter(authorChanges.get(file2)::contains).count() * this.authorMetricWeight/100;
				for (Integer fileInFile1Changes : commitChanges.get(file1)) {
					if (String.valueOf(fileInFile1Changes).equals(file2)) {
						metric += 1*this.commitMetricWeight/100;
					}
				}

				matrixRow.put(metric);
			}
			similarityMatrix.put(matrixRow);
		}
		// In some cases, entities may be missing from the commitChanges keyset.
		matrixData.put("matrix", similarityMatrix);
		matrixData.put("entities", commitChanges.keySet());
		matrixData.put("linkageType", this.linkageType);
		//System.out.print(matrixData);

		return matrixData;
	}

	public Decomposition cut(Decomposition decomposition)
		throws Exception
	{

		String cutValue = Float.valueOf(decomposition.getCutValue()).toString().replaceAll("\\.?0*$", "");
		if (this.getDecompositionNames().contains(decomposition.getCutType() + cutValue)) {
			int i = 2;
			while (this.getDecompositionNames().contains(decomposition.getCutType() + cutValue + "(" + i + ")")) {
				i++;
			}
			decomposition.setName(decomposition.getCutType() + cutValue + "(" + i + ")");
		} else {
			decomposition.setName(decomposition.getCutType() + cutValue);
		}

		File decompositionPath = new File(CODEBASES_PATH + this.codebaseName + "/" + this.name + "/" + decomposition.getName());
		if (!decompositionPath.exists()) {
			decompositionPath.mkdir();
		}

		WebClient.create(SCRIPTS_ADDRESS)
				.get()
				.uri("/scipy/{codebaseName}/{dendrogramName}/{graphName}/{cutType}/{cutValue}/{commitBased}/cut",
						this.codebaseName, this.name, decomposition.getName(), decomposition.getCutType(), Float.toString(decomposition.getCutValue()),
						this.commitBased())
				.exchange()
				.doOnSuccess(clientResponse -> {
					if (clientResponse.statusCode() != HttpStatus.OK)
						throw new RuntimeException("Error Code:" + clientResponse.statusCode());
				}).block();

		JSONObject clustersJSON = CodebaseManager.getInstance().getClusters(
			this.codebaseName,
			this.name,
			decomposition.getName()
		);

		decomposition.setSilhouetteScore((float) clustersJSON.getDouble("silhouetteScore"));

		Iterator<String> clusters = clustersJSON.getJSONObject("clusters").sortedKeys();
		ArrayList<Short> clusterIds = new ArrayList<>();

		while(clusters.hasNext()) {
			clusterIds.add(Short.parseShort(clusters.next()));
		}

		Collections.sort(clusterIds);

		for (Short id : clusterIds) {
			String clusterName = String.valueOf(id);
			JSONArray entities = clustersJSON.getJSONObject("clusters").getJSONArray(id.toString());
			Cluster cluster = new Cluster(id, clusterName);

			for (int i = 0; i < entities.length(); i++) {
				short entityID = (short) entities.getInt(i);

				cluster.addEntity(entityID);
				decomposition.putEntity(entityID, id);
			}

			decomposition.addCluster(cluster);
		}

		decomposition.setNextClusterID(Integer.valueOf(clusterIds.size()).shortValue());

		return decomposition;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getBase() {
		return this.base;
	}

	public boolean commitBased() {
		return this.getBase().equals("COMMIT");
	}
}
