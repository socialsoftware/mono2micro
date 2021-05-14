package redesign

import (
	"app/configuration"
	"app/files"
	"app/metrics"
	"app/training"
	"fmt"
	"sort"
	"strconv"
	"sync"

	"github.com/go-kit/kit/log"
)

var (
	wg       sync.WaitGroup
	mapMutex = sync.RWMutex{}
)

const (
	ONLY_LAST_INVOCATION     = 0
	ALL_PREVIOUS_INVOCATIONS = -1
)

type RedesignHandler interface {
	EstimateCodebaseOrchestrators(*files.Codebase, map[string]string, configuration.CodebaseConfiguration) *configuration.Datasets
	CreateSagaRedesigns(*files.Decomposition, *files.Controller, *files.FunctionalityRedesign) ([]*files.FunctionalityRedesign, error)
	RefactorController(*files.Controller, *files.FunctionalityRedesign, *files.Cluster) *files.FunctionalityRedesign
}

type DefaultHandler struct {
	logger          log.Logger
	metricsHandler  metrics.MetricsHandler
	trainingHandler training.TrainingHandler
	execution       configuration.Execution
}

func New(
	logger log.Logger, metricsHandler metrics.MetricsHandler, trainingHandler training.TrainingHandler, execution configuration.Execution,
) RedesignHandler {
	return &DefaultHandler{
		logger:          log.With(logger, "module", "redesignHandler"),
		metricsHandler:  metricsHandler,
		trainingHandler: trainingHandler,
		execution:       execution,
	}
}

func (svc *DefaultHandler) extractValidControllers(decomposition *files.Decomposition, codebaseConfig configuration.CodebaseConfiguration) map[string]*files.Controller {
	validControllers := map[string]*files.Controller{}
	for _, controller := range decomposition.Controllers {
		if !codebaseConfig.ShouldRefactorController(controller.Name, controller.Type, len(controller.EntitiesPerCluster)) {
			continue
		}

		wg.Add(1)
		go func(controller *files.Controller, validControllers map[string]*files.Controller) {
			defer wg.Done()
			for clusterName := range controller.EntitiesPerCluster {
				clusterID, _ := strconv.Atoi(clusterName)
				cluster := decomposition.GetClusterFromID(clusterID)
				mapMutex.Lock()
				cluster.AddController(controller)
				mapMutex.Unlock()
			}
			mapMutex.Lock()
			validControllers[controller.Name] = controller
			mapMutex.Unlock()
		}(controller, validControllers)
	}
	wg.Wait()
	return validControllers
}

