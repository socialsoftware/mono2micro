export interface AccessDto {
    entity?: string;
    mode?: string;
    frequency?: number;
}

export interface LocalTransaction {
    id?: number;
    clusterID?: number;
    clusterAccesses?: AccessDto[];
    firstAccessedEntityIDs: number[];
}

export interface LocalTransactionsGraph {
    nodes: LocalTransaction[];
    links: string[];
}

export interface Controller {
    name?: string;
    complexity?: number;
    performance?: number;
    entities?: Record<number, number>; // <entityID, mode>
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

export interface Decomposition {
	name?: string;
    codebaseName?: string;
    strategyName?:string;
	expert?: boolean;
	silhouetteScore?: number;
    complexity?: number;
    performance?: number;
	cohesion?: number;
	coupling?: number;
	controllers?: Record<string, Controller>;
    clusters?: Record<number, Cluster>;
    entityIDToClusterID?: Record<number, number>;
}

export interface Codebase {
    name?: string;
    profiles?: Record<string, string[]>; // e.g <Generic, ControllerNamesList>
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
    controller_names?: string[];
    data_dependence_threshold?: number;
    minimize_sum_of_complexities?: boolean;
    refactor_time_out_secs?: number;
    override_previous?: boolean;
}

export enum TraceType {
    NONE = "",
    ALL = "ALL",
    REPRESENTATIVE = "REPRESENTATIVE",
    LONGEST = "LONGEST",
    WITH_MORE_DIFFERENT_ACCESSES = "WITH_MORE_DIFFERENT_ACCESSES",
}