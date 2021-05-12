package redesign

import (
	"app/files"
	"app/metrics"
	"app/training"
	"fmt"
	"sort"
	"strconv"
	"sync"

	"github.com/go-kit/kit/log"
)

const (
	// add only the best redesign as a line in the CSV data
	onlyExportBestRedesign = false
	// number of accesses previous to the invocation that will be taken into account to assert dependency
	// if set to 0, there will be a dependency if the previous cluster does any R, and the next one does a W
	previousReadDistanceThreshold             = 0
	printTraces                               = false
	printSpecificFunctionality                = ""
	excludeLowDistanceRedesigns               = false
	acceptable_complexity_distance_percentage = 0.30
	minimizeSumBothComplexities               = false
)

var (
	wg       sync.WaitGroup
	mapMutex = sync.RWMutex{}
)

type RedesignHandler interface {
	EstimateCodebaseOrchestrators(*files.Codebase, map[string]string, float32, bool, []string) *files.Datasets
	CreateSagaRedesigns(*files.Decomposition, *files.Controller, *files.FunctionalityRedesign) ([]*files.FunctionalityRedesign, error)
	RedesignControllerUsingRules(*files.Controller, *files.FunctionalityRedesign, *files.Cluster) *files.FunctionalityRedesign
}

type DefaultHandler struct {
	logger          log.Logger
	metricsHandler  metrics.MetricsHandler
	trainingHandler training.TrainingHandler
}

func New(logger log.Logger, metricsHandler metrics.MetricsHandler, trainingHandler training.TrainingHandler) RedesignHandler {
	return &DefaultHandler{
		logger:          log.With(logger, "module", "redesignHandler"),
		metricsHandler:  metricsHandler,
		trainingHandler: trainingHandler,
	}
}

func (svc *DefaultHandler) shouldUseController(controllerName string, controllersToUse []string) bool {
	if len(controllersToUse) != 0 {
		for _, name := range controllersToUse {
			if name == controllerName {
				return true
			}
		}
		return false
	}

	return true
}

