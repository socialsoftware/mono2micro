import Decomposition from "../models/decompositions/Decomposition";

export interface Edges {
    edges: Edge[];
}

export interface Edge {
    dist: number;
    e1ID: number;
    e2ID: number;
    functionalities: string[];
}

export interface SimilarityMatrix {
    entities?: number[];
    linkageType?: string;
    matrix?: number[][];
}

export interface AccessDto {
    entity?: string;
    mode?: string;
    frequency?: number;
}

export interface LocalTransaction {
    id?: number;
    clusterName?: string;
    clusterAccesses?: AccessDto[];
    remoteInvocations?: number[];
    firstAccessedEntityIDs: number[];
}

export interface LocalTransactionsGraph {
    nodes: LocalTransaction[];
    links: string[];
}

export interface Functionality {
    name?: string;
    type?: string;
    metrics?: Record<string, any>;
    entitiesPerCluster?: Record<number, number[]>; // <clusterId, entityIDs>
    entities?: Record<number, number>; // <entityID, mode>
    functionalityRedesigns: FunctionalityRedesign[];
    functionalityRedesignNameUsedForMetrics: string;
}

export interface FunctionalityRedesign {
    name?: string;
    usedForMetrics?: boolean;
    metrics?: Record<string, any>;
    redesign?: LocalTransaction[];
    pivotTransaction?: number;
}

export enum MetricType {
    COHESION = "Cohesion",
    COMPLEXITY = "Complexity",
    COUPLING = "Coupling",
    PERFORMANCE = "Performance",
    SILHOUETTE_SCORE = "Silhouette Score",
    SYSTEM_COMPLEXITY = "System Complexity",
    FUNCTIONALITY_COMPLEXITY = "Functionality Complexity",
    INCONSISTENCY_COMPLEXITY = "Inconsistency Complexity",
    TSR = "TSR"
}

export interface Cluster {
    name?: string;
    elements?: Elem;
}

export interface sciPyCluster extends Cluster {
    metrics?: Record<string, any>;
    complexity?: number;
    cohesion?: number;
    coupling?: number;
    couplingDependencies?: Record<number, number[]> // <clusterID, entityIDs>
}

export interface Elem {
    id: number;
    name: string;
}

export interface DomainEntity extends Elem {
    type: string;
}

export interface ComparisonToolDto {
    decomposition1?: Decomposition;
    decomposition2?: Decomposition;
}

export interface DefaultComparisonToolDto extends ComparisonToolDto {
    truePositive?: number;
    trueNegative?: number;
    falsePositive?: number;
    falseNegative?: number;
    falsePairs?: string[];
    accuracy?: number;
    precision?: number;
    recall?: number;
    specificity?: number;
    fmeasure?: number;
    complexity?: number;
    mojoCommon?: number;
    mojoBiggest?: number;
    mojoNew?: number;
    mojoSingletons?: number;
}

export interface RefactorCodebase {
    codebase_name?: string;
    strategy_name?: string;
    decomposition_name?: string;
    functionality_names?: string[];
    data_dependence_threshold?: number;
    minimize_sum_of_complexities?: boolean;
    refactor_time_out_secs?: number;
    override_previous?: boolean;
}

export enum RepresentationInfoParameters {
    DEPTH_PARAMETER = "DEPTH_PARAMETER",
    PROFILE_PARAMETER = "PROFILE_PARAMETER",
    TRACES_MAX_LIMIT_PARAMETER = "TRACES_MAX_LIMIT_PARAMETER",
    TRACE_TYPE_PARAMETER = "TRACE_TYPE_PARAMETER",
}

export enum TraceType {
    NONE = "",
    ALL = "ALL",
    LONGEST = "LONGEST",
    WITH_MORE_DIFFERENT_ACCESSES = "WITH_MORE_DIFFERENT_ACCESSES",
}