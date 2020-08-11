package pt.ist.socialsoftware.mono2micro.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.KeyAlreadyExistsException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.ControllerDto;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.ControllerTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Metrics;

public class Graph {
	private String codebaseName;
	private String dendrogramName;
	private String name;
	private boolean expert;
	private float cutValue;
	private String cutType;
	private float silhouetteScore;
	private float complexity;
	private float cohesion;
	private float coupling;
	private List<Controller> controllers = new ArrayList<>(); // FIXME Hashmap
	private List<Cluster> clusters = new ArrayList<>(); // FIXME Hashmap

	public Graph() {
	}

	public String getCodebaseName() {
		return this.codebaseName;
	}

	public void setCodebaseName(String codebaseName) {
		this.codebaseName = codebaseName;
	}

	public String getDendrogramName() {
		return this.dendrogramName;
	}

	public void setDendrogramName(String dendrogramName) {
		this.dendrogramName = dendrogramName;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isExpert() {
		return expert;
	}

	public void setExpert(boolean expert) {
		this.expert = expert;
	}

	public float getCutValue() {
		return this.cutValue;
	}

	public void setCutValue(float cutValue) {
		this.cutValue = cutValue;
	}

	public String getCutType() {
		return cutType;
	}

	public void setCutType(String cutType) {
		this.cutType = cutType;
	}

	public float getSilhouetteScore() {
		return this.silhouetteScore;
	}

	public void setSilhouetteScore(float silhouetteScore) {
		this.silhouetteScore = silhouetteScore;
	}

	public float getComplexity() {
		return complexity;
	}

	public void setComplexity(float complexity) {
		this.complexity = complexity;
	}

	public float getCohesion() {
		return cohesion;
	}

	public void setCohesion(float cohesion) {
		this.cohesion = cohesion;
	}

	public float getCoupling() {
		return coupling;
	}

	public void setCoupling(float coupling) {
		this.coupling = coupling;
	}

	public int maxClusterSize() {
		int max = 0;
		for (Cluster cluster : this.clusters)
			if (cluster.getEntities().size() > max)
				max = cluster.getEntities().size();
		return max;
	}

	public List<Controller> getControllers() {
		return this.controllers;
	}

	public void addController(Controller controller) {
		this.controllers.add(controller);
	}

	private <A extends AccessDto> void calculateControllerSequences (
		JSONArray entitiesSeq,
		JSONObject clusterAccess,
		Controller controller,
		List<A> accesses
	) throws JSONException {

		for (int i = 0; i < accesses.size(); i++) {
			A access = accesses.get(i);
			String entity = access.getEntity();
			String mode = access.getMode();
			String cluster = null;

			try {
				cluster = this.getClusterWithEntity(entity).getName();
			}
			catch (Exception e) {
				System.err.println("Expert cut does not assign entity " + entity + " to a cluster.");
				throw e;
			}

			if (i == 0) {
				clusterAccess.put("cluster", cluster);
				clusterAccess.put("sequence", new JSONArray());
				clusterAccess.getJSONArray("sequence").put(access.toJSONrray());
				controller.addEntity(entity, mode);

			} else {
				// FIXME
				String previousCluster = this.getClusterWithEntity((accesses.get(i-1)).getEntity()).getName();

				if (cluster.equals(previousCluster)) {
					boolean hasNoCost = false;

					for (int j = 0; j < clusterAccess.getJSONArray("sequence").length(); j++) {
						JSONArray entityPair = clusterAccess.getJSONArray("sequence").getJSONArray(j);
						if (entity.equals(entityPair.getString(0)) && mode.equals(entityPair.getString(1))) {
							hasNoCost = true;
							break;
						}
						if (entity.equals(entityPair.getString(0)) && "W".equals(entityPair.getString(1))) {
							hasNoCost = true;
							break;
						}
					}
					if (!hasNoCost) {
						clusterAccess.getJSONArray("sequence").put(access.toJSONrray());
						controller.addEntity(entity, mode);
					}
				} else {
					entitiesSeq.put(clusterAccess);
					clusterAccess = new JSONObject();
					clusterAccess.put("cluster", cluster);
					clusterAccess.put("sequence", new JSONArray());
					clusterAccess.getJSONArray("sequence").put(access.toJSONrray());
					controller.addEntity(entity, mode);
				}
			}
		}
	}

	public void addStaticControllers(
		List<String> profiles,
		HashMap<String, ControllerDto> datafile
	)
		throws JSONException, IOException
	{
		this.controllers = new ArrayList<>();

		HashMap<String, ControllerDto> datafileJSON;
		if (datafile == null)
			datafileJSON = CodebaseManager.getInstance().getDatafile(this.codebaseName);
		else
			datafileJSON = datafile;

		Codebase codebase = CodebaseManager.getInstance().getCodebase(this.codebaseName);

		for (String profile : profiles) {
			for (String controllerName : codebase.getProfile(profile)) {

				Controller controller = new Controller(controllerName);
				this.addController(controller);
	
				JSONArray entitiesSeq = new JSONArray();
				JSONObject clusterAccess = new JSONObject();

				ControllerDto controllerDto = datafileJSON.get(controllerName);
				List<AccessDto> controllerAccesses = controllerDto.getControllerAccesses();

				calculateControllerSequences(
					entitiesSeq,
					clusterAccess,
					controller,
					controllerAccesses
				);

				entitiesSeq.put(clusterAccess);
				controller.addEntitiesSeq(entitiesSeq);
			}
		}
	}

	public void addDynamicControllers(List<String> profiles) throws JSONException, IOException {
		this.controllers = new ArrayList<>();

		Codebase codebase = CodebaseManager.getInstance().getCodebase(this.codebaseName);

		for (String profile : profiles) {
			for (String controllerName : codebase.getProfile(profile)) {

				Controller controller = new Controller(controllerName);
				this.addController(controller);

				JSONArray entitiesSeq = new JSONArray();
				JSONObject clusterAccess = new JSONObject();

				ControllerTracesIterator iter = new ControllerTracesIterator(
					codebase.getDatafilePath(),
					controllerName
				);

				while (iter.hasMoreTraces()) {
					TraceDto t = iter.nextTrace();

					calculateControllerSequences(
						entitiesSeq,
						clusterAccess,
						controller,
						t.getAccesses()
					);
				}

				entitiesSeq.put(clusterAccess);
				controller.addEntitiesSeq(entitiesSeq);
			}
		}
	}

	public List<Cluster> getClusters() {
		return this.clusters;
	}

	public void addCluster(Cluster cluster) {
		this.clusters.add(cluster);
	}

	public void mergeClusters(
		String cluster1,
		String cluster2,
		String newName
	) {
		Cluster mergedCluster = new Cluster(newName);

		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster1)) {

				for (Entity entity : clusters.get(i).getEntities().values())
					mergedCluster.addEntity(entity);

				clusters.remove(i);

				break;
			}
		}

		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster2)) {

				for (Entity entity : clusters.get(i).getEntities().values())
					mergedCluster.addEntity(entity);
				clusters.remove(i);

				break;
			}

		}
		this.addCluster(mergedCluster);
		this.calculateMetrics();
	}

	public void renameCluster(String clusterName, String newName) {
		if (clusterName.equals(newName))
			return;

		for (Cluster cluster : this.clusters) {
			if (cluster.getName().equals(newName))
				throw new KeyAlreadyExistsException();
		}

		for (Cluster cluster : this.clusters) {
			if (cluster.getName().equals(clusterName)) {
				cluster.setName(newName);
				break;
			}
		}

		this.calculateMetrics();
	}

	public Cluster getCluster(String clusterName) {
		for (Cluster cluster : this.clusters)
			if (cluster.getName().equals(clusterName))
				return cluster;
		return null;
	}

	public Cluster getClusterWithEntity(String entityName) {
		for (Cluster cluster : this.clusters) {
			if (cluster.containsEntity(entityName))
				return cluster;
		}
		return null;
	}

	public void splitCluster(String clusterName, String newName, String[] entities) {
		Cluster currentCluster = this.getCluster(clusterName);
		Cluster newCluster = new Cluster(newName);
		for (String entity : entities) {
			newCluster.addEntity(currentCluster.getEntity(entity));
			currentCluster.removeEntity(entity);
		}
		this.addCluster(newCluster);
		this.calculateMetrics();
	}

	public void transferEntities(String fromCluster, String toCluster, String[] entities) {
		Cluster c1 = this.getCluster(fromCluster);
		Cluster c2 = this.getCluster(toCluster);
		for (String entity : entities) {
			c2.addEntity(c1.getEntity(entity));
			c1.removeEntity(entity);
		}
		this.calculateMetrics();
	}

	public Map<String,List<Controller>> getClusterControllers() {
		Map<String,List<Controller>> clusterControllers = new HashMap<>();

		for (Cluster cluster : this.clusters) {
			List<Controller> touchedControllers = new ArrayList<>();

			for (Controller controller : this.controllers) {

				for (String controllerEntity : controller.getEntities().keySet()) {
					if (cluster.containsEntity(controllerEntity)) {
						touchedControllers.add(controller);
						break;
					}
				}
			}
			clusterControllers.put(cluster.getName(), touchedControllers);
		}
		return clusterControllers;
	}

	public Map<String,List<Cluster>> getControllerClusters() {
		Map<String,List<Cluster>> controllerClusters = new HashMap<>();

		for (Controller controller : this.controllers) {
			List<Cluster> touchedClusters = new ArrayList<>();

			for (Cluster cluster : this.clusters) {

				for (Entity clusterEntity : cluster.getEntities().values()) {
					if (controller.containsEntity(clusterEntity.getName())) {
						touchedClusters.add(cluster);
						break;
					}
				}
			}
			controllerClusters.put(controller.getName(), touchedClusters);
		}
		return controllerClusters;
	}

	public void calculateMetrics() {
		try {
			Codebase codebase = CodebaseManager.getInstance().getCodebase(this.codebaseName);

			if (codebase.isStatic()) {
				this.addStaticControllers(codebase.getDendrogram(this.dendrogramName).getProfiles(), null);

			} else {
				this.addDynamicControllers(codebase.getDendrogram(this.dendrogramName).getProfiles());
			}

		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
	}

	public void calculateMetricsAnalyser(List<String> profiles, HashMap<String, ControllerDto> datafileJSON) {
		try {
			this.addStaticControllers(profiles, datafileJSON);
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
	}

	public void calculateDynamicMetricsAnalyser(List<String> profiles) {
		try {
			this.addDynamicControllers(profiles);
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
	}
}