func (svc *DefaultHandler) EstimateCodebaseOrchestrators(codebase *files.Codebase, idToEntityMap map[string]string, cutValue float32, useExpertDecompositions bool, controllersToUse []string) *files.Datasets {
	datasets := &files.Datasets{
		MetricsDataset:      [][]string{},
		ComplexitiesDataset: [][]string{},
	}

	for _, dendogram := range codebase.Dendrograms {
		decomposition := dendogram.GetDecomposition(cutValue, useExpertDecompositions)
		if decomposition == nil {
			svc.logger.Log("Failed to get decomposition from dendogram")
			continue
		}

		// Add to each cluster, the list of controllers that use it
		for _, controller := range decomposition.Controllers {
			if !svc.shouldUseController(controller.Name, controllersToUse) {
				continue
			}

			wg.Add(1)
			go func(controller *files.Controller) {
				defer wg.Done()
				for clusterName := range controller.EntitiesPerCluster {
					clusterID, _ := strconv.Atoi(clusterName)
					cluster := decomposition.GetClusterFromID(clusterID)
					mapMutex.Lock()
					cluster.AddController(controller)
					mapMutex.Unlock()
				}
			}(controller)
		}
		wg.Wait()

		for _, controller := range decomposition.Controllers {
			if !svc.shouldUseController(controller.Name, controllersToUse) || controller.Name == "VirtualEditionController.createTopicModelling" || controller.Type == "QUERY" {
				continue
			}

			wg.Add(1)
			go func(controller *files.Controller) {
				defer wg.Done()
				if len(controller.EntitiesPerCluster) <= 2 {
					//svc.logger.Log("In order to decide the best redesign the controller must have more than 2 clusters.. Skiping %s", controller.Name)
					return
				}

				initialRedesign := controller.GetFunctionalityRedesign()
				svc.metricsHandler.CalculateDecompositionMetrics(decomposition, controller, initialRedesign)

				controllerTrainingFeatures := svc.trainingHandler.CalculateControllerTrainingFeatures(initialRedesign)

				sagaRedesigns, _ := svc.CreateSagaRedesigns(decomposition, controller, initialRedesign)

				// check if the distance of the best and second best is high enough
				// if not, remove from dataset
				if excludeLowDistanceRedesigns {
					bestRedesign := sagaRedesigns[0]
					secondBestRedesign := sagaRedesigns[1]
					worstRedesign := sagaRedesigns[len(sagaRedesigns)-1]
					distance := float32(secondBestRedesign.FunctionalityComplexity-bestRedesign.FunctionalityComplexity) / float32(worstRedesign.FunctionalityComplexity-bestRedesign.FunctionalityComplexity)
					if distance < acceptable_complexity_distance_percentage {
						fmt.Printf("\nWill not add functionality %s to dataset\n", controller.Name)
						return
					}
				}

				for idx, redesign := range sagaRedesigns {
					if idx == 0 {
						datasets.MetricsDataset = svc.trainingHandler.AddDataToTrainingDataset(datasets.MetricsDataset, codebase, controller, controllerTrainingFeatures, redesign, idToEntityMap)
					}

					datasets.ComplexitiesDataset = svc.addResultToDataset(
						datasets.ComplexitiesDataset,
						codebase,
						controller,
						initialRedesign,
						redesign,
						redesign.OrchestratorID,
						idToEntityMap,
						controllerTrainingFeatures,
					)

					if printTraces && ((printSpecificFunctionality == "" && idx == 0) || (controller.Name == printSpecificFunctionality)) {
						fmt.Printf("\n\n---------- %v ----------\n\n", controller.Name)
						fmt.Printf("Initial redesign\n\n")
						svc.printRedesignTrace(initialRedesign.Redesign, idToEntityMap)

						fmt.Printf("\n\nSAGA\n")
						svc.printRedesignTrace(redesign.Redesign, idToEntityMap)

						fmt.Printf("\nFunctionality Complexity: %v\n", redesign.FunctionalityComplexity)
					}

					if onlyExportBestRedesign {
						break
					}
				}
			}(controller)
		}
		wg.Wait()
	}

	return datasets
}

func (svc *DefaultHandler) printRedesignTrace(invocations []*files.Invocation, idToEntityMap map[string]string) {
	for _, invocation := range invocations {
		fmt.Printf("\n- %v  (%v)\n", invocation.ClusterID, invocation.Type)
		for idx, _ := range invocation.ClusterAccesses {
			fmt.Printf("%v (%v) | ", idToEntityMap[strconv.Itoa(invocation.GetAccessEntityID(idx))], invocation.GetAccessType(idx))
		}
	}
}

