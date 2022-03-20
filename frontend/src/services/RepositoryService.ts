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
    Cluster,
    Controller,
    LocalTransactionsGraph,
    RefactorCodebase,
    Strategy
} from "../type-declarations/types";
import { addSearchParamsToUrl } from "../utils/url";
import {SourceFactory} from "../models/sources/SourceFactory";

export class RepositoryService {
    axios: AxiosInstance;

    refactorizationToolAxios: AxiosInstance;

    constructor() {
        const headers = {
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
            tracesMaxLimit: amountOfTraces
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

    getTranslation(codebaseName: string) {
        return this.axios.get<string>("/codebase/" + codebaseName + "/translation");
    }

    getCodebaseDecompositions(
        codebaseName: string,
        strategyType?: string,
        fieldNames?: string[],
    ) {
        let params: {[key:string]: string[] | string} = {};
        if (strategyType)
            params.strategyType = strategyType;
        if (fieldNames)
            params.fieldNames = fieldNames;
        return this.axios.get<Decomposition[]>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/decompositions",
                params,
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

    createCodebase(codebaseName: string) {
        const data = new FormData();
        data.append('codebaseName', codebaseName);
        return this.axios.post<null>("/codebase/create", data);
    }

    //Sources
    addSource(
        codebaseName: string,
        sourceType: string,
        inputFile: any
    ) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        const data = new FormData();
        data.append('sourceType', sourceType);
        data.append('inputFile', inputFile);

        return this.axios.post<null>("/codebase/" + codebaseName + "/addSource", data, config);
    }

    getSources(codebaseName: string) {
        return this.axios.get("/codebase/" + codebaseName + "/sources")
            .then((response) => {
                return response.data.map((source: any) => SourceFactory.getSource(source))
            });
    }

    //Strategies
    getStrategies(codebaseName: string, strategyType?: string) {
        return this.axios.get<Strategy[]>(addSearchParamsToUrl(
            "/codebase/" + codebaseName + "/strategies",
            strategyType? {strategyType} : {},
        ));
    }

    getStrategy(codebaseName: string, strategyName: string) {
        return this.axios.get<Strategy>("/codebase/" + codebaseName + "/strategy/" + strategyName);
    }

    deleteStrategy(codebaseName: string, strategyName: string) {
        return this.axios.delete<null>("/codebase/" + codebaseName + "/strategy/" + strategyName + "/delete");
    }
    
    createStrategy(request: Strategy) {
        return this.axios.post<null>("/codebase/" + request.codebaseName + "/strategy/createStrategy", request);
    }

    createDecomposition(
        codebaseName: string,
        strategyName: string,
        request: any
    ) {

        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/createDecomposition",
            request
        );
    }

    createExpertDecomposition(
        codebaseName: string,
        strategyName: string,
        type: string,
        expertName: string,
        expertFile: any
    ) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        const data = new FormData();
        data.append('type', type);
        data.append('expertName', expertName);
        data.append('expertFile', expertFile);

        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/createExpertDecomposition",
            data,
            config
        );
    }

    //Decomposition
    getDecompositions(
        codebaseName: string,
        strategyName: string,
        fieldNames?: string[],
    ) {
        return this.axios.get<Decomposition[]>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decompositions",
                fieldNames ? { fieldNames } : {},
            )
        );
    }

    getDecomposition(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        fieldNames?: string[],
    ) {

        return this.axios.get<Decomposition>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName,
                fieldNames ? { fieldNames } : {},
            )
        );
    }

    deleteDecomposition(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
    ) {
        return this.axios.delete<null>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/delete"
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
        clusterID: number,
        otherClusterID: number,
        newName: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/cluster/" + clusterID + "/merge",
            null,
            {
                params: {
                    "otherClusterID" : otherClusterID,
                    "newName" : newName
                }
            }
        );
    }

    renameCluster(
        codebaseName: string,
        dendrogramName: string,
        decompositionName: string,
        clusterID: number,
        newName: string
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/cluster/" + clusterID + "/rename",
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
        clusterID: number,
        newName: string,
        entities: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/cluster/" + clusterID + "/split",
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
        clusterID: number,
        toClusterID: number,
        entities: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/cluster/" + clusterID + "/transferEntities",
            null,
            {
                params: {
                    "toClusterID" : toClusterID,
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
        return this.axios.get<Record<number, Controller[]>>(
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
        clusterID: number,
        entities: string,
        fromID: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/controller/" + controllerName + "/redesign/" + redesignName + "/addCompensating",
            {
                fromID: fromID,
                cluster : clusterID,
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
        fromClusterID: number,
        toClusterID: number,
        localTransactions: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/decomposition/" + decompositionName + "/controller/" + controllerName + "/redesign/" + redesignName + "/dcgi",
            {
                fromCluster: fromClusterID,
                toCluster: toClusterID,
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