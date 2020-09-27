import { Dendrogram } from "../components/dendrogram/Dendrogram";

export interface AccessDto {
    entity?: string;
    mode?: string;
    frequency?: number;
}

export interface LocalTransaction {
    id?: string;
    clusterName?: string;
    clusterAccesses?: AccessDto[];
}

export interface Controller {
    name?: string;
    complexity?: number;
    performance?: number;
    entities?: Record<string, string>; // <entityName, mode>
    localTransactionsGraph?: {
        nodes?: LocalTransaction[];
        links?: string[];
    }
}

export interface Cluster {
    name?: string;
    complexity?: number;
    cohesion?: number;
    coupling?: number;
    couplingDependencies?: Record<string, string[]> // <clusterName, entities>
    entities?: string[];
}

export interface Graph {
    codebaseName?: string;
	dendrogramName?: string;
	name?: string;
	expert?: boolean;
	cutValue?: number;
	cutType?: string;
	silhouetteScore?: number;
    complexity?: number;
    performance?: number;
	cohesion?: number;
	coupling?: number;
	controllers?: Controller[];
	clusters?: Cluster[];
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
    graphs?: Graph[];
    profiles?: string[];
	typeOfTraces?: TypeOfTraces;
}

export interface Codebase {
    name?: string;
    profiles?: Record<string, string[]>; // e.g <Generic, ControllerNamesList>
    dendrograms?: Dendrogram[];
    analysisType?: string;
    datafilePath?: string;
}

export interface AnalyserDto {
    profiles?: String[];
    requestLimit?: number;
    tracesMaxLimit?: number;
    typeOfTraces?: TypeOfTraces;
    expert?: Graph;
}

export interface AnalysisDto {
    graph1?: Graph;
    graph2?: Graph;
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

export enum TypeOfTraces {
    ALL,
    REPRESENTATIVE,
    LONGEST,
    WITH_MORE_DIFFERENT_ACCESSES,
}