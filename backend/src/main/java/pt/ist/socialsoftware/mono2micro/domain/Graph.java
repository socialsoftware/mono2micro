package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.json.JSONException;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.ControllerDto;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.ControllerTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Metrics;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.GraphDeserializer;
import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.util.*;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonDeserialize(using = GraphDeserializer.class)
public class Graph {
	private String codebaseName;
	private String dendrogramName;
	private String name;
	private boolean expert;
	private float cutValue;
	private String cutType;
	private float silhouetteScore;
	private float complexity;
	private float performance;
	private float cohesion;
	private float coupling;
	private List<Controller> controllers = new ArrayList<>();
	private List<Cluster> clusters = new ArrayList<>();

	public Graph() { }

	public String getCodebaseName() { return this.codebaseName; }

	public void setCodebaseName(String codebaseName) { this.codebaseName = codebaseName; }

	public String getDendrogramName() { return this.dendrogramName; }

	public void setDendrogramName(String dendrogramName) {
		this.dendrogramName = dendrogramName;
	}

	public String getName() { return this.name; }

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

	public float getPerformance() { return performance; }

	public void setPerformance(float performance) { this.performance = performance; }

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

	public List<Cluster> getClusters() { return this.clusters; }

	public void setClusters(List<Cluster> clusters) { this.clusters = clusters; }

	public void addCluster(Cluster cluster) { this.clusters.add(cluster); }

	public List<Controller> getControllers() { return this.controllers; }

	public void setControllers(List<Controller> controllers) { this.controllers = controllers; }

	public void addController(Controller controller) { this.controllers.add(controller); }

	public int maxClusterSize() {
		int max = 0;

		for (Cluster cluster : this.clusters) {
			if (cluster.getEntities().size() > max)
				max = cluster.getEntities().size();
		}

		return max;
	}

	private <A extends AccessDto> void calculateControllerSequences (
		Controller controller,
		List<A> accesses
	) {

		Controller.LocalTransaction lt = null;
		List<Controller.LocalTransaction> ltList = new ArrayList<>();
		Map<String, String> entityNameToMode = new HashMap<>();

		String previousCluster = ""; // IntelliJ is afraid. poor him

		int localTransactionsCounter = 1;

//		JSONArray entitiesSeq = new JSONArray();
//		JSONObject clusterAccess = new JSONObject();

		for (int i = 0; i < accesses.size(); i++) {
			A access = accesses.get(i);
			String entity = access.getEntity();
			String mode = access.getMode();
			String cluster;

			try {
				cluster = this.getClusterWithEntity(entity).getName();
			}
			catch (Exception e) {
				System.err.println("Expert cut does not assign entity " + entity + " to a cluster.");
				throw e;
			}

			if (i == 0) {
				lt = new Controller.LocalTransaction(
					localTransactionsCounter++,
					cluster,
					new ArrayList<AccessDto>() { { add(access); } }
				);

				controller.addEntity(entity, mode);
				entityNameToMode.put(entity, mode);

//				clusterAccess.put("cluster", cluster);
//				clusterAccess.put("sequence", new JSONArray());
//				clusterAccess.getJSONArray("sequence").put(
//					new JSONArray().put(entity).put(mode)
//				);

			} else {

				if (cluster.equals(previousCluster)) {
					boolean hasCost = false;
					String savedMode = entityNameToMode.get(entity);

					if (savedMode == null) {
						hasCost = true;

					} else {
						if (savedMode.equals("R") && mode.equals("W"))
							hasCost = true;
					}

					if (hasCost) {
						lt.addClusterAccess(access);
						controller.addEntity(entity, mode);
						entityNameToMode.put(entity, mode);

//						clusterAccess.getJSONArray("sequence").put(
//							new JSONArray().put(entity).put(mode)
//						);
					}

				} else {
					ltList.add(new Controller.LocalTransaction(lt));

					lt = new Controller.LocalTransaction(
						localTransactionsCounter++,
						cluster,
						new ArrayList<AccessDto>() { { add(access); } }
					);

					controller.addEntity(entity, mode);

					entityNameToMode.clear();
					entityNameToMode.put(entity, mode);

//					entitiesSeq.put(clusterAccess);
//					clusterAccess = new JSONObject();
//					clusterAccess.put("cluster", cluster);
//					clusterAccess.put("sequence", new JSONArray());
//					clusterAccess.getJSONArray("sequence").put(
//						new JSONArray().put(entity).put(mode)
//					);
				}
			}

			previousCluster = cluster;
		}

//		entitiesSeq.put(clusterAccess);
//		controller.addEntitiesSeq(entitiesSeq);
//		controller.createFunctionalityRedesign(Constants.DEFAULT_REDESIGN_NAME, true);

		if (lt != null && lt.getClusterAccesses().size() > 0)
			ltList.add(lt);

		if (ltList.size() > 0) {
			// we can piggyback the local transaction list size to know how many hops happened between different clusters
			controller.setPerformance(ltList.size());

			controller.addLocalTransactionSequence(ltList);
		}
	}

