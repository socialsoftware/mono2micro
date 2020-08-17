package pt.ist.socialsoftware.mono2micro.domain;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import javax.management.openmbean.KeyAlreadyExistsException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.ControllerDto;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.ControllerTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Metrics;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;

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

	public Graph() { }

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

	public List<Cluster> getClusters() { return this.clusters; }

	public void addCluster(Cluster cluster) { this.clusters.add(cluster); }

	public List<Controller> getControllers() { return this.controllers; }

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
				}
			}

			previousCluster = cluster;
		}

		if (lt != null && lt.getClusterAccesses().size() > 0)
			ltList.add(lt);

		if (ltList.size() > 0)
			controller.addLocalTransactionSequence(ltList);
	}

	public void addStaticControllers(
		List<String> profiles,
		HashMap<String, ControllerDto> datafile
	)
		throws IOException
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
				ControllerDto controllerDto = datafileJSON.get(controllerName);
				List<AccessDto> controllerAccesses = controllerDto.getControllerAccesses();

				if (controllerAccesses.size() > 0) {
					Controller controller = new Controller(controllerName);
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
	) throws IOException {
		this.controllers = new ArrayList<>();

		Codebase codebase = CodebaseManager.getInstance().getCodebase(this.codebaseName);
		ControllerTracesIterator iter;
		TraceDto t;
		List<AccessDto> traceAccesses;

		ObjectMapper mapper = new ObjectMapper();
		JsonFactory jsonfactory = mapper.getFactory();
		JsonGenerator jGenerator = jsonfactory.createGenerator(
			new FileOutputStream(CODEBASES_PATH + codebaseName + "/controllerEntities.json"),
			JsonEncoding.UTF8
		);

		jGenerator.useDefaultPrettyPrinter(); // for testing purposes
		jGenerator.writeStartObject();

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
							traceAccesses = t.getAccesses();

							if (traceAccesses.size() > 0)
								calculateControllerSequences(controller, traceAccesses);
						}

						break;

					case WITH_MORE_DIFFERENT_ACCESSES:
						t = iter.getTraceWithMoreDifferentAccesses();

						if (t != null) {
							traceAccesses = t.getAccesses();

							if (traceAccesses.size() > 0)
								calculateControllerSequences(controller, traceAccesses);
						}

						break;

					case REPRESENTATIVE:
						Set<String> tracesIds = iter.getRepresentativeTraces();
						iter.reset();

						while (iter.hasMoreTraces()) {
							t = iter.nextTrace();
							traceAccesses = t.getAccesses();

							if (tracesIds.contains(String.valueOf(t.getId())) && traceAccesses.size() > 0)
								calculateControllerSequences(controller, traceAccesses);
						}

						break;

					default:
						while (iter.hasMoreTraces()) {
							t = iter.nextTrace();
							traceAccesses = t.getAccesses();

							if (traceAccesses.size() > 0)
								calculateControllerSequences(controller, traceAccesses);
						}
				}

				if (controller.getEntities().size() > 0) {
					this.addController(controller);
				}

				jGenerator.writeObjectField(controller.getName(), controller.getEntities());
			}
		}

		jGenerator.writeEndObject();
		jGenerator.close();
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
				this.addStaticControllers(
					codebase.getDendrogram(this.dendrogramName).getProfiles(),
					null
				);

			} else {
				Dendrogram d = codebase.getDendrogram(this.dendrogramName);

				this.addDynamicControllers(
					d.getProfiles(),
					d.getTracesMaxLimit(),
					d.getTypeOfTraces()
				);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
	}

	public void calculateMetricsAnalyser(List<String> profiles, HashMap<String, ControllerDto> datafileJSON) {
		try {
			this.addStaticControllers(profiles, datafileJSON);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
	}

	public void calculateDynamicMetricsAnalyser(
		List<String> profiles,
		int tracesMaxLimit,
		Constants.TypeOfTraces traceType
	) {
		try {
			this.addDynamicControllers(
				profiles,
				tracesMaxLimit,
				traceType
			);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Metrics metrics = new Metrics(this);
		metrics.calculateMetrics();
	}
}