func (svc *DefaultHandler) addResultToDataset(
	data [][]string, codebase *files.Codebase, controller *files.Controller, initialRedesign *files.FunctionalityRedesign,
	bestRedesign *files.FunctionalityRedesign, orchestratorID int, idToEntityMap map[string]string, initialMetrics map[int]*training.ClusterMetrics,
) [][]string {
	clusterName := strconv.Itoa(orchestratorID)
	entityNames := []string{}
	for _, entityID := range controller.EntitiesPerCluster[clusterName] {
		entityNames = append(entityNames, idToEntityMap[strconv.Itoa(entityID)])
	}

	entityNamesCSVFormat := ""
	for _, name := range entityNames {
		entityNamesCSVFormat += name + ", "
	}

	systemComplexityReduction := initialRedesign.SystemComplexity - bestRedesign.SystemComplexity
	functionalityComplexityReduction := initialRedesign.FunctionalityComplexity - bestRedesign.FunctionalityComplexity

	orchestratorMetrics := initialMetrics[orchestratorID]

	data = append(data, []string{
		codebase.Name,
		controller.Name,
		strconv.Itoa(orchestratorID),
		entityNamesCSVFormat,
		strconv.Itoa(initialRedesign.SystemComplexity),
		strconv.Itoa(bestRedesign.SystemComplexity),
		strconv.Itoa(systemComplexityReduction),
		strconv.Itoa(initialRedesign.FunctionalityComplexity),
		strconv.Itoa(bestRedesign.FunctionalityComplexity),
		strconv.Itoa(functionalityComplexityReduction),
		strconv.Itoa(initialRedesign.InvocationsCount),
		strconv.Itoa(bestRedesign.InitialInvocationsCount),
		strconv.Itoa(bestRedesign.InvocationsCount),
		strconv.Itoa(bestRedesign.MergedInvocationsCount),
		strconv.Itoa(initialRedesign.AccessesCount),
		strconv.Itoa(bestRedesign.AccessesCount),
		strconv.Itoa(bestRedesign.RecursiveIterations),
		strconv.Itoa(bestRedesign.ClustersBesidesOrchestratorWithMultipleInvocations),
		fmt.Sprintf("%f", orchestratorMetrics.LockInvocationProbability),
		fmt.Sprintf("%f", orchestratorMetrics.ReadInvocationProbability),
		fmt.Sprintf("%f", orchestratorMetrics.ReadOperationProbability),
		fmt.Sprintf("%f", orchestratorMetrics.WriteOperationProbability),
		fmt.Sprintf("%f", orchestratorMetrics.InvocationProbability),
		fmt.Sprintf("%f", orchestratorMetrics.DataDependentInvocationProbability),
		fmt.Sprintf("%f", orchestratorMetrics.OperationProbability),
		fmt.Sprintf("%f", orchestratorMetrics.PivotInvocationFactor),
		fmt.Sprintf("%f", orchestratorMetrics.InvocationOperationFactor),
		fmt.Sprintf("%f", orchestratorMetrics.SystemComplexityContributionPercentage),
		fmt.Sprintf("%f", orchestratorMetrics.FunctionalityComplexityContributionPercentage),
	})

	return data
}

func (svc *DefaultHandler) CreateSagaRedesigns(decomposition *files.Decomposition, controller *files.Controller, initialRedesign *files.FunctionalityRedesign) ([]*files.FunctionalityRedesign, error) {
	sagaRedesigns := []*files.FunctionalityRedesign{}

	for clusterName := range controller.EntitiesPerCluster {
		cluster := decomposition.Clusters[clusterName]

		redesign := svc.RedesignControllerUsingRules(controller, initialRedesign, cluster)

		svc.metricsHandler.CalculateDecompositionMetrics(decomposition, controller, redesign)

		orchestratorID, _ := strconv.Atoi(clusterName)
		redesign.OrchestratorID = orchestratorID

		sagaRedesigns = append(sagaRedesigns, redesign)
	}

	// order the redesigns by ascending complexity
	sort.Slice(sagaRedesigns, func(i, j int) bool {
		if minimizeSumBothComplexities {
			return sagaRedesigns[i].FunctionalityComplexity+sagaRedesigns[i].SystemComplexity < sagaRedesigns[j].FunctionalityComplexity+sagaRedesigns[j].SystemComplexity
		}

		if sagaRedesigns[i].FunctionalityComplexity == sagaRedesigns[j].FunctionalityComplexity {
			return sagaRedesigns[i].SystemComplexity < sagaRedesigns[j].SystemComplexity
		}

		return sagaRedesigns[i].FunctionalityComplexity < sagaRedesigns[j].FunctionalityComplexity
	})

	return sagaRedesigns, nil
}

