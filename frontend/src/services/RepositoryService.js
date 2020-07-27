import axios from 'axios';
import { URL } from '../constants/constants';

export class RepositoryService {
    constructor() {
        var headers = {
            'X-Custom-Header': 'Mono2Micro',
        };

        this.axios = axios.create({
            baseURL: URL,
            timeout: 60000,
            headers: headers,
        });
    }


    //Analysis
    analysis(data) {
        return this.axios.post("/analysis", data);
    }

    analyser(codebaseName, expert, profiles, requestLimit) {
        const analyserData = {
            expert: expert,
            profiles: profiles,
            requestLimit: requestLimit
        };

        return this.axios.post("/codebase/" + codebaseName + "/analyser", analyserData);
    }





    //Codebases
    getCodebases() {
        return this.axios.get("/codebases");
    }

    getCodebase(name) {
        return this.axios.get("/codebase/" + name);
    }

    deleteCodebase(name) {
        return this.axios.delete("/codebase/" + name + "/delete");
    }

    addProfile(codebaseName, profile) {
        return this.axios.post("/codebase/" + codebaseName + "/addProfile", null,
            {
                params: {
                    "profile" : profile
                }
            });
    }

    moveControllers(codebaseName, controllers, targetProfile) {
        return this.axios.post("/codebase/" + codebaseName + "/moveControllers", 
            controllers,
            {
                params: {
                    "targetProfile" : targetProfile
                }
            });
    }

    deleteProfile(codebaseName, profile) {
        return this.axios.delete("/codebase/" + codebaseName + "/deleteProfile", 
            {
                params: {
                    "profile" : profile
                }
            });
    }

    createCodebase(name, datafile) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        var data = new FormData();
        data.append('codebaseName', name);
        data.append('datafile', datafile);
        
        return this.axios.post("/codebase/create", data, config);
    }






    //Dendrograms
    getDendrograms(codebaseName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrograms");
    }

    getDendrogram(codebaseName, dendrogramName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName);
    }

    deleteDendrogram(codebaseName, dendrogramName) {
        return this.axios.delete("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/delete");
    }
    
    createDendrogram(codebaseName, dendrogramName, linkageType, accessMetricWeight, writeMetricWeight, readMetricWeight, sequenceMetricWeight, profiles) {
        const dendrogramData = {
            codebaseName: codebaseName,
            name: dendrogramName,
            linkageType: linkageType,
            accessMetricWeight: accessMetricWeight,
            writeMetricWeight: writeMetricWeight,
            readMetricWeight: readMetricWeight,
            sequenceMetricWeight: sequenceMetricWeight,
            profiles: profiles
        };
        
        return this.axios.post("/codebase/" + codebaseName + "/dendrogram/create", dendrogramData);
    }

    cutDendrogram(codebaseName, dendrogramName, cutValue, cutType) {
        const graphData = {
            codebaseName: codebaseName,
            dendrogramName: dendrogramName,
            expert: false,
            cutValue: cutValue,
            cutType: cutType
        };
        return this.axios.post("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/cut", graphData);
    }

    expertCut(codebaseName, dendrogramName, expertName, expertFile) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        var data = new FormData();
        data.append('expertName', expertName);
        data.append('expertFile', expertFile);

        return this.axios.post("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/expertCut", data, config);
    }





    //Graph
    getGraphs(codebaseName, dendrogramName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graphs");
    }

    getGraph(codebaseName, dendrogramName, graphName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName);
    }

    deleteGraph(codebaseName, dendrogramName, graphName) {
        return this.axios.delete("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/delete");
    }

    

    

    



    //Cluster
    mergeClusters(codebaseName, dendrogramName, graphName, clusterName, otherCluster, newName) {
        return this.axios.post("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/merge", null,
            {
                params: {
                    "otherCluster" : otherCluster,
                    "newName" : newName
                }
            });
    }

    renameCluster(codebaseName, dendrogramName, graphName, clusterName, newName) {
        return this.axios.post("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/rename", null,
            {
                params: {
                    "newName" : newName
                }
            });
    }

    splitCluster(codebaseName, dendrogramName, graphName, clusterName, newName, entities) {
        return this.axios.post("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/split", null,
            {
                params: {
                    "newName" : newName,
                    "entities" : entities
                }
            });
    }

    transferEntities(codebaseName, dendrogramName, graphName, clusterName, toCluster, entities) {
        return this.axios.post("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/transferEntities", null,
            {
                params: {
                    "toCluster" : toCluster,
                    "entities" : entities
                }
            });
    }


    getControllerClusters(codebaseName, dendrogramName, graphName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/controllerClusters");
    }

    getClusterControllers(codebaseName, dendrogramName, graphName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/clusterControllers");
    }



    //FunctionalityRedesign

    addCompensating(codebaseName, dendrogramName, graphName, controllerName, clusterName, entities, fromID) {
        return this.axios.post("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/controller/" + controllerName + "/addCompensating",
            {
                fromID: fromID,
                cluster : clusterName,
                entities : entities
            }, null);
    }
}