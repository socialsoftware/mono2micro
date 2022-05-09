import axios, { AxiosInstance } from 'axios';
import { 
    URL,
    REFACTORIZATION_TOOL_URL,
} from '../constants/constants';
import {
    AnalyserDto,
    AnalysisDto,
    Codebase,
    TraceType,
    Cluster,
    Functionality,
    LocalTransactionsGraph,
    RefactorCodebase
} from "../type-declarations/types";
import { addSearchParamsToUrl } from "../utils/url";
import {SourceFactory} from "../models/sources/SourceFactory";
import Strategy from "../models/strategies/Strategy";
import {StrategyFactory} from "../models/strategies/StrategyFactory";
import {DecompositionFactory} from "../models/decompositions/DecompositionFactory";
import Decomposition from "../models/decompositions/Decomposition";

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
        strategyRecommendation: Strategy
    ) {
        return this.axios.put("/codebase/" + codebaseName + "/recommendation", strategyRecommendation)
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
        codebaseName: string,
        strategyRecommendation: string,
        decompositionNames: string[]
    ) {
        const data = new FormData();
        for (const name of decompositionNames)
            data.append('decompositionNames', name);

        // unfortunately, this is needed so that data.append does not interpret the commas as an array separation
        data.append('decompositionNames', "");

        return this.axios.post<null>("/codebase/" + codebaseName + "/recommendationStrategy/" + strategyRecommendation + "/createDecompositions", data);
    }

    getRecommendationStrategy(codebaseName: string, strategyName: string) {
        return this.axios.get<Strategy>("/codebase/" + codebaseName + "/recommendationStrategy/" + strategyName)
            .then((response) => {return StrategyFactory.getStrategy(response.data)});
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
    addProfile(codebaseName: string, sourceType: string, profile: string) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/source/" + sourceType + "/addProfile",
            null,
            {
                params: {
                    "profile" : profile
                }
            });
    }

    moveFunctionalities(
        codebaseName: string,
        sourceType: string,
        functionalities: string[],
        targetProfile: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/source/" + sourceType + "/moveFunctionalities",
            functionalities,
            {
                params: {
                    "targetProfile" : targetProfile
                }
            }
        );
    }

    deleteProfile(codebaseName: string, sourceType: string, profile: string) {
        return this.axios.delete<null>(
            "/codebase/" + codebaseName + "/source/" + sourceType + "/deleteProfile",
            {
                params: {
                    "profile" : profile
                }
            }
        );
    }

    //Sources
    addCollector(
        codebaseName: string,
        collectorName: string,
        sources: Map<string, File>
    ) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        const data = new FormData();
        data.append("collectorName", collectorName);
        Object.entries(sources).forEach((entry) => {data.append("sourceTypes", entry[0]); data.append("sources", entry[1])});

        return this.axios.post<null>("/codebase/" + codebaseName + "/addCollector", data, config);
    }

    deleteCollector(codebaseName: string, collectorType: string, sources: string[], possibleStrategies: string[]) {
        return this.axios.delete<null>(addSearchParamsToUrl("/codebase/" + codebaseName + "/collector/" + collectorType + "/delete", {sources, possibleStrategies}));
    }

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

    getSource(codebaseName: string, sourceType: string) {
        return this.axios.get("/codebase/" + codebaseName + "/source/" + sourceType + "/getSource")
            .then((response) => {
                return SourceFactory.getSource(response.data);
            });
    }

    getInputFile(codebaseName: string, sourceType: string) {
        return this.axios.get<string>("/codebase/" + codebaseName + "/source/" + sourceType + "/getInputFile");
    }


    //Strategies
    getStrategies(codebaseName: string, strategyTypes?: string[]) {
        return this.axios.get(addSearchParamsToUrl(
            "/codebase/" + codebaseName + "/strategies",
            strategyTypes? {strategyTypes} : {},
        )).then((responseList) => {
            if (responseList.data.length == 0)
                return responseList.data;
            return responseList.data.map((response: any) => {
                return StrategyFactory.getStrategy(response);
            });
        });
    }

    getStrategy(codebaseName: string, strategyName: string) {
        return this.axios.get<Strategy>("/codebase/" + codebaseName + "/strategy/" + strategyName);
    }

    deleteStrategy(codebaseName: string, strategyName: string) {
        return this.axios.delete<null>("/codebase/" + codebaseName + "/strategy/" + strategyName + "/delete");
    }
    
    createStrategy(strategy: Strategy) {
        return this.axios.post<null>("/codebase/" + strategy.codebaseName + "/strategy/createStrategy", strategy);
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

    getFunctionalitiesClusters(
        codebaseName: string,
        strategyName: string,
        decompositionName: string,
    ) {
        return this.axios.get<Record<string, Cluster[]>>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/functionalitiesClusters"
        );
    }

    getClustersFunctionalities(
        codebaseName: string,
        strategyName: string,
        decompositionName: string
    ) {
        return this.axios.get<Record<number, Functionality[]>>(
            "/codebase/" + codebaseName + "/strategy/" + strategyName + "/decomposition/" + decompositionName + "/clustersFunctionalities"
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