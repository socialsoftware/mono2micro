package metrics

import (
	"app/files"
	"strconv"
	"sync"

	"github.com/go-kit/kit/log"
)

const (
	ReadMode      = 1
	WriteMode     = 2
	ReadWriteMode = 3
	Compensatable = "COMPENSATABLE"
	Saga          = "SAGA"
	Query         = "QUERY"
)

var (
	mapMutex = sync.RWMutex{}
)

type MetricsHandler interface {
	CalculateDecompositionMetrics(*files.Decomposition, *files.Controller, *files.FunctionalityRedesign)
	CalculateControllerComplexityAndDependencies(*files.Decomposition, *files.Controller, *files.FunctionalityRedesign)
	CalculateClusterComplexityAndCohesion(*files.Cluster)
	CalculateRedesignComplexities(*files.Decomposition, *files.Controller, *files.FunctionalityRedesign)
}

type DefaultHandler struct {
	logger log.Logger
}

func New(logger log.Logger) MetricsHandler {
	return &DefaultHandler{
		logger: log.With(logger, "module", "codebaseHandler"),
	}
}

func (svc *DefaultHandler) CalculateDecompositionMetrics(decomposition *files.Decomposition, controller *files.Controller, redesign *files.FunctionalityRedesign) {
	var complexity float32
	var cohesion float32
	var coupling float32

	for _, controller := range decomposition.Controllers {
		svc.CalculateControllerComplexityAndDependencies(decomposition, controller, redesign)
		svc.CalculateRedesignComplexities(decomposition, controller, redesign)
		complexity += controller.Complexity
	}

	for _, cluster := range decomposition.Clusters {
		svc.CalculateClusterComplexityAndCohesion(cluster)
		cohesion += cluster.Cohesion

		// svc.CalculateClusterCoupling(cluster)
		// coupling += cluster.Coupling
	}

	cluster_invocations := map[int]int{}
	for _, invocation := range redesign.Redesign {
		redesign.AccessesCount += len(invocation.ClusterAccesses)

		if len(invocation.ClusterAccesses) > 0 {
			redesign.InvocationsCount += 1
		}

		_, exists := cluster_invocations[invocation.ClusterID]
		if !exists {
			cluster_invocations[invocation.ClusterID] = 1
		} else {
			cluster_invocations[invocation.ClusterID] += 1
		}
	}

	for cluster_id, count := range cluster_invocations {
		if count > 1 && cluster_id != redesign.OrchestratorID {
			redesign.ClustersBesidesOrchestratorWithMultipleInvocations += 1
		}
	}

	decomposition.Complexity = complexity / float32(len(decomposition.Controllers))
	decomposition.Cohesion = cohesion / float32(len(decomposition.Clusters))
	decomposition.Coupling = coupling / float32(len(decomposition.Clusters))
}

func (svc *DefaultHandler) CalculateControllerComplexityAndDependencies(decomposition *files.Decomposition, controller *files.Controller, redesign *files.FunctionalityRedesign) {
	if len(controller.EntitiesPerCluster) <= 1 {
		controller.Complexity = 0
		return
	}

	var complexity float32
	for idx, invocation := range redesign.Redesign {
		if invocation.ClusterID == -1 {
			continue
		}

		cluster := decomposition.GetClusterFromID(invocation.ClusterID)
		for i := idx; i < len(redesign.Redesign); i++ {
			if len(redesign.GetInvocation(i).ClusterAccesses) > 0 {
				mapMutex.Lock()
				cluster.AddCouplingDependency(
					redesign.GetInvocation(i).ClusterID,
					redesign.GetInvocation(i).GetAccessEntityID(0),
				)
				mapMutex.Unlock()
			}
		}

		if len(invocation.ClusterAccesses) == 0 {
			continue
		}

		controllersTouchingSameEntities := map[string]bool{}
		for i := range invocation.ClusterAccesses {
			mode := files.MapAccessTypeToMode(invocation.GetAccessType(i))
			controllers := svc.controllersThatTouchEntity(decomposition, controller, invocation.GetAccessEntityID(i), mode)

			for _, controller := range controllers {
				_, alreadySaved := controllersTouchingSameEntities[controller]
				if !alreadySaved {
					controllersTouchingSameEntities[controller] = true
				}
			}
		}
		complexity += float32(len(controllersTouchingSameEntities))
	}

	controller.Complexity = float32(complexity)
	return
}

func (svc *DefaultHandler) controllersThatTouchEntity(decomposition *files.Decomposition, controller *files.Controller, entityID int, mode int) []string {
	var controllers []string

	for _, otherController := range decomposition.Controllers {
		entityMode, containsEntity := otherController.GetEntityMode(entityID)
		if otherController.Name == controller.Name || len(otherController.EntitiesPerCluster) <= 1 || !containsEntity {
			continue
		}

		if entityMode != mode {
			controllers = append(controllers, otherController.Name)
		}
	}

	return controllers
}

