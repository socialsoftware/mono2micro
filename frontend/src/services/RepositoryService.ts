import axios, { AxiosInstance } from 'axios';
import { 
    URL,
    REFACTORIZATION_TOOL_URL,
} from '../constants/constants';
import {
    AnalysisDto,
    Cluster,
    LocalTransactionsGraph,
    RefactorCodebase,
    Edges
} from "../type-declarations/types";
import { addSearchParamsToUrl } from "../utils/url";
import {RepresentationFactory} from "../models/representation/RepresentationFactory";
import Dendrogram from "../models/dendrogram/Dendrogram";
import {DendrogramFactory} from "../models/dendrogram/DendrogramFactory";
import {DecompositionFactory} from "../models/decompositions/DecompositionFactory";
import Decomposition from "../models/decompositions/Decomposition";
import Codebase from "../models/codebase/Codebase";
import {StrategyFactory} from "../models/strategy/StrategyFactory";
import {RecommendationFactory} from "../models/recommendation/RecommendationFactory";
import Recommendation from "../models/recommendation/Recommendation";

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
    analysis(decomposition1Name: string, decomposition2Name: string) {
        return this.axios.post<AnalysisDto>("/analysis/" + decomposition1Name + "/" + decomposition2Name);
    }

    // Recommendation
    recommendation(
        requestedStrategyRecommendation: Recommendation
    ) {
        return this.axios.put("recommendation/createRecommendAccessesSciPy", requestedStrategyRecommendation)
            .then((response) => {return RecommendationFactory.getRecommendation(response.data)});
    }

    getRecommendationResult(
        recommendationName: string
    ) {
        return this.axios.get("/recommendAccessesSciPy/" + recommendationName + "/getRecommendationResult")
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

        return this.axios.post<null>("/recommendAccessesSciPy/" + recommendationName + "/createDecompositions", data);
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
            .then(response => {return response.data.map((strategy:any) => StrategyFactory.getStrategy(strategy))});
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
    getCodebaseRepresentation(codebaseName: string, representationType: string) {
        return this.axios.get("/codebase/" + codebaseName + "/representation/" + representationType + "/getCodebaseRepresentation")
            .then((response) => RepresentationFactory.getRepresentation(response.data));
    }

    getRepresentation(representationName: string) {
        return this.axios.get("/representation/" + representationName + "/getRepresentation")
            .then((response) => RepresentationFactory.getRepresentation(response.data));
    }

    getRepresentationTypes(codebaseName: string) {
        return this.axios.get<string[]>("/codebase/" + codebaseName + "/getRepresentationTypes");
    }

    deleteRepresentation(id: string) {
        return this.axios.delete<null>("/representation/" + id + "/delete");
    }

    getIdToEntity(codebaseName: string) {
        return this.axios.get<string>("/representation/" + codebaseName + "/getIdToEntity");
    }


    //Dendrograms
    deleteDendrogram(dendrogramName: string) {
        return this.axios.delete<null>("/dendrogram/" + dendrogramName + "/delete");
    }

    getDendrogram(dendrogramName: string) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/getDendrogram").then(
            response => { return DendrogramFactory.getDendrogram(response.data); });
    }

    createAccessesSciPyDendrogram(dendrogram: Dendrogram) {
        return this.axios.post<null>("/dendrogram/createAccessesSciPyDendrogram", dendrogram);
    }


    //Strategies
    createStrategy(
        codebaseName: string,
        strategyType: string,
        representations: Map<string, File>
    ) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        const data = new FormData();
        Object.entries(representations).forEach((entry) => {data.append("representationTypes", entry[0]); data.append("representations", entry[1])});

        return this.axios.post<null>("/codebase/" + codebaseName + "/strategy/" + strategyType + "/createStrategy", data, config);
    }

    getStrategyDendrograms(strategyName: string) {
        return this.axios.get("/strategy/" + strategyName + "/getStrategyDendrograms").then(
            response => response.data.map((dendrogram: any) => DendrogramFactory.getDendrogram(dendrogram)));
    }

    getStrategyDecompositions(strategyName: string) {
        return this.axios.get("/strategy/" + strategyName + "/getStrategyDecompositions").then(
            response => response.data.map((decomposition: any) => DecompositionFactory.getDecomposition(decomposition)));
    }

    getStrategy(strategyName: string) {
        return this.axios.get("/strategy/" + strategyName + "/getStrategy").then(
            response => StrategyFactory.getStrategy(response.data));
    }

    deleteStrategy(strategyName: string) {
        return this.axios.delete<null>("/strategy/" + strategyName + "/delete");
    }

    getEdgeWeights(decompositionName: string) {
        return this.axios.get<Edges>("/accessesSciPyDecomposition/" + decompositionName + "/getEdgeWeights");
    }

    getGraphPositions(decompositionName: string) {
        return this.axios.get("/accessesSciPyLog/" + decompositionName + "/getGraphPositions");
    }

    deleteGraphPositions(decompositionName: string) {
        return this.axios.delete("/accessesSciPyLog/" + decompositionName + "/deleteGraphPositions");
    }

    saveGraphPositions(decompositionName: string, graphPositions: any) {
        return this.axios.post<null>("/accessesSciPyLog/" + decompositionName + "/saveGraphPositions", graphPositions);
    }

    snapshotDecomposition(decompositionName: string) {
        return this.axios.get<null>("/accessesSciPyDecomposition/" + decompositionName + "/snapshotDecomposition");
    }

    createAccessesSciPyDecomposition(
        dendrogramName: string,
        cutType: string,
        cutValue: number
    ) {
        return this.axios.post(addSearchParamsToUrl(
            "/dendrogram/" + dendrogramName + "/createAccessesSciPyDecomposition",
            { cutType: cutType, cutValue: cutValue.toString() },
        ));
    }

    createAccessesSciPyExpertDecomposition(
        dendrogramName: string,
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
            "/dendrogram/" + dendrogramName + "/createAccessesSciPyExpertDecomposition",
            data,
            config
        );
    }

    //Decomposition
    getDecompositions(
        dendrogramName: string
    ) {
        return this.axios.get<Decomposition[]>(
            "/dendrogram/" + dendrogramName + "/decompositions",
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
        return this.axios.get<Decomposition>(
            "/decomposition/" + decompositionName
        );
    }

    updatedAccessesSciPyDecomposition(
        decompositionName: string
    ) {
        return this.axios.get<Decomposition>(
            "/accessesSciPyDecomposition/" + decompositionName + "/updatedAccessesSciPyDecomposition"
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
                "/accessesSciPyDecomposition/" + decompositionName + "/getLocalTransactionsGraphForFunctionality",
                { functionalityName: functionalityName },
            )
        );
    }

    //Cluster
    mergeClusters(
        decompositionName: string,
        clusterName: number,
        otherClusterName: number,
        newName: string,
    ) {
        return this.axios.post<null>(
            "/decomposition/" + decompositionName + "/cluster/" + clusterName + "/merge",
            null,
            {
                params: {
                    "otherClusterName" : otherClusterName,
                    "newName" : newName
                }
            }
        );
    }

    renameCluster(
        decompositionName: string,
        clusterName: number,
        newName: string
    ) {
        return this.axios.post<null>(
            "/decomposition/" + decompositionName + "/cluster/" + clusterName + "/rename",
            null,
            {
                params: {
                    "newName" : newName
                },
            }
        );
    }

    splitCluster(
        decompositionName: string,
        clusterName: number,
        newName: string,
        entities: string,
    ) {
        return this.axios.post<null>(
            "/decomposition/" + decompositionName + "/cluster/" + clusterName + "/split",
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
        decompositionName: string,
        newName: string,
        entities: Map<string, number[]>,
    ) {
        return this.axios.post<null>(
            "/decomposition/" + decompositionName + "/formCluster",
            entities,
            {
                params: {
                    "newName" : newName,
                }
            }
        );
    }

    undoOperation(
        decompositionName: string
    ) {
        return this.axios.get<Record<number, Cluster>>("/accessesSciPyLog/" + decompositionName + "/undoOperation");
    }

    redoOperation(
        decompositionName: string
    ) {
        return this.axios.get<Record<number, Cluster>>("/accessesSciPyLog/" + decompositionName + "/redoOperation");
    }

    canUndoRedo(
        decompositionName: string
    ) {
        return this.axios.get<Record<string, Boolean>>("/accessesSciPyLog/" + decompositionName + "/canUndoRedo");
    }

    transferEntities(
        decompositionName: string,
        clusterName: number,
        toClusterName: number,
        entities: string,
    ) {
        return this.axios.post<null>(
            "/decomposition/" + decompositionName + "/cluster/" + clusterName + "/transferEntities",
            null,
            {
                params: {
                    "toClusterName" : toClusterName,
                    "entities" : entities
                }
            }
        );
    }

    getFunctionalitiesAndFunctionalitiesClusters(
        decompositionName: string,
    ) {
        return this.axios.get<Record<string, any>>(
            "/accessesSciPyDecomposition/" + decompositionName + "/getFunctionalitiesAndFunctionalitiesClusters"
        );
    }

    getClustersAndClustersFunctionalities(
        decompositionName: string
    ) {
        return this.axios.get<Record<string, any>>(
            "/accessesSciPyDecomposition/" + decompositionName + "/getClustersAndClustersFunctionalities"
        );
    }

    getSearchItems(
        decompositionName: string
    ) {
        return this.axios.get<any[]>(
            "/accessesSciPyDecomposition/" + decompositionName + "/getSearchItems"
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