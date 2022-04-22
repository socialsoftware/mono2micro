package metrics

import (
	"functionality_refactor/app/mono2micro"
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
	CalculateDecompositionMetrics(*mono2micro.Decomposition, *mono2micro.Functionality, *mono2micro.FunctionalityRedesign)
}

type DefaultHandler struct {
	logger log.Logger
}

func New(logger log.Logger) MetricsHandler {
	return &DefaultHandler{
		logger: log.With(logger, "module", "codebaseHandler"),
	}
}

func (svc *DefaultHandler) CalculateDecompositionMetrics(
	decomposition *mono2micro.Decomposition, functionality *mono2micro.Functionality, redesign *mono2micro.FunctionalityRedesign,
) {
	var complexity float32
	var cohesion float32
	var coupling float32

	for _, functionality := range decomposition.Functionalities {
		svc.calculateFunctionalityComplexityAndDependencies(decomposition, functionality, redesign)
		svc.calculateRedesignComplexities(decomposition, functionality, redesign)
		complexity += functionality.Complexity
	}

	for _, cluster := range decomposition.Clusters {
		svc.calculateClusterComplexityAndCohesion(cluster)
		cohesion += cluster.Cohesion

		// svc.CalculateClusterCoupling(cluster)
		// coupling += cluster.Coupling
	}

	clusterInvocations := map[int]int{}
	for _, invocation := range redesign.Redesign {
		redesign.AccessesCount += len(invocation.ClusterAccesses)

		if len(invocation.ClusterAccesses) > 0 {
			redesign.InvocationsCount += 1
		}

		_, exists := clusterInvocations[invocation.ClusterID]
		if !exists {
			clusterInvocations[invocation.ClusterID] = 1
		} else {
			clusterInvocations[invocation.ClusterID] += 1
		}
	}

	for clusterID, count := range clusterInvocations {
		if count > 1 && clusterID != redesign.OrchestratorID {
			redesign.ClustersBesidesOrchestratorWithMultipleInvocations += 1
		}
	}

	decomposition.Complexity = complexity / float32(len(decomposition.Functionalities))
	decomposition.Cohesion = cohesion / float32(len(decomposition.Clusters))
	decomposition.Coupling = coupling / float32(len(decomposition.Clusters))
}

func (svc *DefaultHandler) calculateFunctionalityComplexityAndDependencies(
	decomposition *mono2micro.Decomposition, functionality *mono2micro.Functionality, redesign *mono2micro.FunctionalityRedesign,
) {
	if len(functionality.EntitiesPerCluster) <= 1 {
		functionality.Complexity = 0
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

		functionalitiesTouchingSameEntities := map[string]bool{}
		for i := range invocation.ClusterAccesses {
			mode := mono2micro.MapAccessTypeToMode(invocation.GetAccessType(i))
			functionalities := svc.functionalitiesThatTouchEntity(decomposition, functionality, invocation.GetAccessEntityID(i), mode)

			for _, functionality := range functionalities {
				_, alreadySaved := functionalitiesTouchingSameEntities[functionality]
				if !alreadySaved {
					functionalitiesTouchingSameEntities[functionality] = true
				}
			}
		}
		complexity += float32(len(functionalitiesTouchingSameEntities))
	}

	functionality.Complexity = float32(complexity)
	return
}

func (svc *DefaultHandler) functionalitiesThatTouchEntity(
	decomposition *mono2micro.Decomposition, functionality *mono2micro.Functionality, entityID int, mode int,
) []string {
	var functionalities []string

	for _, otherFunctionality := range decomposition.Functionalities {
		entityMode, containsEntity := otherFunctionality.GetEntityMode(entityID)
		if otherFunctionality.Name == functionality.Name || len(otherFunctionality.EntitiesPerCluster) <= 1 || !containsEntity {
			continue
		}

		if entityMode != mode {
			functionalities = append(functionalities, otherFunctionality.Name)
		}
	}

	return functionalities
}

func (svc *DefaultHandler) calculateClusterComplexityAndCohesion(cluster *mono2micro.Cluster) {
	var complexity float32
	var cohesion float32
	var numberEntitiesTouched float32

	for _, functionality := range cluster.Functionalities {
		for entityName := range functionality.Entities {
			entityID, _ := strconv.Atoi(entityName)
			if cluster.ContainsEntity(entityID) {
				numberEntitiesTouched++
			}
		}

		cohesion += numberEntitiesTouched / float32(len(cluster.Entities))
		complexity += functionality.Complexity
	}

	complexity /= float32(len(cluster.Functionalities))
	cluster.Complexity = complexity

	cohesion /= float32(len(cluster.Functionalities))
	cluster.Cohesion = cohesion
	return
}