	public void addStaticControllers(
		List<String> profiles,
		HashMap<String, ControllerDto> datafile
	)
		throws IOException
	{
		System.out.println("Adding static controllers...");

		this.controllers = new ArrayList<>();

		HashMap<String, ControllerDto> datafileJSON;

		if (datafile == null)
			datafileJSON = CodebaseManager.getInstance().getDatafile(this.codebaseName);
		else
			datafileJSON = datafile;

		Codebase codebase = CodebaseManager.getInstance().getCodebaseWithFields(
			codebaseName,
			new HashSet<String>() {{ add("profiles"); }}
		);

		for (String profile : profiles) {
			for (String controllerName : codebase.getProfile(profile)) {
				ControllerDto controllerDto = datafileJSON.get(controllerName);
				List<AccessDto> controllerAccesses = controllerDto.getControllerAccesses();

				Controller controller = new Controller(controllerName);
				if (controllerAccesses.size() > 0) {
//					Controller controller = new Controller(controllerName);
					this.addController(controller);

					calculateControllerSequences(
						controller,
						controllerAccesses
					);
				}
			}
		}
	}

	public void addDynamicControllers(
		List<String> profiles,
		int tracesMaxLimit,
		Constants.TypeOfTraces typeOfTraces
	)
		throws IOException
	{
		System.out.println("Adding dynamic controllers...");

		this.controllers = new ArrayList<>();

		Codebase codebase = CodebaseManager.getInstance().getCodebaseWithFields(
			codebaseName,
			new HashSet<String>() {{ add("profiles"); add("datafilePath"); }}
		);

		ControllerTracesIterator iter;
		TraceDto t;
		List<AccessDto> traceAccesses;

		for (String profile : profiles) {
			for (String controllerName : codebase.getProfile(profile)) {
				iter = new ControllerTracesIterator(
					codebase.getDatafilePath(),
					controllerName,
					tracesMaxLimit
				);

				Controller controller = new Controller(controllerName);

				switch (typeOfTraces) {
					case LONGEST:
						t = iter.getLongestTrace();

						if (t != null) {
							traceAccesses = t.expand();

							if (traceAccesses.size() > 0)
								calculateControllerSequences(controller, traceAccesses);
						}

						break;

					case WITH_MORE_DIFFERENT_ACCESSES:
						t = iter.getTraceWithMoreDifferentAccesses();

						if (t != null) {
							traceAccesses = t.expand();

							if (traceAccesses.size() > 0)
								calculateControllerSequences(controller, traceAccesses);
						}

						break;

					case REPRESENTATIVE:
						Set<String> tracesIds = iter.getRepresentativeTraces();
						iter.reset();

						while (iter.hasMoreTraces()) {
							t = iter.nextTrace();
							traceAccesses = t.expand();

							if (tracesIds.contains(String.valueOf(t.getId())) && traceAccesses.size() > 0)
								calculateControllerSequences(controller, traceAccesses);
						}

						break;

					default:
						while (iter.hasMoreTraces()) {
							t = iter.nextTrace();
							traceAccesses = t.expand();

							if (traceAccesses.size() > 0)
								calculateControllerSequences(controller, traceAccesses);
						}
				}

				if (controller.getEntities().size() > 0) {
					this.addController(controller);
				}
			}
		}
	}