func (svc *DefaultHandler) RedesignControllerUsingRules(controller *files.Controller, initialRedesign *files.FunctionalityRedesign, orchestrator *files.Cluster) *files.FunctionalityRedesign {
	redesign := &files.FunctionalityRedesign{
		Name:                    controller.Name,
		UsedForMetrics:          true,
		Redesign:                []*files.Invocation{},
		SystemComplexity:        0,
		FunctionalityComplexity: 0,
		InconsistencyComplexity: 0,
		PivotTransaction:        0,
	}

	// Initialize Invocation, set dependencies and orchestrator
	orchestratorID, _ := strconv.Atoi(orchestrator.Name)
	var invocationID int
	var prevInvocation *files.Invocation
	for _, initialInvocation := range initialRedesign.Redesign {
		if initialInvocation.ClusterID == -1 {
			continue
		}

		// if this one or the previous is not the orchestrator
		if initialInvocation.ClusterID != orchestratorID && (prevInvocation == nil || prevInvocation.ClusterID != orchestratorID) {
			// add empty orchestrator invocation
			invocation := &files.Invocation{
				Name:              fmt.Sprintf("%d: %d", invocationID, orchestratorID),
				ID:                invocationID,
				ClusterID:         orchestratorID,
				ClusterAccesses:   [][]interface{}{},
				RemoteInvocations: []int{},
				Type:              "RETRIABLE",
			}

			redesign.Redesign = append(redesign.Redesign, invocation)
			invocationID++
		}

		// add actual invocation
		invocationType := initialInvocation.GetTypeFromAccesses()
		invocation := &files.Invocation{
			Name:              fmt.Sprintf("%d: %d", invocationID, initialInvocation.ClusterID),
			ID:                invocationID,
			ClusterID:         initialInvocation.ClusterID,
			ClusterAccesses:   initialInvocation.ClusterAccesses,
			RemoteInvocations: []int{},
			Type:              invocationType,
		}

		redesign.Redesign = append(redesign.Redesign, invocation)
		prevInvocation = invocation
		invocationID++
	}

	redesign.InitialInvocationsCount = len(redesign.Redesign)

	// while any merge is done, iterate all the invocations
	mergedInvocations := 1
	for mergedInvocations > 0 {
		fmt.Printf("Functionality %s will be scanned for merges... | Invocations: %d\n", controller.Name, len(redesign.Redesign))
		redesign.Redesign, mergedInvocations = svc.mergeAllPossibleInvocations(redesign)

		if mergedInvocations > 0 {
			redesign.RecursiveIterations += 1
		}
	}

	return redesign
}

func (svc *DefaultHandler) mergeAllPossibleInvocations(redesign *files.FunctionalityRedesign) ([]*files.Invocation, int) {
	var mergeCount int
	var deleted int
	var isLast bool
	prevClusterInvocations := map[int][]int{}

	invocations := redesign.Redesign

	for idx := 0; idx < len(invocations); idx++ {
		var addToPreviousInvocations bool
		invocation := invocations[idx]

		prevInvocations, exists := prevClusterInvocations[invocation.ClusterID]

		//hasAccesses := len(invocation.ClusterAccesses) > 0

		if !exists {
			addToPreviousInvocations = true
		} else {
			prevInvocationIdx := prevInvocations[len(prevInvocations)-1]
			if idx == len(invocations)-1 {
				isLast = true
			}

			if !svc.isMergeableWithPrevious(invocations, prevInvocationIdx, idx, isLast) {
				addToPreviousInvocations = true
			} else {
				invocations, prevClusterInvocations, deleted = svc.mergeInvocations(invocations, prevClusterInvocations, prevInvocationIdx, idx)
				svc.pruneInvocationAccesses(invocations[prevInvocationIdx])

				redesign.MergedInvocationsCount += 1
				mergeCount += 1

				// fix prevInvocations map after merge changes
				for cluster, prevInvocations := range prevClusterInvocations {
					for prevIdx, prevID := range prevInvocations {
						if prevID > idx-deleted {
							prevClusterInvocations[cluster][prevIdx] = prevID - deleted
						}
					}
				}

				idx -= deleted
			}
		}

		if addToPreviousInvocations {
			prevClusterInvocations[invocation.ClusterID] = append(prevClusterInvocations[invocation.ClusterID], idx)
		}
	}

	return invocations, mergeCount
}