func (svc *DefaultHandler) calculateRedesignComplexities(
	decomposition *mono2micro.Decomposition, functionality *mono2micro.Functionality, redesign *mono2micro.FunctionalityRedesign,
) {
	if functionality.Type == Query {
		svc.queryRedesignComplexity(decomposition, functionality, redesign)
	} else {
		svc.sagasRedesignComplexity(decomposition, functionality, redesign)
	}
}

func (svc *DefaultHandler) queryRedesignComplexity(
	decomposition *mono2micro.Decomposition, functionality *mono2micro.Functionality, redesign *mono2micro.FunctionalityRedesign,
) {
	entitiesRead := functionality.EntitiesTouchedInMode(mono2micro.MapAccessTypeToMode("R"))

	var inconsistencyComplexity int
	for _, otherFunctionality := range decomposition.Functionalities {
		var entitiesReadThatAreWrittenInOther []int
		var clustersInCommon []*mono2micro.Cluster

		if otherFunctionality.Name == functionality.Name || len(otherFunctionality.EntitiesPerCluster) <= 1 || otherFunctionality.Type != "SAGA" {
			continue
		}

		entitiesWritten := otherFunctionality.EntitiesTouchedInMode(mono2micro.MapAccessTypeToMode("W"))
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

func (svc *DefaultHandler) sagasRedesignComplexity(
	decomposition *mono2micro.Decomposition, functionality *mono2micro.Functionality, redesign *mono2micro.FunctionalityRedesign,
) {
	var functionalityComplexity int
	var systemComplexity int

	for _, invocation := range redesign.Redesign {
		var functionalitiesThatReadInWrittenEntities int
		var functionalitiesThatWriteInReadEntities int

		for i := range invocation.ClusterAccesses {
			entity := invocation.GetAccessEntityID(i)
			mode := mono2micro.MapAccessTypeToMode(invocation.GetAccessType(i))

			if mode >= WriteMode { // 2 -> W, 3 -> RW
				if invocation.Type == "COMPENSATABLE" {
					systemComplexityResult := svc.systemComplexity(decomposition, functionality, redesign, entity)

					functionalitiesThatReadInWrittenEntities += systemComplexityResult
					systemComplexity += systemComplexityResult

					functionalityComplexity++
				}
			}

			if mode != WriteMode { // 1 -> R
				costOfRead := svc.costOfRead(decomposition, functionality, redesign, entity)

				functionalitiesThatWriteInReadEntities += costOfRead

				functionalityComplexity += costOfRead
			}
		}

		invocation.FunctionalitiesThatReadInWrittenEntities = functionalitiesThatReadInWrittenEntities
		invocation.FunctionalitiesThatWriteInReadEntities = functionalitiesThatWriteInReadEntities
	}

	redesign.FunctionalityComplexity = functionalityComplexity
	redesign.SystemComplexity = systemComplexity
}

func (svc *DefaultHandler) systemComplexity(
	decomposition *mono2micro.Decomposition, functionality *mono2micro.Functionality, redesign *mono2micro.FunctionalityRedesign, entity int,
) int {
	var systemComplexity int

	for _, otherFunctionality := range decomposition.Functionalities {
		mode, containsEntity := otherFunctionality.GetEntityMode(entity)
		if otherFunctionality.Name == functionality.Name || !containsEntity || mode == WriteMode {
			continue
		}

		systemComplexity++
	}

	return systemComplexity
}

func (svc *DefaultHandler) costOfRead(
	decomposition *mono2micro.Decomposition, functionality *mono2micro.Functionality, redesign *mono2micro.FunctionalityRedesign, entity int,
) int {
	var functionalityComplexity int

	for _, otherFunctionality := range decomposition.Functionalities {
		mode, containsEntity := otherFunctionality.GetEntityMode(entity)
		if otherFunctionality.Name == functionality.Name || len(otherFunctionality.EntitiesPerCluster) <= 1 || !containsEntity {
			continue
		}

		if mode >= WriteMode {
			functionalityComplexity++
		}
	}

	return functionalityComplexity
}
