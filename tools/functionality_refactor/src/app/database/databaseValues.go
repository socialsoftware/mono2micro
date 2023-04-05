package database

type Decomposition struct {
	Name                       string                       `bson:"_id,omitempty"`
	Expert                     bool                         `bson:"expert,omitempty"`
	Metrics                    map[string]float64           `bson:"metrics,omitempty"`
	Clusters                   map[string]*Cluster          `bson:"clusters,omitempty"`
	RepresentationInformations []RepresentationInformations `bson:"representationInformations,omitempty"`
}

type RepresentationInformations struct {
	Functionalities   map[string]*DBRef `bson:"functionalities,omitempty"`
	DecompositionName string            `bson:"decompositionName,omitempty"`
	Name              string            `bson:"_class,omitempty"`
}

type DBRef struct {
	DB  interface{} `bson:"$db"`
	Ref interface{} `bson:"$ref"`
	ID  interface{} `bson:"$id"`
}

type Cluster struct {
	Name                 string           `bson:"name,omitempty"`
	Complexity           float64          `bson:"complexity,omitempty"`
	Cohesion             float64          `bson:"cohesion,omitempty"`
	Coupling             float64          `bson:"coupling,omitempty"`
	CouplingDependencies map[string][]int `bson:"couplingDependencies,omitempty"`
	Entities             []Entity         `bson:"elements,omitempty"`
}

type Entity struct {
	Id   int    `bson:"_id,omitempty"`
	Name string `bson:"name,omitempty"`
}

type Functionality struct {
	Name                                    string             `bson:"name,omitempty"`
	Type                                    string             `bson:"type,omitempty"`
	Metrics                                 map[string]float64 `bson:"metrics,omitempty"`
	Entities                                map[string]int     `bson:"entities,omitempty"`
	FunctionalityRedesigns                  map[string]string  `bson:"functionalityRedesigns,omitempty"`
	FunctionalityRedesignNameUsedForMetrics string             `bson:"functionalityRedesignNameUsedForMetrics,omitempty"`
	EntitiesPerCluster                      map[string][]int   `bson:"entitiesPerCluster,omitempty"`
}

type FunctionalityRedesign struct {
	Name             string         `bson:"name,omitempty"`
	Redesign         []*Invocation  `bson:"redesign,omitempty"`
	Metrics          map[string]int `bson:"metrics,omitempty"`
	PivotTransaction int            `bson:"pivotTransaction,omitempty"`
}

type Invocation struct {
	Name              string          `bson:"name,omitempty"`
	ID                int             `bson:"id,omitempty"`
	ClusterName       string          `bson:"clusterName,omitempty"`
	ClusterAccesses   [][]interface{} `bson:"clusterAccesses,omitempty"`
	RemoteInvocations []int           `bson:"remoteInvocations,omitempty"`
	Type              string          `bson:"type,omitempty"`
}
