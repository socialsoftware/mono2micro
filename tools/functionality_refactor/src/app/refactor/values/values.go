package values

import (
	"fmt"
	"functionality_refactor/app/mono2micro"
)

type RefactorCodebaseRequest struct {
	CodebaseName              string   `json:"codebase_name,omitempty"`
	StrategyName              string   `json:"strategy_name,omitempty"`
	DecompositionName         string   `json:"decomposition_name,omitempty"`
	FunctionalityNames        []string `json:"functionality_names,omitempty"`
	DataDependenceThreshold   int      `json:"data_dependence_threshold,omitempty"`
	MinimizeSumOfComplexities bool     `json:"minimize_sum_of_complexities,omitempty"`
	RefactorTimeOutSecs       int      `json:"refactor_time_out_secs,omitempty"`
}

func (r *RefactorCodebaseRequest) ShouldRefactorFunctionality(functionality *mono2micro.Functionality) bool {
	var shouldRefactor bool

	if len(r.FunctionalityNames) > 0 {
		for _, functionalityName := range r.FunctionalityNames {
			if functionality.Name == functionalityName {
				shouldRefactor = true
			}
		}
		return shouldRefactor
	}

	shouldRefactor = true
	if functionality.Type == "QUERY" {
		fmt.Printf("wont refactor %s because its a Query\n", functionality.Name)
		return false
	}

	if len(functionality.EntitiesPerCluster) <= 2 {
		fmt.Printf("wont refactor %s because it has less than 3 clusters\n", functionality.Name)
		return false
	}

	return shouldRefactor
}

type RefactorCodebaseResponse struct {
	CodebaseName            string                    `json:"codebase_name"`
	StrategyName            string                    `json:"strategy_name"`
	DecompositionName       string                    `json:"decomposition_name"`
	Functionalities         map[string]*Functionality `json:"functionalities"`
	DataDependenceThreshold int                       `json:"data_dependence_threshold"`
	Status                  string                    `json:"status"`
}

type Functionality struct {
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
	ClusterID int      `json:"cluster_id"`
	Accesses  []Access `json:"accesses"`
}

type Access struct {
	EntityID int    `json:"entity_id"`
	Type     string `json:"type"`
}
