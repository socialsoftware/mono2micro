import Decomposition from "../models/decompositions/Decomposition";

export interface AccessDto {
    entity?: string;
    mode?: string;
    frequency?: number;
}

export interface LocalTransaction {
    id?: number;
    clusterID?: number;
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
    metrics?: Metric[];
    entities?: Record<number, number>; // <entityID, mode>
    functionalityRedesigns: FunctionalityRedesign[];
}

export interface FunctionalityRedesign {
    name?: string;
    usedForMetrics?: boolean;
    metrics?: Metric[];
    redesign?: LocalTransaction[];
    pivotTransaction?: number;
}

export interface Metric {
    type: string,
    value: any
}

export enum MetricType {
    COHESION = "Cohesion",
    COMPLEXITY = "Complexity",
    COUPLING = "Coupling",
    PERFORMANCE = "Performance",
    SILHOUETTE_SCORE = "Silhouette Score",
    SYSTEM_COMPLEXITY = "System Complexity",
    FUNCTIONALITY_COMPLEXITY = "Functionality Complexity",
    INCONSISTENCY_COMPLEXITY = "Inconsistency Complexity"
}

export interface Cluster {
    id?: number;
    name?: string;
    complexity?: number;
    cohesion?: number;
    coupling?: number;
    couplingDependencies?: Record<number, number[]> // <clusterID, entityIDs>
    entities?: number[];
}

export interface Codebase {
    name?: string;
    profiles?: Record<string, string[]>; // e.g <Generic, FunctionalityNamesList>
    datafilePath?: string;
}

export interface AnalyserDto {
    profile?: string;
    requestLimit?: number;
    tracesMaxLimit?: number;
    traceType?: TraceType;
    expert?: Decomposition;
}

export interface AnalysisDto {
    decomposition1?: Decomposition;
    decomposition2?: Decomposition;
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

export enum TraceType {
    NONE = "",
    ALL = "ALL",
    LONGEST = "LONGEST",
    WITH_MORE_DIFFERENT_ACCESSES = "WITH_MORE_DIFFERENT_ACCESSES",
}