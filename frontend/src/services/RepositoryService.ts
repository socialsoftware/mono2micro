import axios, { AxiosInstance } from 'axios';
import { 
    URL,
    REFACTORIZATION_TOOL_URL,
} from '../constants/constants';
import {
    AnalyserDto,
    AnalysisDto,
    TraceType,
    Cluster,
    Functionality,
    LocalTransactionsGraph,
    RefactorCodebase,
    SimilarityMatrix, Edges
} from "../type-declarations/types";
import { addSearchParamsToUrl } from "../utils/url";
import {SourceFactory} from "../models/sources/SourceFactory";
import Strategy from "../models/strategies/Strategy";
import {StrategyFactory} from "../models/strategies/StrategyFactory";
import {DecompositionFactory} from "../models/decompositions/DecompositionFactory";
import Decomposition from "../models/decompositions/Decomposition";
import Codebase from "../models/codebase/Codebase";

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

    // Recommendation
    recommendation(
        codebaseName: string,
        requestedStrategyRecommendation: Strategy
    ) {
        return this.axios.put("/codebase/" + codebaseName + "/recommendation", requestedStrategyRecommendation)
            .then((response) => {return StrategyFactory.getStrategy(response.data)});
    }

    getRecommendationResult(
        codebaseName: string,
        strategyName: string
    ) {
        return this.axios.get("/codebase/" + codebaseName + "/recommendationStrategy/" + strategyName + "/getRecommendationResult")
            .then(response => {
                if (response.data === "")
                    return [];
                else return response.data;
            });
    }

    createRecommendationDecompositions(
        strategyRecommendation: string,
        decompositionNames: string[]
    ) {
        const data = new FormData();
        for (const name of decompositionNames)
            data.append('decompositionNames', name);

        // unfortunately, this is needed so that data.append does not interpret the commas as an array separation
        data.append('decompositionNames', "");

        return this.axios.post<null>("/recommendAccessesSciPyStrategy/" + strategyRecommendation + "/createDecompositions", data);
    }

    getRecommendationStrategy(codebaseName: string, strategyName: string) {
        return this.axios.get<Strategy>("/codebase/" + codebaseName + "/recommendationStrategy/" + strategyName)
            .then((response) => {return StrategyFactory.getStrategy(response.data)});
    }

    //Codebases
    getCodebases() {
        return this.axios.get("/codebases").then(response => response.data.map((codebase:any) => new Codebase(codebase)));
    }

    getCodebase(codebaseName: string) {
        return this.axios.get("/codebase/" + codebaseName).then(response => new Codebase(response.data));
    }

    getCodebaseDecompositions(
        codebaseName: string,
        strategyType?: string
    ) {
        return this.axios.get<Decomposition[]>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/decompositions",
                strategyType? {strategyType} : {},
            )
        );
    }

    deleteCodebase(name: string) {
        return this.axios.delete<null>("/codebase/" + name + "/delete");
    }

    createCodebase(codebaseName: string) {
        const data = new FormData();
        data.append('codebaseName', codebaseName);
        return this.axios.post<null>("/codebase/create", data);
    }

    // Profiles
    addAccessesProfile(sourceName: string, profile: string) {
        return this.axios.post<null>(
            "/source/" + sourceName + "/addAccessesProfile",
            null,
            {
                params: {
                    "profile" : profile
                }
            });
    }

    moveAccessesFunctionalities(
        sourceName: string,
        functionalities: string[],
        targetProfile: string,
    ) {
        return this.axios.post<null>(
            "/source/" + sourceName + "/moveAccessesFunctionalities",
            functionalities,
            {
                params: {
                    "targetProfile" : targetProfile
                }
            }
        );
    }

    deleteAccessesProfile(sourceName: string, profile: string) {
        return this.axios.delete<null>(
            "/source/" + sourceName + "/deleteAccessesProfile",
            {
                params: {
                    "profile" : profile
                }
            }
        );
    }

    //Sources
    addSources(
        codebaseName: string,
        sources: Map<string, File>
    ) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        const data = new FormData();
        Object.entries(sources).forEach((entry) => {data.append("sourceTypes", entry[0]); data.append("sources", entry[1])});

        return this.axios.post<null>("/codebase/" + codebaseName + "/addSources", data, config);
    }

    getCodebaseSource(codebaseName: string, sourceType: string) {
        return this.axios.get("/codebase/" + codebaseName + "/source/" + sourceType + "/getCodebaseSource")
            .then((response) => SourceFactory.getSource(response.data));
    }

    getSource(sourceName: string) {
        return this.axios.get("/source/" + sourceName + "/getSource")
            .then((response) => SourceFactory.getSource(response.data));
    }

    getSourceTypes(codebaseName: string) {
        return this.axios.get<string[]>("/codebase/" + codebaseName + "/getSourceTypes");
    }

    deleteSource(id: string) {
        return this.axios.delete<null>("/source/" + id + "/delete");
    }

    getInputFile(sourceName: string) {
        return this.axios.get<string>("/source/" + sourceName + "/getInputFile");
    }


    //Strategies
    getCodebaseStrategies(codebaseName: string) {
        return this.axios.get("/codebase/" + codebaseName + "/getCodebaseStrategies").then(responseList => {
            if (responseList.data.length == 0)
                return responseList.data;
            return responseList.data.map((response: any) => {
                return StrategyFactory.getStrategy(response);
            });
        });
    }

    getStrategy(codebaseName: string, strategyName: string) {
        return this.axios.get("/codebase/" + codebaseName + "/strategy/" + strategyName).then(
            response => { return StrategyFactory.getStrategy(response); });
    }

    deleteStrategy(strategyName: string) {
        return this.axios.delete<null>("/strategy/" + strategyName + "/delete");
    }
    
    createAccessesSciPyStrategy(strategy: Strategy) {
        return this.axios.post<null>("/strategy/createAccessesSciPyStrategy", strategy);
    }

    createRecommendAccessesSciPyStrategy(strategy: Strategy) {
        return this.axios.post<null>("/strategy/createRecommendAccessesSciPyStrategy", strategy);
    }

    getEdgeWeights(codebaseName: string, strategyName: string, decompositionName: string) {
        return this.axios.get<Edges>("/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/getEdgeWeights");
    }

    getGraphPositions(codebaseName: string, strategyName: string, decompositionName: string) {
        return this.axios.get("/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/getGraphPositions");
    }

    deleteGraphPositions(codebaseName: string, strategyName: string, decompositionName: string) {
        return this.axios.delete("/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/deleteGraphPositions");
    }

    saveGraphPositions(codebaseName: string, strategyName: string, decompositionName: string, graphPositions: any) {
        return this.axios.post<null>("/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/saveGraphPositions", graphPositions);
    }

    createAccessesSciPyDecomposition(
        strategyName: string,
        cutType: string,
        cutValue: number
    ) {
        return this.axios.get(addSearchParamsToUrl(
            "/strategy/" + strategyName + "/createAccessesSciPyDecomposition",
            { cutType: cutType, cutValue: cutValue.toString() },
        ));
    }

    createAccessesSciPyExpertDecomposition(
        strategyName: string,
        expertName: string,
        expertFile: any
    ) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        const data = new FormData();
        data.append('expertName', expertName);
        data.append('expertFile', expertFile);

        return this.axios.post<null>(
            "/strategy/" + strategyName + "/createExpertDecomposition",
            data,
            config
        );
    }

    //Decomposition
    getDecompositions(
        codebaseName: string,
        strategyName: string
    ) {
        return this.axios.get<Decomposition[]>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decompositions",
        ).then(responseList => {
            if (responseList.data.length == 0)
                return responseList.data;
            return responseList.data.map((response: any) => {
                return DecompositionFactory.getDecomposition(response);
            });
        });
    }

    getDecomposition(
        codebaseName: string,
        strategyName: string,
        decompositionName: string
    ) {
        return this.axios.get<Decomposition>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName
        );
    }

    updatedAccessesSciPyDecomposition(
        codebaseName: string,
        strategyName: string,
        decompositionName: string
    ) {
        return this.axios.get<Decomposition>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/updatedAccessesSciPyDecomposition"
        );
    }

    getAccessesSciPyClusters(
        codebaseName: string,
        strategyName: string,
        decompositionName: string
    ) {
        return this.axios.get<Cluster[]>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/getClusters"
        );
    }

    deleteDecomposition(
        codebaseName: string,
        strategyName: string,
        decompositionName: string
    ) {
        return this.axios.delete<null>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/delete"
        );
    }

    getLocalTransactionsGraphForFunctionality(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        functionalityName: string
    ) {
        return this.axios.get<LocalTransactionsGraph>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/getLocalTransactionsGraphForFunctionality",
                { functionalityName: functionalityName },
            )
        );
    }

    //Cluster
    mergeClusters(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        clusterID: number,
        otherClusterID: number,
        newName: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/cluster/" + clusterID + "/merge",
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
        strategyName: string,
        decompositionName: string,
        clusterID: number,
        newName: string
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/cluster/" + clusterID + "/rename",
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
        strategyName: string,
        decompositionName: string,
        clusterID: number,
        newName: string,
        entities: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/cluster/" + clusterID + "/split",
            null,
            {
                params: {
                    "newName" : newName,
                    "entities" : entities
                }
            }
        );
    }

    formCluster(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        newName: string,
        entities: Map<string, number[]>,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/formCluster",
            entities,
            {
                params: {
                    "newName" : newName,
                }
            }
        );
    }

    undoOperation(
        codebaseName: string,
        strategyName: string,
        decompositionName: string
    ) {
        return this.axios.get<Record<number, Cluster>>("/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/undoOperation");
    }

    transferEntities(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        clusterID: number,
        toClusterID: number,
        entities: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/cluster/" + clusterID + "/transferEntities",
            null,
            {
                params: {
                    "toClusterID" : toClusterID,
                    "entities" : entities
                }
            }
        );
    }

    getFunctionalitiesAndFunctionalitiesClusters(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
    ) {
        return this.axios.get<Record<string, any>>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/getFunctionalitiesAndFunctionalitiesClusters"
        );
    }

    getClustersAndClustersFunctionalities(
        codebaseName: string,
        strategyName: string,
        decompositionName: string
    ) {
        return this.axios.get<Record<string, any>>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/getClustersAndClustersFunctionalities"
        );
    }

    getSearchItems(
        codebaseName: string,
        strategyName: string,
        decompositionName: string
    ) {
        return this.axios.get<any[]>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/getSearchItems"
        );
    }


    //FunctionalityRedesign

    getOrCreateRedesign(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        functionalityName: string
    ) {
        return this.axios.get<Functionality>("/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/getOrCreateRedesign");
    }


    addCompensating(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        functionalityName: string,
        redesignName: string,
        clusterID: number,
        entities: string,
        fromID: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/addCompensating",
            {
                fromID: fromID,
                cluster : clusterID,
                entities : entities
            }
        );
    }

    sequenceChange(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        functionalityName: string,
        redesignName: string,
        localTransaction: string,
        newCaller: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/sequenceChange",
            {
                localTransactionID: localTransaction,
                newCaller: newCaller
            }
        );
    }

    dcgi(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        functionalityName: string,
        redesignName: string,
        fromClusterID: number,
        toClusterID: number,
        localTransactions: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/dcgi",
            {
                fromCluster: fromClusterID,
                toCluster: toClusterID,
                localTransactions: localTransactions
            }
        );
    }

    selectPivotTransaction(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        functionalityName: string,
        redesignName: string,
        transactionID: string,
        newRedesignName: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/pivotTransaction",
            null,
            { params: {"transactionID" : transactionID, "newRedesignName": newRedesignName}}
        );
    }

    changeLTName(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        functionalityName: string,
        redesignName: string,
        transactionID: string,
        newName: string
    ) {
        return this.axios.post(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/changeLTName",
            null,
            { params: {"transactionID" : transactionID, "newName": newName}}
        );
    }

    deleteRedesign(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        functionalityName: string,
        redesignName: string
    ){
        return this.axios.delete(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/deleteRedesign"
        );
    }

    setUseForMetrics(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        functionalityName: string,
        redesignName: string
    ){
        return this.axios.post(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/useForMetrics"
        );
    }

    refactorCodebase(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
        functionalityNames: string[],
        dataDependenceThreshold: number,
        minimizeSumOfComplexities: boolean,
        timeOut: number,
        newRefactor: boolean,
    ) {

        const refactorRequest: RefactorCodebase = {
            codebase_name: codebaseName,
            strategy_name: strategyName,
            decomposition_name: decompositionName,
            functionality_names: functionalityNames,
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
        strategyName: string,
        decompositionName: string,
    ) {

        return this.refactorizationToolAxios.get(
            "/refactor/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName ,
        );
    }
}