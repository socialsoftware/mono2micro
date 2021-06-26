package values

import (
	"fmt"
	"functionality_refactor/app/mono2micro"
)

type RefactorCodebaseRequest struct {
	CodebaseName              string   `json:"codebase_name,omitempty"`
	DendrogramName            string   `json:"dendrogram_name,omitempty"`
	DecompositionName         string   `json:"decomposition_name,omitempty"`
	ControllerNames           []string `json:"controller_names,omitempty"`
	DataDependenceThreshold   int      `json:"data_dependence_threshold,omitempty"`
	MinimizeSumOfComplexities bool     `json:"minimize_sum_of_complexities,omitempty"`
	RefactorTimeOutSecs       int      `json:"refactor_time_out_secs,omitempty"`
}

func (r *RefactorCodebaseRequest) ShouldRefactorController(controller *mono2micro.Controller) bool {
	var shouldRefactor bool

	if len(r.ControllerNames) > 0 {
		for _, controllerName := range r.ControllerNames {
			if controller.Name == controllerName {
				shouldRefactor = true
			}
		}
		return shouldRefactor
	}

	shouldRefactor = true
	if controller.Type == "QUERY" || len(controller.EntitiesPerCluster) <= 2 {
		fmt.Printf("wont refactor %s\n", controller.Name)
		shouldRefactor = false
	}

	return shouldRefactor
}

type RefactorCodebaseResponse struct {
	CodebaseName            string                 `json:"codebase_name"`
	DendrogramName          string                 `json:"dendogram_name"`
	DecompositionName       string                 `json:"decomposition_name"`
	Controllers             map[string]*Controller `json:"controllers"`
	DataDependenceThreshold int                    `json:"data_dependence_threshold"`
	Status                  string                 `json:"status"`
}

type Controller struct {
	Name     string    `json:"name,omitempty"`
	Monolith *Monolith `json:"monolith,omitempty"`
	Refactor *Refactor `json:"refactor,omitempty"`
	Error    string    `json:"error,"`
	Status   string    `json:"status"`
}

type Monolith struct {
	ComplexityMetrics *ComplexityMetrics `json:"complexity_metrics,omitempty"`
}

type Refactor struct {
	Orchestrator      *Cluster           `json:"orchestrator,omitempty"`
	ComplexityMetrics *ComplexityMetrics `json:"complexity_metrics,omitempty"`
	ExecutionMetrics  *ExecutionMetrics  `json:"execution_metrics,omitempty"`
	Invocations       []Invocation       `json:"call_graph,omitempty"`
}

type Cluster struct {
	Name     string `json:"name,omitempty"`
	ID       int    `json:"id,omitempty"`
	Entities []int  `json:"entities,omitempty"`
}

type ComplexityMetrics struct {
	SystemComplexity        int `json:"system_complexity,omitempty"`
	FunctionalityComplexity int `json:"functionality_complexity,omitempty"`
	InvocationsCount        int `json:"invocations_count,omitempty"`
	AccessesCount           int `json:"accesses_count,omitempty"`
}

type ExecutionMetrics struct {
	SystemComplexityReduction        int `json:"system_complexity_reduction,omitempty"`
	FunctionalityComplexityReduction int `json:"functionality_complexity_reduction,omitempty"`
	InvocationMerges                 int `json:"invocation_merges,omitempty"`
}

type ClusterMetrics struct {
	ReadAccessProbability              float32 `json:"read_access_probability,omitempty"`
	WriteAccessProbability             float32 `json:"write_access_probability,omitempty"`
	InvocationProbability              float32 `json:"invocation_probability,omitempty"`
	DataDependentInvocationProbability float32 `json:"data_dependent_invocation_probability,omitempty"`
}

type Invocation struct {
	ClusterID int      `json:"cluster_id,omitempty"`
	Accesses  []Access `json:"accesses"`
}

type Access struct {
	EntityID int    `json:"entity_id,omitempty"`
	Type     string `json:"type,omitempty"`
}
