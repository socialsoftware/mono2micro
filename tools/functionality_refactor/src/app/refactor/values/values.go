package values

import "functionality_refactor/app/files"

type RefactorCodebaseRequest struct {
	CodebaseName              string   `json:"codebase_name"`
	DendrogramName            string   `json:"dendrogram_name"`
	DecompositionName         string   `json:"decomposition_name"`
	ControllerNames           []string `json:"controller_names"`
	DataDependenceThreshold   int      `json:"data_dependence_threshold"`
	MinimizeSumOfComplexities bool     `json:"minimize_sum_of_complexities"`
	RefactorTimeOutSecs       int      `json:"refactor_time_out_secs"`
}

func (r *RefactorCodebaseRequest) ShouldRefactorController(controller *files.Controller) bool {
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
}

type Controller struct {
	Name     string    `json:"name,omitempty"`
	Monolith *Monolith `json:"monolith,omitempty"`
	Refactor *Refactor `json:"refactor,omitempty"`
	Error    string    `json:"error,omitempty"`
}

type Monolith struct {
	ComplexityMetrics *ComplexityMetrics `json:"complexity_metrics,omitempty"`
}

type Refactor struct {
	Orchestrator      *Cluster           `json:"orchestrator,omitempty"`
	ComplexityMetrics *ComplexityMetrics `json:"complexity_metrics,omitempty"`
	ExecutionMetrics  *ExecutionMetrics  `json:"execution_metrics,omitempty"`
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
