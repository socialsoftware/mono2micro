import axios, { AxiosInstance } from 'axios';
import { URL } from '../constants/constants';
import { AnalyserDto, Graph, AnalysisDto, Codebase, TypeOfTraces, Dendrogram, Cluster, Controller } from "../type-declarations/types";
import { addSearchParamsToUrl } from "../utils/url";

export class RepositoryService {
    axios: AxiosInstance;

    constructor() {
        var headers = {
            'X-Custom-Header': 'Mono2Micro',
        };

        this.axios = axios.create({
            baseURL: URL,
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
        expert: Graph,
        profiles: string[],
        requestLimit: number,
        amountOfTraces: number,
        typeOfTraces: TypeOfTraces,
    ) {
        const analyserData: AnalyserDto = {
            expert: expert || {},
            profiles,
            requestLimit,
            typeOfTraces,
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
            { fieldNames }
        ));
    }

    getCodebase(codebaseName: string, fieldNames?: string[]) {
        return this.axios.get<Codebase>(addSearchParamsToUrl(
            "/codebase/" + codebaseName,
            { fieldNames }
        ));
    }

    //Dendrograms
    getCodebaseGraphs(
        codebaseName: string,
        fieldNames?: string[],
    ) {
        return this.axios.get<Graph[]>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/graphs",
                { fieldNames }
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
        analysisType: string,
    ) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        var data = new FormData();
        data.append('codebaseName', name);
        data.append('datafile', datafile);
        data.append('analysisType', analysisType);
        
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
                { fieldNames }
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
        profiles: string[],
        amountOfTraces: number,
        typeOfTraces: TypeOfTraces,
    ) {
        const dendrogramData: Dendrogram = {
            codebaseName,
            name: dendrogramName,
            linkageType,
            accessMetricWeight,
            writeMetricWeight,
            readMetricWeight,
            sequenceMetricWeight,
            profiles,
            tracesMaxLimit: amountOfTraces,
            typeOfTraces,
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
        const graphData: Graph = {
            codebaseName,
            dendrogramName,
            expert: false,
            cutValue,
            cutType,
        };

        console.log(graphData);
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/cut",
            graphData
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

    //Graph
    getGraphs(
        codebaseName: string,
        dendrogramName: string,
        fieldNames?: string[],
    ) {
        return this.axios.get<Graph[]>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graphs",
                { fieldNames }
            )
        );
    }

    getGraph(
        codebaseName: string,
        dendrogramName: string,
        graphName: string,
        fieldNames?: string[],
    ) {
        return this.axios.get<Graph>(
            addSearchParamsToUrl(
                "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName,
                { fieldNames }
            )
        );
    }

    deleteGraph(
        codebaseName: string,
        dendrogramName: string,
        graphName: string,
    ) {
        return this.axios.delete<null>("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/delete");
    }

    //Cluster
    mergeClusters(
        codebaseName: string,
        dendrogramName: string,
        graphName: string,
        clusterName: string,
        otherCluster: string,
        newName: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/merge",
            null,
            {
                params: {
                    "otherCluster" : otherCluster,
                    "newName" : newName
                }
            });
    }

    renameCluster(
        codebaseName: string,
        dendrogramName: string,
        graphName: string,
        clusterName: string,
        newName: string
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/rename",
            null,
            {
                params: {
                    "newName" : newName
                },
            });
    }

    splitCluster(
        codebaseName: string,
        dendrogramName: string,
        graphName: string,
        clusterName: string,
        newName: string,
        entities: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/split",
            null,
            {
                params: {
                    "newName" : newName,
                    "entities" : entities
                }
            });
    }

    transferEntities(
        codebaseName: string,
        dendrogramName: string,
        graphName: string,
        clusterName: string,
        toCluster: string,
        entities: string,
    ) {
        return this.axios.post<null>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/transferEntities",
            null,
            {
                params: {
                    "toCluster" : toCluster,
                    "entities" : entities
                }
            });
    }

    getControllerClusters(
        codebaseName: string,
        dendrogramName: string,
        graphName: string,
    ) {
        return this.axios.get<Record<string, Cluster[]>>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/controllerClusters"
        );
    }

    getClusterControllers(
        codebaseName: string,
        dendrogramName: string,
        graphName: string
    ) {
        return this.axios.get<Record<string, Controller[]>>(
            "/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/clusterControllers"
        );
    }
}