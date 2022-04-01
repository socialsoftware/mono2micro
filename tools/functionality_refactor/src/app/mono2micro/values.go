package mono2micro

import (
	"strconv"
)

type Codebase struct {
	Name       string    `json:"name,omitempty"`
	Collectors []*string `json:"collectors,omitempty"`
}

type Source struct {
	Type          string              `json:"type,omitempty"`
	CodebaseName  string              `json:"codebaseName,omitempty"`
	InputFilePath string              `json:"inputFilePath,omitempty"`
	Profiles      map[string][]string `json:"profiles,omitempty"`
}

type Strategy struct {
	Type                 string   `json:"type,omitempty"`
	Name                 string   `json:"name,omitempty"`
	CodebaseName         string   `json:"codebaseName,omitempty"`
	DecompositionsNames  []string `json:"decompositionsNames,omitempty"`
	Profile              string   `json:"profile,omitempty"`
	TracesMaxLimit       string   `json:"tracesMaxLimit,omitempty"`
	TraceType            string   `json:"traceType,omitempty"`
	AccessMetricWeight   string   `json:"accessMetricWeight,omitempty"`
	WriteMetricWeight    string   `json:"writeMetricWeight,omitempty"`
	ReadMetricWeight     string   `json:"readMetricWeight,omitempty"`
	SequenceMetricWeight string   `json:"sequenceMetricWeight,omitempty"`
	LinkageType          string   `json:"linkageType,omitempty"`
}

type Decomposition struct {
	StrategyType        string                 `json:"strategyType,omitempty"`
	Name                string                 `json:"name,omitempty"`
	CodebaseName        string                 `json:"codebaseName,omitempty"`
	StrategyName        string                 `json:"strategyName,omitempty"`
	Expert              bool                   `json:"expert,omitempty"`
	Complexity          float32                `json:"complexity,omitempty"`
	Cohesion            float32                `json:"cohesion,omitempty"`
	Coupling            float32                `json:"coupling,omitempty"`
	Clusters            map[int]*Cluster       `json:"clusters,omitempty"`
	Controllers         map[string]*Controller `json:"controllers,omitempty"`
	EntityIDToClusterID map[int]int            `json:"entityIDToClusterID,omitempty"`
}

func (d *Decomposition) GetClusterFromID(clusterID int) *Cluster {
	for id, cluster := range d.Clusters {
		if id == clusterID {
			return cluster
		}
	}
	return nil
}

func (d *Decomposition) GetEntityCluster(id int) *Cluster {
	clusterID, found := d.EntityIDToClusterID[id]
	if !found {
		return nil
	}

	cluster, found := d.Clusters[clusterID]
	if !found {
		return nil
	}

	return cluster
}

type Cluster struct {
	Id                   int                    `json:"id,omitempty"`
	Complexity           float32                `json:"complexity,omitempty"`
	Cohesion             float32                `json:"cohesion,omitempty"`
	Coupling             float32                `json:"coupling,omitempty"`
	CouplingDependencies map[int][]int          `json:"couplingDependencies,omitempty"`
	Entities             []int                  `json:"entities,omitempty"`
	Controllers          map[string]*Controller `json:"controllers,omitempty"`
}

func (c *Cluster) AddCouplingDependency(clusterID int, entityID int) {

	clusterDependencies, ok := c.CouplingDependencies[clusterID]
	if !ok {
		c.CouplingDependencies[clusterID] = []int{entityID}
		return
	}

	for _, id := range clusterDependencies {
		if id == entityID {
			return
		}
	}

	c.CouplingDependencies[clusterID] = append(c.CouplingDependencies[clusterID], entityID)
	return
}

func (c *Cluster) AddController(controller *Controller) {
	if c.Controllers == nil {
		c.Controllers = map[string]*Controller{}
	}

	_, exists := c.Controllers[controller.Name]
	if exists {
		return
	}

	c.Controllers[controller.Name] = controller
	return
}

func (c *Cluster) ContainsEntity(id int) bool {
	for entityID := range c.Entities {
		if entityID == id {
			return true
		}
	}
	return false
}

type Controller struct {
	Name                   string                   `json:"name,omitempty"`
	Type                   string                   `json:"type,omitempty"`
	Complexity             float32                  `json:"complexity,omitempty"`
	Performance            float32                  `json:"performance,omitempty"`
	Entities               map[string]int           `json:"entities,omitempty"`
	FunctionalityRedesigns []*FunctionalityRedesign `json:"functionalityRedesigns,omitempty"`
	EntitiesPerCluster     map[int][]int            `json:"entitiesPerCluster,omitempty"`
}