	public void mergeClusters(
		String cluster1,
		String cluster2,
		String newName
	) throws Exception {
		Cluster mergedCluster = new Cluster(newName);

		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster1)) {

				for (String entity : clusters.get(i).getEntities())
					mergedCluster.addEntity(entity);

				clusters.remove(i);

				break;
			}
		}

		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getName().equals(cluster2)) {

				for (String entity : clusters.get(i).getEntities())
					mergedCluster.addEntity(entity);

				clusters.remove(i);

				break;
			}

		}
		this.addCluster(mergedCluster);
		this.calculateMetrics();
	}

	public void renameCluster(String clusterName, String newName) throws Exception {
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

	public void splitCluster(String clusterName, String newName, String[] entities) throws Exception {
		Cluster currentCluster = this.getCluster(clusterName);
		Cluster newCluster = new Cluster(newName);
		for (String entity : entities) {
			if (currentCluster.containsEntity(entity)) {
				newCluster.addEntity(entity);
				currentCluster.removeEntity(entity);
			}
		}
		this.addCluster(newCluster);
		this.calculateMetrics();
	}

	public void transferEntities(String fromClusterName, String toClusterName, String[] entities) throws Exception {
		Cluster fromCluster = this.getCluster(fromClusterName);
		Cluster toCluster = this.getCluster(toClusterName);

		for (String entity : entities) {
			if (fromCluster.containsEntity(entity)) {
				toCluster.addEntity(entity);
				fromCluster.removeEntity(entity);
			}
		}

		this.calculateMetrics();
	}

	@JsonIgnore
	public Map<String,List<Controller>> getClusterControllers() {
		Map<String, List<Controller>> clusterControllers = new HashMap<>();

		for (Cluster cluster : this.clusters) {
			List<Controller> touchedControllers = new ArrayList<>();

			for (Controller controller : this.controllers) {
				if (controller != null) {
					for (String controllerEntity : controller.getEntities().keySet()) {
						if (cluster.containsEntity(controllerEntity)) {
							touchedControllers.add(controller);
							break;
						}
					}
				}
			}
			clusterControllers.put(cluster.getName(), touchedControllers);
		}
		return clusterControllers;
	}

	@JsonIgnore
	public Map<String,List<Cluster>> getControllerClusters() {
		Map<String,List<Cluster>> controllerClusters = new HashMap<>();

		for (Controller controller : this.controllers) {
			List<Cluster> touchedClusters = new ArrayList<>();

			for (Cluster cluster : this.clusters) {

				for (String clusterEntity : cluster.getEntities()) {
					if (controller.containsEntity(clusterEntity)) {
						touchedClusters.add(cluster);
						break;
					}
				}
			}
			controllerClusters.put(controller.getName(), touchedClusters);
		}
		return controllerClusters;
	}

	public void calculateMetrics() throws Exception {
		CodebaseManager cm = CodebaseManager.getInstance();

		Codebase codebase = cm.getCodebaseWithFields(
			this.codebaseName,
			new HashSet<String>() {{ add("analysisType"); }}
		);

		Dendrogram d = cm.getCodebaseDendrogramWithFields(
			this.codebaseName,
			this.dendrogramName,
			new HashSet<String>() {{ add("profiles"); add("typeOfTraces"); add("tracesMaxLimit"); }}
		);

		if (codebase.isStatic()) {
			this.addStaticControllers(
				d.getProfiles(),
				null
			);

		} else {
			this.addDynamicControllers(
				d.getProfiles(),
				d.getTracesMaxLimit(),
				d.getTypeOfTraces()
			);
		}

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
	}

	public void calculateMetricsAnalyser(
		List<String> profiles,
		HashMap<String, ControllerDto> datafileJSON
	) throws IOException, JSONException {
		this.addStaticControllers(profiles, datafileJSON);

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
	}

	public void calculateDynamicMetricsAnalyser(
		List<String> profiles,
		int tracesMaxLimit,
		Constants.TypeOfTraces traceType
	) throws IOException, JSONException {
		this.addDynamicControllers(
			profiles,
			tracesMaxLimit,
			traceType
		);

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
	}

	public Controller getController(String controllerName) {

		for (Controller controller : this.controllers) {
			if(controller.getName().equals(controllerName))
				return  controller;
		}
		return null;
	}
}