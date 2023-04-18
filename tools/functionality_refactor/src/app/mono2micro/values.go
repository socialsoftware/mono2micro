package mono2micro

import (
	"strconv"
)

type Codebase struct {
	Name       string    `json:"name,omitempty"`
	Collectors []*string `json:"collectors,omitempty"`
}

type Representation struct {
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
	StrategyType          string                    `json:"strategyType,omitempty"`
	Name                  string                    `json:"name,omitempty"`
	CodebaseName          string                    `json:"codebaseName,omitempty"`
	StrategyName          string                    `json:"strategyName,omitempty"`
	Expert                bool                      `json:"expert,omitempty"`
	Complexity            float64                   `json:"complexity,omitempty"`
	Cohesion              float64                   `json:"cohesion,omitempty"`
	Coupling              float64                   `json:"coupling,omitempty"`
	Clusters              map[string]*Cluster       `json:"clusters,omitempty"`
	Functionalities       map[string]*Functionality `json:"functionalities,omitempty"`
	EntityIDToClusterName map[int]string            `json:"entityIDToClusterID,omitempty"`
}

func (d *Decomposition) GetClusterFromID(clusterName string) *Cluster {
	for name, cluster := range d.Clusters {
		if name == clusterName {
			return cluster
		}
	}
	return nil
}

func (d *Decomposition) GetEntityCluster(id int) *Cluster {
	clusterName, found := d.EntityIDToClusterName[id]
	if !found {
		return nil
	}

	cluster, found := d.Clusters[clusterName]
	if !found {
		return nil
	}
	
	return cluster
}

type Cluster struct {
	Name                 string                    `json:"name,omitempty"`
	Complexity           float64                   `json:"complexity,omitempty"`
	Cohesion             float64                   `json:"cohesion,omitempty"`
	Coupling             float64                   `json:"coupling,omitempty"`
	CouplingDependencies map[string][]int          `json:"couplingDependencies,omitempty"`
	Entities             []Entity                  `json:"entities,omitempty"`
	Functionalities      map[string]*Functionality `json:"functionalities,omitempty"`
}

type Entity struct {
	Id   int    `json:"id,omitempty"`
	Name string `json:"name,omitempty"`
}

func (c *Cluster) AddCouplingDependency(clusterName string, entityID int) {

	clusterDependencies, ok := c.CouplingDependencies[clusterName]
	if !ok {
		c.CouplingDependencies[clusterName] = []int{entityID}
		return
	}

	for _, id := range clusterDependencies {
		if id == entityID {
			return
		}
	}

	c.CouplingDependencies[clusterName] = append(c.CouplingDependencies[clusterName], entityID)
	return
}

func (c *Cluster) AddFunctionality(functionality *Functionality) {
	if c.Functionalities == nil {
		c.Functionalities = map[string]*Functionality{}
	}

	_, exists := c.Functionalities[functionality.Name]
	if exists {
		return
	}

	c.Functionalities[functionality.Name] = functionality
	return
}

func (c *Cluster) ContainsEntity(id int) bool {
	for _, entity := range c.Entities {
		if entity.Id == id {
			return true
		}
	}
	return false
}

type Functionality struct {
	Name                                    string                   `json:"name,omitempty"`
	Type                                    string                   `json:"type,omitempty"`
	Complexity                              float64                  `json:"complexity,omitempty"`
	Performance                             float64                  `json:"performance,omitempty"`
	Entities                                map[string]int           `json:"entities,omitempty"`
	FunctionalityRedesigns                  []*FunctionalityRedesign `json:"functionalityRedesigns,omitempty"`
	FunctionalityRedesignNameUsedForMetrics string                   `json:"functionalityRedesignNameUsedForMetrics,omitempty"`
	EntitiesPerCluster                      map[string][]int         `json:"entitiesPerCluster,omitempty"`
}

func (c *Functionality) GetFunctionalityRedesign() *FunctionalityRedesign {
	for _, redesign := range c.FunctionalityRedesigns {
		if redesign.Name == c.FunctionalityRedesignNameUsedForMetrics {
			return redesign
		}
	}
	return nil
}

func (c *Functionality) GetEntityMode(id int) (int, bool) {
	entityName := strconv.Itoa(id)
	mode, exists := c.Entities[entityName]
	return mode, exists
}

func (c *Functionality) EntitiesTouchedInMode(mode int) map[int]int {
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
	Redesign                                           []*Invocation `json:"redesign,omitempty"`
	SystemComplexity                                   int           `json:"systemComplexity,omitempty"`
	FunctionalityComplexity                            int           `json:"functionalityComplexity,omitempty"`
	InconsistencyComplexity                            int           `json:"inconsistencyComplexity,omitempty"`
	PivotTransaction                                   int           `json:"pivotTransaction,omitempty"`
	OrchestratorName                                   string        `json:"orchestrator_name,omitempty"`
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
	Name                                     string          `json:"name,omitempty"`
	ID                                       int             `json:"id,omitempty"`
	ClusterName                              string          `json:"clusterName,omitempty"`
	ClusterAccesses                          [][]interface{} `json:"clusterAccesses,omitempty"`
	RemoteInvocations                        []int           `json:"remoteInvocations,omitempty"`
	Type                                     string          `json:"type,omitempty"`
	FunctionalitiesThatReadInWrittenEntities int             `json:"functionalities_that_read_in_written_entities,omitempty"`
	FunctionalitiesThatWriteInReadEntities   int             `json:"functionalities_that_write_in_read_entities,omitempty"`
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