func (svc *DefaultHandler) CalculateClusterComplexityAndCohesion(cluster *files.Cluster) {
	var complexity float32
	var cohesion float32
	var numberEntitiesTouched float32

	for _, controller := range cluster.Controllers {
		for entityName := range controller.Entities {
			entityID, _ := strconv.Atoi(entityName)
			if cluster.ContainsEntity(entityID) {
				numberEntitiesTouched++
			}
		}

		cohesion += numberEntitiesTouched / float32(len(cluster.Entities))
		complexity += controller.Complexity
	}

	complexity /= float32(len(cluster.Controllers))
	cluster.Complexity = complexity

	cohesion /= float32(len(cluster.Controllers))
	cluster.Cohesion = cohesion
	return
}

func (svc *DefaultHandler) CalculateRedesignComplexities(decomposition *files.Decomposition, controller *files.Controller, redesign *files.FunctionalityRedesign) {
	if controller.Type == Query {
		svc.queryRedesignComplexity(decomposition, controller, redesign)
	} else {
		svc.sagasRedesignComplexity(decomposition, controller, redesign)
	}
}

func (svc *DefaultHandler) queryRedesignComplexity(decomposition *files.Decomposition, controller *files.Controller, redesign *files.FunctionalityRedesign) {
	entitiesRead := controller.EntitiesTouchedInMode(files.MapAccessTypeToMode("R"))

	var inconsistencyComplexity int
	for _, otherController := range decomposition.Controllers {
		var entitiesReadThatAreWrittenInOther []int
		var clustersInCommon []*files.Cluster

		if otherController.Name == controller.Name || len(otherController.EntitiesPerCluster) <= 1 || otherController.Type != "SAGA" {
			continue
		}

		entitiesWritten := otherController.EntitiesTouchedInMode(files.MapAccessTypeToMode("W"))
		for entity := range entitiesRead {
			_, written := entitiesWritten[entity]
			if written {
				entitiesReadThatAreWrittenInOther = append(entitiesReadThatAreWrittenInOther, entity)
			}
		}

		for entityID := range entitiesReadThatAreWrittenInOther {
			cluster := decomposition.GetEntityCluster(entityID)
			clustersInCommon = append(clustersInCommon, cluster)
		}

		if len(clustersInCommon) > 1 {
			inconsistencyComplexity += len(clustersInCommon)
		}
	}

	redesign.InconsistencyComplexity = inconsistencyComplexity
}

func (svc *DefaultHandler) sagasRedesignComplexity(decomposition *files.Decomposition, controller *files.Controller, redesign *files.FunctionalityRedesign) {
	var functionalityComplexity int
	var systemComplexity int

	for _, invocation := range redesign.Redesign {
		var controllerstThatReadInWrittenEntities int
		var controllersThatWriteInReadEntities int

		for i := range invocation.ClusterAccesses {
			entity := invocation.GetAccessEntityID(i)
			mode := files.MapAccessTypeToMode(invocation.GetAccessType(i))

			if mode >= WriteMode { // 2 -> W, 3 -> RW
				if invocation.Type == "COMPENSATABLE" {
					systemComplexityResult := svc.systemComplexity(decomposition, controller, redesign, entity)

					controllerstThatReadInWrittenEntities += systemComplexityResult
					systemComplexity += systemComplexityResult

					functionalityComplexity++
				}
			}

			if mode != WriteMode { // 1 -> R
				costOfRead := svc.costOfRead(decomposition, controller, redesign, entity)

				controllersThatWriteInReadEntities += costOfRead

				functionalityComplexity += costOfRead
			}
		}

		invocation.ControllerstThatReadInWrittenEntities = controllerstThatReadInWrittenEntities
		invocation.ControllersThatWriteInReadEntities = controllersThatWriteInReadEntities
	}

	redesign.FunctionalityComplexity = functionalityComplexity
	redesign.SystemComplexity = systemComplexity
}

func (svc *DefaultHandler) systemComplexity(decomposition *files.Decomposition, controller *files.Controller, redesign *files.FunctionalityRedesign, entity int) int {
	var systemComplexity int

	for _, otherController := range decomposition.Controllers {
		mode, containsEntity := otherController.GetEntityMode(entity)
		if otherController.Name == controller.Name || !containsEntity || mode == WriteMode {
			continue
		}

		systemComplexity++
	}

	return systemComplexity
}

func (svc *DefaultHandler) costOfRead(decomposition *files.Decomposition, controller *files.Controller, redesign *files.FunctionalityRedesign, entity int) int {
	var functionalityComplexity int

	for _, otherController := range decomposition.Controllers {
		mode, containsEntity := otherController.GetEntityMode(entity)
		if otherController.Name == controller.Name || len(otherController.EntitiesPerCluster) <= 1 || !containsEntity {
			continue
		}

		mode, exists := otherController.GetEntityMode(entity)
		if exists && mode >= WriteMode {
			functionalityComplexity++
		}
	}

	return functionalityComplexity
}
