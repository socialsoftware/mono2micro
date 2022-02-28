import { Dendrogram } from "../components/dendrogram/Dendrogram";

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
    nextClusterID?: number;
    codebaseName?: string;
	dendrogramName?: string;
	expert?: boolean;
	cutValue?: number;
	cutType?: string;
	silhouetteScore?: number;
    complexity?: number;
    performance?: number;
	cohesion?: number;
	coupling?: number;
	controllers?: Record<string, Controller>;
    clusters?: Record<number, Cluster>;
    entityIDToClusterID?: Record<number, number>;
}

export interface Dendrogram {
    codebaseName?: string;
    name?: string;
    linkageType?: string;
    accessMetricWeight?: number;
    writeMetricWeight?: number;
	readMetricWeight?: number;
	sequenceMetricWeight?: number;
	tracesMaxLimit?: number;
    decompositions?: Decomposition[];
    profile?: string;
	traceType?: TraceType;
    similarityGeneratorType?: SimilarityGeneratorType;
    clusteringAlgorithmType?: ClusteringAlgorithmType;
}

export interface Codebase {
    name?: string;
    profiles?: Record<string, string[]>; // e.g <Generic, ControllerNamesList>
    dendrograms?: Dendrogram[];
    datafilePath?: string;
}

export interface AnalyserDto {
    profile?: string;
    requestLimit?: number;
    tracesMaxLimit?: number;
    traceType?: TraceType;
    expert?: Decomposition;
    similarityGeneratorType?: SimilarityGeneratorType;
    clusteringAlgorithmType?: ClusteringAlgorithmType;
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
    dendrogram_name?: string;
    decomposition_name?: string;
    controller_names?: string[];
    data_dependence_threshold?: number;
    minimize_sum_of_complexities?: boolean;
    refactor_time_out_secs?: number;
    override_previous?: boolean;
}

export enum TraceType {
    ALL = "ALL",
    REPRESENTATIVE = "REPRESENTATIVE",
    LONGEST = "LONGEST",
    WITH_MORE_DIFFERENT_ACCESSES = "WITH_MORE_DIFFERENT_ACCESSES",
}

export enum SimilarityGeneratorType {
    DEFAULT = "DEFAULT"
}

export enum ClusteringAlgorithmType {
    SCIPY = "SCIPY"
}