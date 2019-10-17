package pt.ist.socialsoftware.mono2micro.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Graph;

public class Metrics {
    private Graph graph;

    public Metrics(Graph graph) {
        this.graph = graph;
    }

    public void calculateMetrics() {
        Map<String,List<Controller>> clusterControllers = graph.getClusterControllers();
		float graphComplexity = 0;
		float graphCohesion = 0;
		float graphCoupling = 0;
		
		for (Controller controller : graph.getControllers()) {
			calculateControllerComplexity(controller);
			graphComplexity += controller.getComplexity();
		}
		graphComplexity /= graph.getControllers().size();
		graphComplexity = BigDecimal.valueOf(graphComplexity).setScale(2, RoundingMode.HALF_UP).floatValue();
		this.graph.setComplexity(graphComplexity);
            
		for (Cluster cluster : graph.getClusters()) {
			calculateClusterComplexity(cluster, clusterControllers.get(cluster.getName()));

			calculateClusterCohesion(cluster, clusterControllers.get(cluster.getName()));
			graphCohesion += cluster.getCohesion();

			calculateClusterCoupling(cluster);
			graphCoupling += cluster.getCoupling();
		}
		graphCohesion /= graph.getClusters().size();
		graphCohesion = BigDecimal.valueOf(graphCohesion).setScale(2, RoundingMode.HALF_UP).floatValue();
		this.graph.setCohesion(graphCohesion);
		graphCoupling /= graph.getClusters().size();
		graphCoupling = BigDecimal.valueOf(graphCoupling).setScale(2, RoundingMode.HALF_UP).floatValue();
		this.graph.setCoupling(graphCoupling);
    }

    private void calculateControllerComplexity(Controller controller) {
		if (graph.getControllerClusters().get(controller.getName()).size() == 1) {
			controller.setComplexity(0);
			return;	
		}

		float complexity = 0;
		Set<String> clusterReadCost = new HashSet<>();
		Set<String> clusterWriteCost = new HashSet<>();
		try {
			JSONArray accessSequence = new JSONArray(controller.getEntitiesSeq());

			for (int i = 0; i < accessSequence.length(); i++) {
				JSONObject clusterAccess = accessSequence.getJSONObject(i);
				JSONArray entitiesSequence = clusterAccess.getJSONArray("sequence");
				for (int j = 0; j < entitiesSequence.length(); j++) {
					JSONArray entityAccess = entitiesSequence.getJSONArray(j);
					String entity = entityAccess.getString(0);
					String mode = entityAccess.getString(1);

					if (mode.equals("R"))
						costOfRead(controller, entity, clusterReadCost);
					else
						costOfWrite(controller, entity, clusterWriteCost);
				}

				complexity += clusterReadCost.size();
				complexity += clusterWriteCost.size();
				clusterReadCost.clear();
				clusterWriteCost.clear();
			}

			controller.setComplexity(complexity);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }

    private void costOfRead(Controller controller, String entity, Set<String> clusterReadCost) {
        for (Controller otherController : this.graph.getControllers()) {
            if (!otherController.getName().equals(controller.getName()) &&
                otherController.containsEntity(entity) && 
                otherController.getEntities().get(entity).contains("W")) {
                    clusterReadCost.add(otherController.getName());
                }
        }
    }

    private void costOfWrite(Controller controller, String entity, Set<String> clusterWriteCost) {
        for (Controller otherController : this.graph.getControllers()) {
            if (!otherController.getName().equals(controller.getName()) &&
                otherController.containsEntity(entity) && 
                otherController.getEntities().get(entity).contains("R")) {
                    clusterWriteCost.add(otherController.getName());
                }
        }
    }

	private void calculateClusterComplexity(Cluster cluster, List<Controller> controllers) {
		if (graph.getClusters().size() == 1) {
			cluster.setComplexity(0);
			return;	
		}

		float complexity = 0;
		for (Controller controller : controllers) {
			complexity += controller.getComplexity();
		}
		complexity /= controllers.size();
		complexity = BigDecimal.valueOf(complexity).setScale(2, RoundingMode.HALF_UP).floatValue();
		cluster.setComplexity(complexity);
	}

	public void calculateClusterCohesion(Cluster cluster, List<Controller> controllers) {
		float cohesion = 0;
		for (Controller controller : controllers) {
			float numberEntitiesTouched = 0;
			for (String controllerEntity : controller.getEntities().keySet()) {
				if (cluster.containsEntity(controllerEntity))
					numberEntitiesTouched++;
			}
			cohesion += numberEntitiesTouched / cluster.getEntities().size();
		}
		cohesion /= controllers.size();
		cohesion = BigDecimal.valueOf(cohesion).setScale(2, RoundingMode.HALF_UP).floatValue();
		cluster.setCohesion(cohesion);
	}

	private void calculateClusterCoupling(Cluster c1) {
		float coupling = 0;
		Map<String,Float> couplingPairs = new HashMap<>();
		for (Cluster c2 : graph.getClusters()) {
			if (c1.getName().equals(c2.getName())) {
				couplingPairs.put(c2.getName(), Float.valueOf(-1));
				continue;
			}
			Set<String> touchedEntities = new HashSet<>();

			try {
				for (Controller controller : this.graph.getControllers()) {
					JSONArray accessSequence = new JSONArray(controller.getEntitiesSeq());
					for (int i = 0; i < accessSequence.length() - 1; i++) {
						JSONObject clusterAccess = accessSequence.getJSONObject(i);
						String fromCluster = clusterAccess.getString("cluster");

						JSONObject nextClusterAccess = accessSequence.getJSONObject(i+1);
						String toCluster = nextClusterAccess.getString("cluster");
						String toEntity = nextClusterAccess.getJSONArray("sequence").getJSONArray(0).getString(0);
					
						if (c1.getName().equals(fromCluster) &&
							c2.getName().equals(toCluster)) {
								touchedEntities.add(toEntity);
						}
					}
				}

				coupling += touchedEntities.size();
				couplingPairs.put(c2.getName(), Float.valueOf(touchedEntities.size()));
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
		coupling = graph.getClusters().size() == 1 ? 0 : coupling / (graph.getClusters().size() - 1);
		c1.setCoupling(coupling);
		c1.setCouplingPairs(couplingPairs);
	}
}