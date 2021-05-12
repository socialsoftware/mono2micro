package redesign_test

import (
	"app/common/log"
	"app/files"
	"app/metrics"
	"app/redesign"
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

func newRedesignHandler() redesign.RedesignHandler {
	logger := log.NewLogger()
	metricsHandler := metrics.New(logger)
	return redesign.New(logger, metricsHandler)
}

func TestRedesignUsingRules(t *testing.T) {
	handler := newRedesignHandler()

	controller := &files.Controller{
		Name:                   "Controller",
		Type:                   "",
		Complexity:             0,
		Performance:            0,
		Entities:               map[string]int{},
		FunctionalityRedesigns: []*files.FunctionalityRedesign{},
		EntitiesPerCluster:     map[string][]int{},
	}

	initialRedesign := &files.FunctionalityRedesign{
		Name:           "Monolith",
		UsedForMetrics: true,
		Redesign: []*files.Invocation{
			{
				Name:      "AdminController.loadTEIFragmentsAtOnce",
				ID:        0,
				ClusterID: -1,
				Type:      "COMPENSATABLE",
			},
			{
				Name:      "1: 0",
				ID:        1,
				ClusterID: 0,
				ClusterAccesses: [][]interface{}{
					{
						"R", 1,
					},
				},
				RemoteInvocations: []int{},
				Type:              "RETRIABLE",
			},
			{
				Name:      "2: 1",
				ID:        2,
				ClusterID: 1,
				ClusterAccesses: [][]interface{}{
					{
						"R", 2,
					},
				},
				RemoteInvocations: []int{},
				Type:              "RETRIABLE",
			},
			{
				Name:      "3: 2",
				ID:        3,
				ClusterID: 2,
				ClusterAccesses: [][]interface{}{
					{
						"W", 3,
					},
					{
						"W", 4,
					},
				},
				RemoteInvocations: []int{},
				Type:              "COMPENSATABLE",
			},
			{
				Name:      "4: 1",
				ID:        4,
				ClusterID: 1,
				ClusterAccesses: [][]interface{}{
					{
						"W", 2,
					},
				},
				RemoteInvocations: []int{},
				Type:              "COMPENSATABLE",
			},
			{
				Name:      "5: 0",
				ID:        5,
				ClusterID: 0,
				ClusterAccesses: [][]interface{}{
					{
						"W", 5,
					},
				},
				RemoteInvocations: []int{},
				Type:              "COMPENSATABLE",
			},
		},
	}

	orchestrator := &files.Cluster{
		Name:                 "0",
		Complexity:           0,
		Cohesion:             0,
		Coupling:             0,
		CouplingDependencies: map[string][]int{},
		Entities:             []int{1, 5},
	}

	result := handler.RedesignControllerUsingRules(controller, initialRedesign, orchestrator)

	expectation := &files.FunctionalityRedesign{
		Name:           "currentRedesign",
		UsedForMetrics: true,
		Redesign: []*files.Invocation{
			{
				Name:      "0: 0",
				ID:        0,
				ClusterID: 0,
				ClusterAccesses: [][]interface{}{
					{
						"R", 1,
					},
				},
				RemoteInvocations: []int{},
				Type:              "RETRIABLE",
			},
			{
				Name:      "1: 1",
				ID:        1,
				ClusterID: 1,
				ClusterAccesses: [][]interface{}{
					{
						"RW", 2,
					},
				},
				RemoteInvocations: []int{},
				Type:              "COMPENSATABLE",
			},
			{
				Name:      "2: 0",
				ID:        2,
				ClusterID: 0,
				ClusterAccesses: [][]interface{}{
					{
						"W", 5,
					},
				},
				RemoteInvocations: []int{},
				Type:              "COMPENSATABLE",
			},
			{
				Name:      "3: 2",
				ID:        3,
				ClusterID: 2,
				ClusterAccesses: [][]interface{}{
					{
						"W", 3,
					},
					{
						"W", 4,
					},
				},
				RemoteInvocations: []int{},
				Type:              "COMPENSATABLE",
			},
		},
	}

	for _, invocation := range result.Redesign {
		fmt.Printf("\n%s | idx: %d\n", invocation.Name, invocation.ID)
		for _, access := range invocation.ClusterAccesses {
			fmt.Printf("%v %v\n", access[0], access[1])
		}

		fmt.Printf("\n")
	}

	assert.Equal(t, expectation, result)
}
