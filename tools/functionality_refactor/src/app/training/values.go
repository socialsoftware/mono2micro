package training

type EntityMetrics struct {
	ReadOperations         int     `json:"read_operations,omitempty"`
	WriteOperations        int     `json:"write_operations,omitempty"`
	Operations             int     `json:"operations,omitempty"`
	PivotOperations        int     `json:"pivot_operations,omitempty"`
	AverageOperations      float32 `json:"average_operations,omitempty"`
	AveragePivotOperations float32 `json:"average_pivot_operations,omitempty"`
}

type ClusterMetrics struct {
	ClusterID                                     int     `json:"cluster_id,omitempty"`
	InvocationIds                                 []int   `json:"invocation_ids,omitempty"`
	LockInvocations                               int     `json:"lock_invocations,omitempty"`
	ReadInvocations                               int     `json:"read_invocations,omitempty"`
	Invocations                                   int     `json:"invocations,omitempty"`
	ReadOperations                                int     `json:"read_operations,omitempty"`
	WriteOperations                               int     `json:"write_operations,omitempty"`
	Operations                                    int     `json:"operations,omitempty"`
	PivotInvocations                              int     `json:"pivot_invocations,omitempty"`
	DataDependentInvocations                      int     `json:"data_dependent_invocations,omitempty"`
	AverageInvocationOperations                   float32 `json:"average_invocation_operations,omitempty"`
	AverageInvocationReadOperations               float32 `json:"average_invocation_read_operations,omitempty"`
	AverageInvocationWriteOperations              float32 `json:"average_invocation_write_operations,omitempty"`
	AveragePivotInvocations                       float32 `json:"average_pivot_invocations,omitempty"`
	LockInvocationProbability                     float32 `json:"lock_invocation_probability,omitempty"`
	ReadInvocationProbability                     float32 `json:"read_invocation_probability,omitempty"`
	ReadOperationProbability                      float32 `json:"read_operation_probability,omitempty"`
	WriteOperationProbability                     float32 `json:"write_operation_probability,omitempty"`
	InvocationProbability                         float32 `json:"invocation_probability,omitempty"`
	DataDependentInvocationProbability            float32 `json:"data_dependent_invocation_probability,omitempty"`
	OperationProbability                          float32 `json:"operation_probability,omitempty"`
	PivotInvocationFactor                         float32 `json:"pivot_invocation_factor,omitempty"`
	InvocationOperationFactor                     float32 `json:"invocation_operation_factor,omitempty"`
	ControllerstThatReadInWrittenEntities         int     `json:"controllerst_that_read_in_written_entities,omitempty"`
	ControllersThatWriteInReadEntities            int     `json:"controllers_that_write_in_read_entities,omitempty"`
	FunctionalityComplexityContributionPercentage float32
	SystemComplexityContributionPercentage        float32
}

type FeatureMetrics struct {
	Clusters                         int     `json:"clusters,omitempty"`
	LockInvocations                  int     `json:"lock_invocations,omitempty"`
	ReadInvocations                  int     `json:"read_invocations,omitempty"`
	Invocations                      int     `json:"invocations,omitempty"`
	ReadOperations                   int     `json:"read_operations,omitempty"`
	WriteOperations                  int     `json:"write_operations,omitempty"`
	Operations                       int     `json:"operations,omitempty"`
	AverageInvocationOperations      float32 `json:"average_invocation_operations,omitempty"`
	AverageInvocationReadOperations  float32 `json:"average_invocation_read_operations,omitempty"`
	AverageInvocationWriteOperations float32 `json:"average_invocation_write_operations,omitempty"`
	AveragePivotInvocations          float32 `json:"average_pivot_invocations,omitempty"`
}
