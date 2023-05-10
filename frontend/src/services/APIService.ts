import axios, { AxiosInstance } from 'axios';
import { 
    URL,
    REFACTORIZATION_TOOL_URL,
} from '../constants/constants';
import {
    ComparisonToolDto,
    Cluster,
    LocalTransactionsGraph,
    RefactorCodebase,
    Edges
} from "../type-declarations/types";
import { addSearchParamsToUrl } from "../utils/url";
import {RepresentationFactory} from "../models/representation/RepresentationFactory";
import Similarity from "../models/similarity/Similarity";
import {SimilarityFactory} from "../models/similarity/SimilarityFactory";
import {DecompositionFactory} from "../models/decompositions/DecompositionFactory";
import Decomposition from "../models/decompositions/Decomposition";
import Codebase from "../models/codebase/Codebase";
import {RecommendationFactory} from "../models/recommendation/RecommendationFactory";
import Recommendation from "../models/recommendation/Recommendation";
import Representation from "../models/representation/Representation";
import Strategy from "../models/strategy/Strategy";

export class APIService {
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
    analysis(decomposition1Name: string, decomposition2Name: string) {
        return this.axios.post<ComparisonToolDto>("/comparison/" + decomposition1Name + "/" + decomposition2Name);
    }

    // Recommendation
    recommendation(
        requestedStrategyRecommendation: Recommendation
    ) {
        return this.axios.put("recommendation/createRecommendation", requestedStrategyRecommendation)
            .then((response) => {return RecommendationFactory.getRecommendation(response.data)});
    }

    getRecommendationResult(
        recommendationName: string
    ) {
        return this.axios.get("/recommendation/" + recommendationName + "/getRecommendationResult")
            .then(response => {
                if (response.data === "")
                    return [];
                else return response.data;
            });
    }

    createRecommendationDecompositions(
        recommendationName: string,
        decompositionNames: string[]
    ) {
        const data = new FormData();
        for (const name of decompositionNames)
            data.append('decompositionNames', name);

        // unfortunately, this is needed so that data.append does not interpret the commas as an array separation
        data.append('decompositionNames', "");

        return this.axios.post<null>("/recommendation/" + recommendationName + "/createDecompositions", data);
    }

    //Codebases
    getCodebases() {
        return this.axios.get("/codebases").then(response => response.data.map((codebase:any) => new Codebase(codebase)));
    }

    getCodebase(codebaseName: string) {
        return this.axios.get("/codebase/" + codebaseName).then(response => new Codebase(response.data));
    }

    getCodebaseStrategies(codebaseName: string) {
        return this.axios.get("/codebase/" + codebaseName + "/getCodebaseStrategies")
            .then(response => {return response.data.map((strategy:any) => new Strategy(strategy))});
    }

    getCodebaseDecompositions(codebaseName: string) {
        return this.axios.get<Decomposition[]>("/codebase/" + codebaseName + "/getCodebaseDecompositions");
    }

    deleteCodebase(name: string) {
        return this.axios.delete<null>("/codebase/" + name + "/delete");
    }

    createCodebase(codebaseName: string) {
        const data = new FormData();
        data.append('codebaseName', codebaseName);
        return this.axios.post<null>("/codebase/create", data);
    }

    getCodebaseRepresentationGroups(codebaseName: string) {
        return this.axios.get("/codebase/" + codebaseName + "/getCodebaseRepresentationGroups");
    }

    getRepresentationGroups() {
        return this.axios.get("/codebase/getRepresentationGroups");
    }

    // Profiles
    addAccessesProfile(representationName: string, profile: string) {
        return this.axios.post<null>(
            "/representation/" + representationName + "/addAccessesProfile",
            null,
            {
                params: {
                    "profile" : profile
                }
            });
    }

    moveAccessesFunctionalities(
        representationName: string,
        functionalities: string[],
        targetProfile: string,
    ) {
        return this.axios.post<null>(
            "/representation/" + representationName + "/moveAccessesFunctionalities",
            functionalities,
            {
                params: {
                    "targetProfile" : targetProfile
                }
            }
        );
    }

    deleteAccessesProfile(representationName: string, profile: string) {
        return this.axios.delete<null>(
            "/representation/" + representationName + "/deleteAccessesProfile",
            {
                params: {
                    "profile" : profile
                }
            }
        );
    }

    //Representation

    addRepresentations(
        codebaseName: string,
        selectedRepresentationType: string,
        representations: Map<string, File>
    ) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        const data = new FormData();
        Object.entries(representations).forEach((entry) => {data.append("representationTypes", entry[0]); data.append("representations", entry[1])});