func (svc *DefaultHandler) isMergeableWithPrevious(
	invocations []*files.Invocation, prevInvocationIdx int, invocationIdx int, isLast bool,
) bool {
	if len(invocations[invocationIdx].ClusterAccesses) == 0 && !isLast && prevInvocationIdx != invocationIdx-1 {
		return false
	}

	// if the invocation is just R, it can be merged
	if !invocations[invocationIdx].ContainsLock() || prevInvocationIdx == invocationIdx-1 {
		return true
	}

	prevInvocation := invocations[invocationIdx-1]
	// if the previous is an empty orchestrator call we take into consideration the one before
	if len(prevInvocation.ClusterAccesses) == 0 && invocationIdx > 1 {
		prevInvocation = invocations[invocationIdx-2]
	}

	if previousReadDistanceThreshold == 0 {
		return !prevInvocation.ContainsRead()
	}

	var distance int
	for idx := len(prevInvocation.ClusterAccesses) - 1; idx >= 0; idx-- {
		if prevInvocation.GetAccessType(idx) == "R" || prevInvocation.GetAccessType(idx) == "RW" {
			return false
		}

		distance++
		if distance == previousReadDistanceThreshold {
			break
		}
	}

	return true
}

func (svc *DefaultHandler) mergeInvocations(
	invocations []*files.Invocation, prevInvocations map[int][]int, prevInvocationIdx int, invocationIdx int,
) ([]*files.Invocation, map[int][]int, int) {
	newInvocations := []*files.Invocation{}
	var invocationID int
	var deletedCount int

	for idx, invocation := range invocations {
		if idx == prevInvocationIdx {
			// append the accesses to the previous invocation
			for _, access := range invocations[invocationIdx].ClusterAccesses {
				invocations[prevInvocationIdx].ClusterAccesses = append(invocations[prevInvocationIdx].ClusterAccesses, access)
			}
		}

		var removeInvocation bool
		// if its the invocation to merge or the invocation previous to the one to merge and its empty
		if idx == invocationIdx || (idx == invocationIdx-1 && len(invocation.ClusterAccesses) == 0) {
			// check if its empty (its the orchestrator) and can be deleted
			removeInvocation = true
			deletedCount++
		}

		if removeInvocation {
			var newPrevInvocations []int
			for _, prevIdx := range prevInvocations[invocations[idx].ClusterID] {
				if prevIdx != idx {
					newPrevInvocations = append(newPrevInvocations, prevIdx)
				}
			}

			prevInvocations[invocations[idx].ClusterID] = newPrevInvocations
		} else {
			invocation.ID = invocationID
			newInvocations = append(newInvocations, invocation)
			invocationID++
		}
	}

	return newInvocations, prevInvocations, deletedCount
}

func (svc *DefaultHandler) pruneInvocationAccesses(invocation *files.Invocation) {
	previousEntityAccesses := map[int]string{}
	newAccesses := [][]interface{}{}

	var containsLock bool

	for idx := range invocation.ClusterAccesses {
		entity := invocation.GetAccessEntityID(idx)
		accessType := invocation.GetAccessType(idx)

		if accessType == "W" {
			containsLock = true
		}

		previousAccessType, exists := previousEntityAccesses[entity]
		if !exists {
			previousEntityAccesses[entity] = accessType
			continue
		} else if previousAccessType == "R" && accessType == "W" {
			previousEntityAccesses[entity] = "RW"
			continue
		} else if previousAccessType == "W" && accessType == "R" {
			previousEntityAccesses[entity] = "WR"
			continue
		} else if previousAccessType == "WR" && accessType == "W" {
			previousEntityAccesses[entity] = "RW"
			continue
		}
	}

	for entity, accessType := range previousEntityAccesses {
		if accessType == "WR" {
			accessType = "W"
		}
		newAccesses = append(newAccesses, []interface{}{accessType, entity})
	}

	if containsLock {
		invocation.Type = "COMPENSATABLE"
	} else {
		invocation.Type = "RETRIABLE"
	}
	invocation.ClusterAccesses = newAccesses
	return
}