func (c *Controller) GetFunctionalityRedesign() *FunctionalityRedesign {
	for _, redesign := range c.FunctionalityRedesigns {
		if redesign.UsedForMetrics {
			return redesign
		}
	}
	return nil
}

func (c *Controller) GetEntityMode(id int) (int, bool) {
	entityName := strconv.Itoa(id)
	mode, exists := c.Entities[entityName]
	return mode, exists
}

func (c *Controller) EntitiesTouchedInMode(mode int) map[int]int {
	results := map[int]int{}
	for entity, accessMode := range c.Entities {
		if accessMode == mode {
			entityID, _ := strconv.Atoi(entity)
			results[entityID] = mode
		}
	}
	return results
}

type FunctionalityRedesign struct {
	Name                                               string        `json:"name,omitempty"`
	UsedForMetrics                                     bool          `json:"usedForMetrics,omitempty"`
	Redesign                                           []*Invocation `json:"redesign,omitempty"`
	SystemComplexity                                   int           `json:"systemComplexity,omitempty"`
	FunctionalityComplexity                            int           `json:"functionalityComplexity,omitempty"`
	InconsistencyComplexity                            int           `json:"inconsistencyComplexity,omitempty"`
	PivotTransaction                                   int           `json:"pivotTransaction,omitempty"`
	OrchestratorID                                     int           `json:"orchestrator_id,omitempty"`
	RecursiveIterations                                int           `json:"recursive_iterations,omitempty"`
	MergedInvocationsCount                             int           `json:"merged_invocations_count,omitempty"`
	InvocationsCount                                   int           `json:"invocations_count,omitempty"`
	ClustersBesidesOrchestratorWithMultipleInvocations int           `json:"clusters_besides_orchestrator_with_multiple_invocations,omitempty"`
	InitialInvocationsCount                            int           `json:"initial_invocations_count,omitempty"`
	AccessesCount                                      int           `json:"accesses_count,omitempty"`
}

func (f *FunctionalityRedesign) GetInvocation(idx int) *Invocation {
	return f.Redesign[idx]
}

type Invocation struct {
	Name                                  string          `json:"name,omitempty"`
	ID                                    int             `json:"id,omitempty"`
	ClusterID                             int             `json:"clusterID,omitempty"`
	ClusterAccesses                       [][]interface{} `json:"clusterAccesses,omitempty"`
	RemoteInvocations                     []int           `json:"remoteInvocations,omitempty"`
	Type                                  string          `json:"type,omitempty"`
	ControllerstThatReadInWrittenEntities int             `json:"controllerst_that_read_in_written_entities,omitempty"`
	ControllersThatWriteInReadEntities    int             `json:"controllers_that_write_in_read_entities,omitempty"`
}

func (i *Invocation) AddPrunedAccess(entity int, accessType string) {
	for _, access := range i.ClusterAccesses {
		if access[1] == entity {
			if access[0] == "R" && accessType == "W" {
				access[0] = "RW"
			}
			return
		}
	}

	i.ClusterAccesses = append(i.ClusterAccesses, []interface{}{accessType, entity})
	return
}

func (i *Invocation) GetAccessEntityID(idx int) int {
	id, ok := i.ClusterAccesses[idx][1].(int)
	if ok {
		return int(id)
	}

	return int(i.ClusterAccesses[idx][1].(float64))
}

func (i *Invocation) GetAccessType(idx int) string {
	return i.ClusterAccesses[idx][0].(string)
}

func (i *Invocation) ContainsLock() bool {
	for _, access := range i.ClusterAccesses {
		if access[0] == "W" || access[0] == "RW" {
			return true
		}
	}
	return false
}

func (i *Invocation) ContainsRead() bool {
	for _, access := range i.ClusterAccesses {
		if access[0] == "R" || access[0] == "RW" {
			return true
		}
	}
	return false
}

func (i *Invocation) GetTypeFromAccesses() string {
	if i.ContainsLock() {
		return "COMPENSATABLE"
	}
	return "RETRIABLE"
}

func MapAccessTypeToMode(accessType string) int {
	modeMap := map[string]int{
		"R":  1,
		"W":  2,
		"RW": 3,
	}

	return modeMap[accessType]
}
