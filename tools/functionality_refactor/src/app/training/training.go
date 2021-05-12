package training

import (
	"app/files"
	"fmt"
	"strconv"

	"github.com/go-kit/kit/log"
)

type TrainingHandler interface {
	CalculateControllerTrainingFeatures(*files.FunctionalityRedesign) map[int]*ClusterMetrics
	AddDataToTrainingDataset([][]string, *files.Codebase, *files.Controller, map[int]*ClusterMetrics, *files.FunctionalityRedesign, map[string]string) [][]string
}

type DefaultHandler struct {
	logger log.Logger
}

func New(logger log.Logger) TrainingHandler {
	return &DefaultHandler{
		logger: log.With(logger, "module", "trainingHandler"),
	}
}

func (svc *DefaultHandler) CalculateControllerTrainingFeatures(redesign *files.FunctionalityRedesign) map[int]*ClusterMetrics {
	featureMetrics := FeatureMetrics{}
	clusterMetrics := make(map[int]*ClusterMetrics)

	var accessType string
	for index, invocation := range redesign.Redesign {
		if invocation.ClusterID == -1 {
			continue
		}

		var metrics *ClusterMetrics
		var ok bool
		metrics, ok = clusterMetrics[invocation.ClusterID]
		if !ok {
			metrics = &ClusterMetrics{
				ClusterID:     invocation.ClusterID,
				InvocationIds: []int{index},
			}
			clusterMetrics[invocation.ClusterID] = metrics
			featureMetrics.Clusters += 1
		}

		metrics.InvocationIds = append(metrics.InvocationIds, index)

		metrics.ControllersThatWriteInReadEntities += invocation.ControllersThatWriteInReadEntities
		metrics.ControllerstThatReadInWrittenEntities += invocation.ControllerstThatReadInWrittenEntities

		var containsSemanticLock bool
		for idx := range invocation.ClusterAccesses {
			accessType = invocation.GetAccessType(idx)

			if accessType == "R" {
				metrics.ReadOperations += 1
				featureMetrics.ReadOperations += 1
			} else if accessType == "W" {
				metrics.WriteOperations += 1
				featureMetrics.WriteOperations += 1
				containsSemanticLock = true
			}

			metrics.Operations += 1
			featureMetrics.Operations += 1
		}

		if containsSemanticLock {
			metrics.LockInvocations += 1
			featureMetrics.LockInvocations += 1
		} else {
			metrics.ReadInvocations += 1
			featureMetrics.ReadInvocations += 1
		}

		if index > 0 && redesign.Redesign[index-1].ContainsLock() {
			metrics.DataDependentInvocations += 1
		}

		metrics.Invocations += 1
		featureMetrics.Invocations += 1
	}

	svc.calculateFinalClusterMetrics(&featureMetrics, clusterMetrics, redesign)
	return clusterMetrics
}

func (svc *DefaultHandler) calculateFinalClusterMetrics(featureMetrics *FeatureMetrics, clusterMetrics map[int]*ClusterMetrics, redesign *files.FunctionalityRedesign) {
	for _, metrics := range clusterMetrics {
		metrics.PivotInvocations = featureMetrics.Invocations - metrics.Invocations - metrics.InvocationIds[0] - (featureMetrics.Invocations - metrics.InvocationIds[len(metrics.InvocationIds)-1] - 1)

		metrics.AverageInvocationOperations = float32(metrics.Operations) / float32(metrics.Invocations)
		metrics.AverageInvocationReadOperations = float32(metrics.ReadOperations) / float32(metrics.Invocations)
		metrics.AverageInvocationWriteOperations = float32(metrics.WriteOperations) / float32(metrics.Invocations)

		if metrics.Invocations > 1 {
			metrics.AveragePivotInvocations = float32(metrics.PivotInvocations) / float32((metrics.Invocations - 1))
		}

		metrics.LockInvocationProbability = float32(metrics.LockInvocations) / float32(metrics.Invocations)
		metrics.ReadInvocationProbability = float32(metrics.ReadInvocations) / float32(metrics.Invocations)
		metrics.DataDependentInvocationProbability = float32(metrics.DataDependentInvocations) / float32(metrics.Invocations)
		metrics.ReadOperationProbability = float32(metrics.ReadOperations) / float32(metrics.Operations)
		metrics.WriteOperationProbability = float32(metrics.WriteOperations) / float32(metrics.Operations)
		metrics.InvocationProbability = float32(metrics.Invocations) / float32(featureMetrics.Invocations)
		metrics.OperationProbability = float32(metrics.Operations) / float32(featureMetrics.Operations)

		featureMetrics.AverageInvocationOperations += float32(metrics.AverageInvocationOperations) / float32(featureMetrics.Clusters)
		featureMetrics.AverageInvocationReadOperations += float32(metrics.AverageInvocationReadOperations) / float32(featureMetrics.Clusters)
		featureMetrics.AverageInvocationWriteOperations += float32(metrics.AverageInvocationWriteOperations) / float32(featureMetrics.Clusters)
		featureMetrics.AveragePivotInvocations += float32(metrics.AveragePivotInvocations) / float32(featureMetrics.Clusters)
	}

	for _, metrics := range clusterMetrics {
		metrics.PivotInvocationFactor = float32(metrics.AveragePivotInvocations) / float32(featureMetrics.AveragePivotInvocations)
		metrics.InvocationOperationFactor = float32(metrics.AverageInvocationOperations) / float32(featureMetrics.AverageInvocationOperations)

		metrics.SystemComplexityContributionPercentage = float32(metrics.ControllerstThatReadInWrittenEntities) / float32(redesign.SystemComplexity)
		metrics.FunctionalityComplexityContributionPercentage = float32(metrics.ControllersThatWriteInReadEntities) / float32(redesign.FunctionalityComplexity)
	}
}

func (svc *DefaultHandler) AddDataToTrainingDataset(
	data [][]string, codebase *files.Codebase, controller *files.Controller, clusterMetrics map[int]*ClusterMetrics, redesign *files.FunctionalityRedesign, idToEntityMap map[string]string,
) [][]string {
	for cluster, metrics := range clusterMetrics {
		var result int
		if redesign.OrchestratorID == cluster {
			result = 1
		}

		clusterName := strconv.Itoa(cluster)
		entityNames := []string{}
		for _, entityID := range controller.EntitiesPerCluster[clusterName] {
			entityNames = append(entityNames, idToEntityMap[strconv.Itoa(entityID)])
		}

		entityNamesCSVFormat := ""
		for _, name := range entityNames {
			entityNamesCSVFormat += name + ", "
		}

		data = append(data, []string{
			codebase.Name,
			controller.Name,
			//controller.Type,
			strconv.Itoa(cluster),
			//entityNamesCSVFormat,
			fmt.Sprintf("%f", metrics.LockInvocationProbability),
			fmt.Sprintf("%f", metrics.ReadInvocationProbability),
			fmt.Sprintf("%f", metrics.ReadOperationProbability),
			fmt.Sprintf("%f", metrics.WriteOperationProbability),
			fmt.Sprintf("%f", metrics.InvocationProbability),
			fmt.Sprintf("%f", metrics.DataDependentInvocationProbability),
			fmt.Sprintf("%f", metrics.OperationProbability),
			fmt.Sprintf("%f", metrics.PivotInvocationFactor),
			fmt.Sprintf("%f", metrics.InvocationOperationFactor),
			fmt.Sprintf("%f", metrics.SystemComplexityContributionPercentage),
			fmt.Sprintf("%f", metrics.FunctionalityComplexityContributionPercentage),
			strconv.Itoa(result),
		})
	}

	return data
}
