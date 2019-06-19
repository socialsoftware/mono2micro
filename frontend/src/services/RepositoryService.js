import axios from 'axios';
import { URL } from '../constants/constants';

export class RepositoryService {
    constructor() {
        var headers = {
            'X-Custom-Header': 'Mono2Micro',
        };

        this.axios = axios.create({
            baseURL: URL,
            timeout: 30000,
            headers: headers,
        });
    }

    //Experts
    getExpertNames() {
        return this.axios.get("/expertNames");
    }

    getExperts() {
        return this.axios.get("/experts");
    }

    getExpert(name) {
        return this.axios.get("/expert/" + name);
    }

    deleteExpert(name) {
        return this.axios.delete("/expert/" + name + "/delete");
    }

    addCluster(expertName, cluster) {
        return this.axios.get("/expert/" + expertName + "/addCluster", 
            {
                params: {
                    "cluster" : cluster
                }
            });
    }

    moveEntities(expertName, entities, cluster) {
        return this.axios.post("/expert/" + expertName + "/moveEntities?cluster=" + cluster , 
            entities
        );
    }

    deleteCluster(expertName, cluster) {
        return this.axios.delete("/expert/" + expertName + "/deleteCluster", 
            {
                params: {
                    "cluster" : cluster
                }
            });
    }

    createExpert(name, codebase) {
        return this.axios.post("/expert/create", {
            name: name,
            codebase: codebase
        });
    }





    //Codebases
    getCodebaseNames() {
        return this.axios.get("/codebaseNames");
    }

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
        return this.axios.get("/codebase/" + codebaseName + "/addProfile", 
            {
                params: {
                    "profile" : profile
                }
            });
    }

    moveControllers(codebaseName, controllers, profile) {
        return this.axios.post("/codebase/" + codebaseName + "/moveControllers?profile=" + profile , 
            controllers
        );
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
        data.append('name', name);
        data.append('datafile', datafile);
        
        return this.axios.post("/codebase/create", data, config);
    }






    //Dendrograms
    getDendrogramNames() {
        return this.axios.get("/dendrogramNames");
    }

    getDendrograms() {
        return this.axios.get("/dendrograms");
    }

    getDendrogram(name) {
        return this.axios.get("/dendrogram/" + name);
    }

    deleteDendrogram(name) {
        return this.axios.delete("/dendrogram/" + name + "/delete");
    }
    
    createDendrogram(name, linkageType, accessMetricWeight, readWriteMetricWeight, sequenceMetricWeight, codebase, profiles) {
        const dendrogramData = {
            name: name,
            linkageType: linkageType,
            accessMetricWeight: accessMetricWeight,
            readWriteMetricWeight: readWriteMetricWeight,
            sequenceMetricWeight: sequenceMetricWeight,
            codebase: codebase,
            profiles: profiles
        };
        
        return this.axios.post("/dendrogram/create", dendrogramData);
    }

    cutDendrogram(dendrogramName, cutValue) {
        const graphData = {
            dendrogramName: dendrogramName,
            cutValue: cutValue
        };
        return this.axios.post("/dendrogram/" + dendrogramName + "/cut", graphData);
    }






    //Graph
    getGraphs(dendrogramName) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/graphs");
    }

    getGraph(dendrogramName, graphName) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/graph/" + graphName);
    }

    renameGraph(dendrogramName, graphName, newName) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/graph/" + graphName + "/rename", 
            {
                params: {
                    "newName" : newName
                }
            });
    }

    deleteGraph(dendrogramName, graphName) {
        return this.axios.delete("/dendrogram/" + dendrogramName + "/graph/" + graphName + "/delete");
    }

    

    

    



    //Cluster
    mergeClusters(dendrogramName, graphName, clusterName, otherCluster, newName) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/merge", 
            {
                params: {
                    "otherCluster" : otherCluster,
                    "newName" : newName
                }
            });
    }

    renameCluster(dendrogramName, graphName, clusterName, newName) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/rename", 
            {
                params: {
                    "newName" : newName
                }
            });
    }

    splitCluster(dendrogramName, graphName, clusterName, newName, entities) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/split", 
            {
                params: {
                    "newName" : newName,
                    "entities" : entities
                }
            });
    }

    transferEntities(dendrogramName, graphName, clusterName, toCluster, entities) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/transferEntities", 
            {
                params: {
                    "toCluster" : toCluster,
                    "entities" : entities
                }
            });
    }

    getControllerClusters(dendrogramName, graphName) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/graph/" + graphName + "/controllerClusters");
    }

    getClusterControllers(dendrogramName, graphName) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/graph/" + graphName + "/clusterControllers");
    }



    //Controller
    getControllers(dendrogramName) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/controllers");
    }

    getController(dendrogramName, controllerName) {
        return this.axios.get("/dendrogram/" + dendrogramName + "/controller/" + controllerName);
    }
}