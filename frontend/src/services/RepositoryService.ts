import axios, { AxiosInstance } from 'axios';
import { 
    URL,
    REFACTORIZATION_TOOL_URL,
} from '../constants/constants';
import {
    AnalyserDto,
    Decomposition,
    AnalysisDto,
    Codebase,
    TraceType,
    Dendrogram,
    Cluster,
    Controller,
    LocalTransactionsGraph,
    RefactorCodebase,
} from "../type-declarations/types";
import { addSearchParamsToUrl } from "../utils/url";

export class RepositoryService {
    axios: AxiosInstance;

    refactorizationToolAxios: AxiosInstance;

    constructor() {
        var headers = {
            'X-Custom-Header': 'Mono2Micro',
        };

        this.axios = axios.create({
            baseURL: URL,
            timeout: 0,
            headers: headers,
        });

        this.refactorizationToolAxios = axios.create({
            baseURL: REFACTORIZATION_TOOL_URL,
            timeout: 0,
            headers: headers,
        });
    }

    //Analysis
    analysis(data: AnalysisDto) {
        return this.axios.post<AnalysisDto>("/analysis", data);
    }

    analyser(
        codebaseName: string,
        expert: Decomposition,
        profile: string,
        requestLimit: number,
        amountOfTraces: number,
        traceType: TraceType,
    ) {
        const analyserData: AnalyserDto = {
            expert: expert || {},
            profile,
            requestLimit,
            traceType,
            tracesMaxLimit: amountOfTraces,
        };

        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/analyser",
            analyserData
        );
    }

    //Codebases
    getCodebases(fieldNames?: string[]) {
        return this.axios.get<Codebase[]>(addSearchParamsToUrl(
            "/codebases",
            fieldNames ? { fieldNames } : {},
        ));
    }

    getCodebase(codebaseName: string, fieldNames?: string[]) {
        return this.axios.get<Codebase>(addSearchParamsToUrl(
            "/codebase/" + codebaseName,
            fieldNames ? { fieldNames } : {},
        ));
    }

    //Dendrograms
    getCodebaseDecompositions(
        codebaseName: string,
        fieldNames?: string[],
    ) {
        return this.axios.get<Decomposition[]>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/decompositions",
                fieldNames ? { fieldNames } : {},
            )
        );
    }

    deleteCodebase(name: string) {
        return this.axios.delete<null>("/codebase/" + name + "/delete");
    }

    addProfile(codebaseName: string, profile: string) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/addProfile",
            null,
            {
                params: {
                    "profile" : profile
                }
            });
    }

    moveControllers(
        codebaseName: string,
        controllers: string[],
        targetProfile: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/moveControllers", 
            controllers,
            {
                params: {
                    "targetProfile" : targetProfile
                }
            }
        );
    }

    deleteProfile(codebaseName: string, profile: string) {
        return this.axios.delete<null>(
            "/codebase/" + codebaseName + "/deleteProfile", 
            {
                params: {
                    "profile" : profile
                }
            }
        );
    }

    createCodebase(
        name: string,
        datafile: any,
    ) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        var data = new FormData();
        data.append('codebaseName', name);
        data.append('datafile', datafile);
        
        return this.axios.post<null>(
            "/codebase/create",
            data,
            config,
        );
    }

    //Dendrograms
    getDendrograms(
        codebaseName: string,
        fieldNames?: string[],
    ) {
        return this.axios.get<Dendrogram[]>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/dendrograms",
                fieldNames ? { fieldNames } : {}
            )
        );
    }

    getDendrogram(codebaseName: string, dendrogramName: string) {
        return this.axios.get<Dendrogram>("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName);
    }

    deleteDendrogram(codebaseName: string, dendrogramName: string) {
        return this.axios.delete<null>("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/delete");
    }
    
    createDendrogram(
        codebaseName: string,
        dendrogramName: string,
        linkageType: string,
        accessMetricWeight: number,
        writeMetricWeight: number,
        readMetricWeight: number,
        sequenceMetricWeight: number,
        profile: string,
        amountOfTraces: number,
        traceType: TraceType,
    ) {
        const dendrogramData: Dendrogram = {
            codebaseName,
            name: dendrogramName,
            linkageType,
            accessMetricWeight,
            writeMetricWeight,
            readMetricWeight,
            sequenceMetricWeight,
            profile,
            tracesMaxLimit: amountOfTraces,
            traceType,
        };
        
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/create",
            dendrogramData
        );
    }

    cutDendrogram(
        codebaseName: string,
        dendrogramName: string,
        cutValue: number,
        cutType: string,
    ) {
        const decompositionData: Decomposition = {
            codebaseName,
            dendrogramName,
            expert: false,
            cutValue,
            cutType,
        };

        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/cut",
            decompositionData
        );
    }

    expertCut(
        codebaseName: string,
        dendrogramName: string,
        expertName: string,
        expertFile: any,
    ) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        var data = new FormData();
        data.append('expertName', expertName);
        data.append('expertFile', expertFile);

        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/expertCut",
            data,
            config
        );
    }

    //Decomposition
    getDecompositions(
        codebaseName: string,
        dendrogramName: string,
        fieldNames?: string[],
    ) {
        return this.axios.get<Decomposition[]>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decompositions",
                fieldNames ? { fieldNames } : {},
            )
        );
    }

    getDecomposition(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        fieldNames?: string[],
    ) {

        return this.axios.get<Decomposition>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName,
                fieldNames ? { fieldNames } : {},
            )
        );
    }

    deleteDecomposition(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
    ) {
        return this.axios.delete<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/delete"
        );
    }

    getLocalTransactionsGraphForController(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        controllerName: string
    ) {
        return this.axios.get<LocalTransactionsGraph>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/getLocalTransactionsGraphForController",
                { controllerName },
            )
        );
    }

    //Cluster
    mergeClusters(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        clusterName: string,
        otherCluster: string,
        newName: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/cluster/" + clusterName + "/merge",
            null,
            {
                params: {
                    "otherCluster" : otherCluster,
                    "newName" : newName
                }
            }
        );
    }

    renameCluster(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        clusterName: string,
        newName: string
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/cluster/" + clusterName + "/rename",
            null,
            {
                params: {
                    "newName" : newName
                },
            }
        );
    }

    splitCluster(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        clusterName: string,
        newName: string,
        entities: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/cluster/" + clusterName + "/split",
            null,
            {
                params: {
                    "newName" : newName,
                    "entities" : entities
                }
            }
        );
    }

    transferEntities(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        clusterName: string,
        toCluster: string,
        entities: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/cluster/" + clusterName + "/transferEntities",
            null,
            {
                params: {
                    "toCluster" : toCluster,
                    "entities" : entities
                }
            }
        );
    }

    getControllersClusters(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
    ) {
        return this.axios.get<Record<string, Cluster[]>>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/controllersClusters"
        );
    }

    getClustersControllers(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string
    ) {
        return this.axios.get<Record<string, Controller[]>>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/clustersControllers"
        );
    }


    //FunctionalityRedesign

    getOrCreateRedesign(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        controllerName: string
    ) {
        return this.axios.get<Controller>("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/controller/" + controllerName + "/getOrCreateRedesign");
    }


    addCompensating(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        controllerName: string,
        redesignName: string,
        clusterName: string,
        entities: string,
        fromID: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/controller/" + controllerName + "/redesign/" + redesignName + "/addCompensating",
            {
                fromID: fromID,
                cluster : clusterName,
                entities : entities
            }
        );
    }

    sequenceChange(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        controllerName: string,
        redesignName: string,
        localTransaction: string,
        newCaller: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/controller/" + controllerName + "/redesign/" + redesignName + "/sequenceChange",
            {
                localTransactionID: localTransaction,
                newCaller: newCaller
            }
        );
    }

    dcgi(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        controllerName: string,
        redesignName: string,
        fromCluster: string,
        toCluster: string,
        localTransactions: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/controller/" + controllerName + "/redesign/" + redesignName + "/dcgi",
            {
                fromCluster: fromCluster,
                toCluster: toCluster,
                localTransactions: localTransactions
            }
        );
    }

    selectPivotTransaction(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        controllerName: string,
        redesignName: string,
        transactionID: string,
        newRedesignName: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/controller/" + controllerName + "/redesign/" + redesignName + "/pivotTransaction",
            null,
            { params: {"transactionID" : transactionID, "newRedesignName": newRedesignName}}
        );
    }

    changeLTName(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        controllerName: string,
        redesignName: string,
        transactionID: string,
        newName: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/controller/" + controllerName + "/redesign/" + redesignName + "/changeLTName",
            null,
            { params: {"transactionID" : transactionID, "newName": newName}}
        );
    }

    deleteRedesign(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        controllerName: string,
        redesignName: string
    ){
        return this.axios.delete(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/controller/" + controllerName + "/redesign/" + redesignName + "/deleteRedesign"
        );
    }

    setUseForMetrics(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        controllerName: string,
        redesignName: string
    ){
        return this.axios.post(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/controller/" + controllerName + "/redesign/" + redesignName + "/useForMetrics"
        );
    }

    refactorCodebase(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        controllerNames: string[],
        dataDependenceThreshold: number,
        minimizeSumOfComplexities: boolean,
        timeOut: number,
        newRefactor: boolean,
    ) {

        const refactorRequest: RefactorCodebase = {
            codebase_name: codebaseName,
            dendrogram_name: dendrogramName,
            decomposition_name: decompositionName,
            controller_names: controllerNames,
            data_dependence_threshold: dataDependenceThreshold,
            minimize_sum_of_complexities: minimizeSumOfComplexities,
            refactor_time_out_secs: timeOut,
            override_previous: newRefactor,
        };

        return this.refactorizationToolAxios.post(
            "/refactor",
            refactorRequest
        );
    }

    viewRefactor(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
    ) {

        return this.refactorizationToolAxios.get(
            "/refactor/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName ,
        );
    }
}