func (svc *DefaultHandler) EstimateCodebaseOrchestrators(
	codebase *files.Codebase, idToEntityMap map[string]string, codebaseConfig configuration.CodebaseConfiguration,
) *configuration.Datasets {
	datasets := &configuration.Datasets{
		MetricsDataset:      [][]string{},
		ComplexitiesDataset: [][]string{},
	}

	for _, dendogram := range codebase.Dendrograms {
		decomposition := dendogram.GetDecomposition(
			codebaseConfig.CutValue, codebaseConfig.UseExpertDecompositions,
		)
		if decomposition == nil {
			svc.logger.Log("Failed to get decomposition from dendogram")
			continue
		}

		// Add to each cluster, the list of controllers that use it
		validControllers := svc.extractValidControllers(decomposition, codebaseConfig)

		for _, controller := range validControllers {
			wg.Add(1)
			go func(controller *files.Controller) {
				defer wg.Done()
				initialRedesign := controller.GetFunctionalityRedesign()
				svc.metricsHandler.CalculateDecompositionMetrics(decomposition, controller, initialRedesign)
				controllerTrainingFeatures := svc.trainingHandler.CalculateControllerTrainingFeatures(initialRedesign)

				sagaRedesigns, _ := svc.CreateSagaRedesigns(decomposition, controller, initialRedesign)

				// check if the distance of the best and second best is high enough
				// if not, remove from dataset
				if svc.execution.Configuration.ExcludeLowDistanceRedesigns {
					bestRedesign := sagaRedesigns[0]
					secondBestRedesign := sagaRedesigns[1]
					worstRedesign := sagaRedesigns[len(sagaRedesigns)-1]
					distance := float32(secondBestRedesign.FunctionalityComplexity-bestRedesign.FunctionalityComplexity) / float32(worstRedesign.FunctionalityComplexity-bestRedesign.FunctionalityComplexity)
					if distance < svc.execution.Configuration.AcceptableComplexityDistanceThreshold {
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

					if svc.execution.Configuration.PrintTraces && ((svc.execution.Configuration.PrintSpecificFunctionality == "" && idx == 0) || (controller.Name == svc.execution.Configuration.PrintSpecificFunctionality)) {
						fmt.Printf("\n\n---------- %v ----------\n\n", controller.Name)
						fmt.Printf("Initial redesign\n\n")
						svc.printRedesignTrace(initialRedesign.Redesign, idToEntityMap)

						fmt.Printf("\n\nSAGA\n")
						svc.printRedesignTrace(redesign.Redesign, idToEntityMap)

						fmt.Printf("\nFunctionality Complexity: %v\n", redesign.FunctionalityComplexity)
					}

					if svc.execution.Configuration.OnlyExportBestRedesign {
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

		redesign := svc.RefactorController(controller, initialRedesign, cluster)

		svc.metricsHandler.CalculateDecompositionMetrics(decomposition, controller, redesign)

		orchestratorID, _ := strconv.Atoi(clusterName)
		redesign.OrchestratorID = orchestratorID

		sagaRedesigns = append(sagaRedesigns, redesign)
	}

	// order the redesigns by ascending complexity
	sort.Slice(sagaRedesigns, func(i, j int) bool {
		if svc.execution.Configuration.MinimizeSumBothComplexities {
			return sagaRedesigns[i].FunctionalityComplexity+sagaRedesigns[i].SystemComplexity < sagaRedesigns[j].FunctionalityComplexity+sagaRedesigns[j].SystemComplexity
		}

		if sagaRedesigns[i].FunctionalityComplexity == sagaRedesigns[j].FunctionalityComplexity {
			return sagaRedesigns[i].SystemComplexity < sagaRedesigns[j].SystemComplexity
		}

		return sagaRedesigns[i].FunctionalityComplexity < sagaRedesigns[j].FunctionalityComplexity
	})

	return sagaRedesigns, nil
}

func (svc *DefaultHandler) RefactorController(controller *files.Controller, initialRedesign *files.FunctionalityRedesign, orchestrator *files.Cluster) *files.FunctionalityRedesign {
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
	redesign = svc.addOrchestratorPivotInvocations(orchestratorID, initialRedesign, redesign)

	// while any merge is done, iterate all the invocations
	var mergedInvocations int
	iterate := true
	for iterate {
		fmt.Println("Doing merge iteration for controller ", controller.Name)

		redesign.Redesign, mergedInvocations = svc.mergeAllPossibleInvocations(redesign)
		redesign.RecursiveIterations += 1

		if mergedInvocations < 1 {
			iterate = false
		}
	}

	return redesign
}

func (svc *DefaultHandler) addOrchestratorPivotInvocations(
	orchestratorID int, initialRedesign *files.FunctionalityRedesign, newRedesign *files.FunctionalityRedesign,
) *files.FunctionalityRedesign {
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
				Type:              "COMPENSATABLE",
			}

			newRedesign.Redesign = append(newRedesign.Redesign, invocation)
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

		newRedesign.Redesign = append(newRedesign.Redesign, invocation)
		prevInvocation = invocation
		invocationID++
	}

	newRedesign.InitialInvocationsCount = len(newRedesign.Redesign)
	return newRedesign
}

func (svc *DefaultHandler) mergeAllPossibleInvocations(redesign *files.FunctionalityRedesign) ([]*files.Invocation, int) {
	var mergeCount int
	var deleted int
	prevClusterInvocations := map[int][]int{}

	invocations := redesign.Redesign

	for originalInvocationIdx := 0; originalInvocationIdx < len(invocations); originalInvocationIdx++ {
		var addToPreviousInvocations bool
		originalInvocation := invocations[originalInvocationIdx]

		prevInvocations, exists := prevClusterInvocations[originalInvocation.ClusterID]

		if !exists {
			addToPreviousInvocations = true
		} else {
			destinyInvocationIdx := prevInvocations[len(prevInvocations)-1]

			if svc.isMergeForbidden(invocations, destinyInvocationIdx, originalInvocationIdx) {
				addToPreviousInvocations = true
			} else {
				invocations, prevClusterInvocations, deleted = svc.mergeInvocations(invocations, prevClusterInvocations, destinyInvocationIdx, originalInvocationIdx)
				svc.pruneInvocationAccesses(invocations[destinyInvocationIdx])

				redesign.MergedInvocationsCount += 1
				mergeCount += 1

				// fix prevInvocations map after merge changes
				for cluster, prevInvocations := range prevClusterInvocations {
					for prevIdx, prevID := range prevInvocations {
						if prevID > originalInvocationIdx-deleted {
							prevClusterInvocations[cluster][prevIdx] = prevID - deleted
						}
					}
				}

				originalInvocationIdx -= deleted
			}
		}

		if addToPreviousInvocations {
			prevClusterInvocations[originalInvocation.ClusterID] = append(prevClusterInvocations[originalInvocation.ClusterID], originalInvocationIdx)
		}
	}

	return invocations, mergeCount
}

func (svc *DefaultHandler) isMergeForbidden(
	invocations []*files.Invocation, destinyInvocationIdx int, originalInvocationIdx int,
) bool {
	var isLastInvocation bool
	if originalInvocationIdx == len(invocations)-1 {
		isLastInvocation = true
	}

	originalInvocation := invocations[originalInvocationIdx]

	if len(originalInvocation.ClusterAccesses) == 0 && !isLastInvocation && destinyInvocationIdx != originalInvocationIdx-1 {
		return true
	}

	// if the invocation is just R, it can be merged
	if !originalInvocation.ContainsLock() || destinyInvocationIdx == originalInvocationIdx-1 {
		return false
	}

	var mergeForbidden bool
	for idx := originalInvocationIdx - 1; idx >= 0; idx-- {
		pivotInvocation := invocations[idx]
		if pivotInvocation.ClusterID == originalInvocation.ClusterID {
			break
		}

		if len(pivotInvocation.ClusterAccesses) == 0 {
			continue
		}

		if svc.execution.Configuration.DataDependenceThreshold == ONLY_LAST_INVOCATION {
			mergeForbidden = pivotInvocation.ContainsRead()
			break
		} else {
			mergeForbidden = pivotInvocation.ContainsRead()
			if mergeForbidden {
				break
			}

			if (svc.execution.Configuration.DataDependenceThreshold != ALL_PREVIOUS_INVOCATIONS) && (originalInvocationIdx-idx == svc.execution.Configuration.DataDependenceThreshold) {
				break
			}
		}
	}

	return mergeForbidden
}

func (svc *DefaultHandler) mergeInvocations(
	invocations []*files.Invocation, prevInvocations map[int][]int, destinyInvocationIdx int, originalInvocationIdx int,
) ([]*files.Invocation, map[int][]int, int) {
	newInvocations := []*files.Invocation{}
	var invocationID int
	var deletedCount int

	for idx, invocation := range invocations {
		if idx == destinyInvocationIdx {
			// append the accesses to the previous invocation
			for _, access := range invocations[originalInvocationIdx].ClusterAccesses {
				invocations[destinyInvocationIdx].ClusterAccesses = append(invocations[destinyInvocationIdx].ClusterAccesses, access)
			}
		}

		var removeInvocation bool
		// if its the invocation to merge or the invocation previous to the one to merge and its empty
		if idx == originalInvocationIdx || (idx == originalInvocationIdx-1 && len(invocation.ClusterAccesses) == 0) {
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