        return this.axios.post<null>("/codebase/" + codebaseName + "/addRepresentations/" + selectedRepresentationType, data, config);
    }

    getRepresentations(codebaseName: string) {
        return this.axios.get<Representation[]>("/codebase/" + codebaseName + "/getRepresentations")
            .then((response) => RepresentationFactory.getRepresentations(response.data));
    }

    getCodebaseRepresentation(codebaseName: string, representationType: string) {
        return this.axios.get("/codebase/" + codebaseName + "/representation/" + representationType + "/getCodebaseRepresentation")
            .then((response) => RepresentationFactory.getRepresentation(response.data));
    }

    getRepresentation(representationName: string) {
        return this.axios.get("/representation/" + representationName + "/getRepresentation")
            .then((response) => RepresentationFactory.getRepresentation(response.data));
    }

    deleteRepresentation(id: string) {
        return this.axios.delete<null>("/representation/" + id + "/delete");
    }

    getIdToEntity(codebaseName: string) {
        return this.axios.get<string>("/representation/" + codebaseName + "/getIdToEntity");
    }


    //Similarities
    deleteSimilarity(similarityName: string) {
        return this.axios.delete<null>("/similarity/" + similarityName + "/delete");
    }

    getSimilarity(similarityName: string) {
        return this.axios.get("/similarity/" + similarityName + "/getSimilarity").then(
            response => { return SimilarityFactory.getSimilarity(response.data); });
    }

    createSimilarity(similarity: Similarity) {
        return this.axios.post<null>("/similarity/create", similarity);
    }

    getAllowableCodebaseStrategyTypes(codebaseName: string) {
        return this.axios.get("/codebase/" + codebaseName + "/getAllowableCodebaseStrategyTypes");
    }

    getAlgorithmSupportedStrategyTypes(algorithmType: string) {
        return this.axios.get("/clustering/" + algorithmType + "/getAlgorithmSupportedStrategyTypes");
    }


    //Strategies
    createStrategy(
        codebaseName: string,
        strategyRepresentations: string[],
        strategyAlgorithm: string
    ) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        const data = new FormData();
        strategyRepresentations.forEach(repr => data.append("strategyTypes", repr));
        data.append("algorithmType", strategyAlgorithm);

        return this.axios.post<null>("/codebase/" + codebaseName + "/createStrategy", data, config);
    }

    getAlgorithms() {
        return this.axios.get("/strategy/getAlgorithms");
    }

    getStrategySimilarities(strategyName: string) {
        return this.axios.get("/strategy/" + strategyName + "/getStrategySimilarities").then(
            response => response.data.map((similarity: any) => SimilarityFactory.getSimilarity(similarity)));
    }

    getStrategyDecompositions(strategyName: string) {
        return this.axios.get("/strategy/" + strategyName + "/getStrategyDecompositions").then(
            response => response.data.map((decomposition: any) => DecompositionFactory.getDecomposition(decomposition)));
    }

    getStrategy(strategyName: string) {
        return this.axios.get("/strategy/" + strategyName + "/getStrategy").then(
            response => new Strategy(response.data));
    }

    deleteStrategy(strategyName: string) {
        return this.axios.delete<null>("/strategy/" + strategyName + "/delete");
    }

    getEdgeWeights(decompositionName: string, viewType: string) {
        return this.axios.get<Edges>("/decomposition/" + decompositionName + "/" + viewType + "/getEdgeWeights");
    }

    getGraphPositions(decompositionName: string) {
        return this.axios.get("/positionHistory/" + decompositionName + "/getGraphPositions");
    }

    saveGraphPositions(decompositionName: string, graphPositions: any) {
        return this.axios.post<null>("/positionHistory/" + decompositionName + "/saveGraphPositions", graphPositions);
    }

    snapshotDecomposition(decompositionName: string) {
        return this.axios.get<null>("/decomposition/" + decompositionName + "/snapshotDecomposition");
    }

    createDecomposition(decompositionDto: any) {
        return this.axios.post("/similarity/createDecomposition", decompositionDto);
    }

    createExpertDecomposition(
        similarityName: string,
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

        return this.axios.post<null>("/similarity/" + similarityName + "/createExpertDecomposition", data, config);
    }

    //Decomposition

    getDecompositions(
        similarityName: string
    ) {
        return this.axios.get<Decomposition[]>(
            "/similarity/" + similarityName + "/decompositions",
        ).then(responseList => {
            if (responseList.data.length == 0)
                return responseList.data;
            return responseList.data.map((response: any) => {
                return DecompositionFactory.getDecomposition(response);
            });
        });
    }

    getDecomposition(
        decompositionName: string
    ) {
        return this.axios.get<Decomposition>("/decomposition/" + decompositionName)
            .then(response => DecompositionFactory.getDecomposition(response.data));
    }

    getClusters(
        decompositionName: string
    ) {
        return this.axios.get<Record<number, Cluster>>("/decomposition/" + decompositionName + "/getClusters");
    }

    updateDecomposition(
        decompositionName: string
    ) {
        return this.axios.get<Decomposition>(
            "/decomposition/" + decompositionName + "/update"
        );
    }

    async exportDecomposition(decompositionName: string) {
        return await this.axios.get(
            "/decomposition/" + decompositionName + "/export",
            {responseType: 'blob',}
        );
    }

    deleteDecomposition(
        decompositionName: string
    ) {
        return this.axios.delete<null>(
            "/decomposition/" + decompositionName + "/delete"
        );
    }

    getLocalTransactionsGraphForFunctionality(
        decompositionName: string,
        functionalityName: string
    ) {
        return this.axios.get<LocalTransactionsGraph>(
            addSearchParamsToUrl(
                "/accesses/" + decompositionName + "/getLocalTransactionsGraphForFunctionality",
                { functionalityName: functionalityName },
            )
        );
    }

    //Cluster

    mergeClusters(
        decompositionName: string,
        data: any
    ) {
        return this.axios.post<null>(
            "/decomposition/" + decompositionName + "/merge", data);
    }

    renameCluster(
        decompositionName: string,
        data: any
    ) {
        return this.axios.post<null>("/decomposition/" + decompositionName + "/rename", data);
    }

    splitCluster(
        decompositionName: string,
        data: any
    ) {
        return this.axios.post<null>("/decomposition/" + decompositionName + "/split", data);
    }

    formCluster(
        decompositionName: string,
        data: any
    ) {
        return this.axios.post<null>("/decomposition/" + decompositionName + "/formCluster", data);
    }

    transferEntities(
        decompositionName: string,
        data: any
    ) {
        return this.axios.post<null>("/decomposition/" + decompositionName + "/transfer", data);
    }

    undoOperation(
        decompositionName: string
    ) {
        return this.axios.get("/history/" + decompositionName + "/undoOperation");
    }

    redoOperation(
        decompositionName: string
    ) {
        return this.axios.get("/history/" + decompositionName + "/redoOperation");
    }

    canUndoRedo(
        decompositionName: string
    ) {
        return this.axios.get<Record<string, Boolean>>("/history/" + decompositionName + "/canUndoRedo");
    }

    getFunctionalitiesAndFunctionalitiesClusters(
        decompositionName: string,
    ) {
        return this.axios.get<Record<string, any>>(
            "/accesses/" + decompositionName + "/getFunctionalitiesAndFunctionalitiesClusters"
        );
    }

    getClustersAndClustersFunctionalities(
        decompositionName: string
    ) {
        return this.axios.get<Record<string, any>>(
            "/accesses/" + decompositionName + "/getClustersAndClustersFunctionalities"
        );
    }

    getSearchItems(
        decompositionName: string,
        viewType: string
) {
        return this.axios.get<any[]>(
            "/decomposition/" + decompositionName + "/" + viewType + "/getSearchItems"
        );
    }


    //FunctionalityRedesign

    addCompensating(
        decompositionName: string,
        functionalityName: string,
        redesignName: string,
        clusterID: number,
        entities: string,
        fromID: string
    ) {
        return this.axios.post(
            "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/addCompensating",
            {
                fromID: fromID,
                cluster : clusterID,
                entities : entities
            }
        );
    }

    sequenceChange(
        decompositionName: string,
        functionalityName: string,
        redesignName: string,
        localTransaction: string,
        newCaller: string
    ) {
        return this.axios.post(
            "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/sequenceChange",
            {
                localTransactionID: localTransaction,
                newCaller: newCaller
            }
        );
    }

    dcgi(
        decompositionName: string,
        functionalityName: string,
        redesignName: string,
        fromClusterID: number,
        toClusterID: number,
        localTransactions: string
    ) {
        return this.axios.post(
            "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/dcgi",
            {
                fromCluster: fromClusterID,
                toCluster: toClusterID,
                localTransactions: localTransactions
            }
        );
    }

    selectPivotTransaction(
        decompositionName: string,
        functionalityName: string,
        redesignName: string,
        transactionID: string,
        newRedesignName: string
    ) {
        return this.axios.post(
            "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/pivotTransaction",
            null,
            { params: {"transactionID" : transactionID, "newRedesignName": newRedesignName}}
        );
    }

    changeLTName(
        decompositionName: string,
        functionalityName: string,
        redesignName: string,
        transactionID: string,
        newName: string
    ) {
        return this.axios.post(
            "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/changeLTName",
            null,
            { params: {"transactionID" : transactionID, "newName": newName}}
        );
    }

    deleteRedesign(
        decompositionName: string,
        functionalityName: string,
        redesignName: string
    ){
        return this.axios.delete(
            "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/deleteRedesign"
        );
    }

    setUseForMetrics(
        decompositionName: string,
        functionalityName: string,
        redesignName: string
    ){
        return this.axios.post(
            "/decomposition/" + decompositionName + "/functionality/" + functionalityName + "/redesign/" + redesignName + "/useForMetrics"
        );
    }

    refactorCodebase(
        decompositionName: string,
        functionalityNames: string[],
        dataDependenceThreshold: number,
        minimizeSumOfComplexities: boolean,
        timeOut: number,
        newRefactor: boolean,
    ) {

        const refactorRequest: RefactorCodebase = {
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
        decompositionName: string,
    ) {

        return this.refactorizationToolAxios.get(
            "/refactor/decomposition/" + decompositionName ,
        );
    }
}