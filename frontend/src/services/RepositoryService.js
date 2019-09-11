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

    analyser(data) {
        return this.axios.post("/analyser", data);
    }


    //Experts
    getExpertNames(codebaseName) {
        return this.axios.get("/codebase/" + codebaseName + "/expertNames");
    }

    getExperts(codebaseName) {
        return this.axios.get("/codebase/" + codebaseName + "/experts");
    }

    getExpert(codebaseName, expertName) {
        return this.axios.get("/codebase/" + codebaseName + "/expert/" + expertName);
    }

    deleteExpert(codebaseName, expertName) {
        return this.axios.delete("/codebase/" + codebaseName + "/expert/" + expertName + "/delete");
    }

    addCluster(codebaseName, expertName, cluster) {
        return this.axios.get("/codebase/" + codebaseName + "/expert/" + expertName + "/addCluster", 
            {
                params: {
                    "cluster" : cluster
                }
            });
    }

    moveEntities(codebaseName, expertName, entities, cluster) {
        return this.axios.post("/codebase/" + codebaseName + "/expert/" + expertName + "/moveEntities?cluster=" + cluster , 
            entities
        );
    }

    deleteCluster(codebaseName, expertName, cluster) {
        return this.axios.delete("/codebase/" + codebaseName + "/expert/" + expertName + "/deleteCluster", 
            {
                params: {
                    "cluster" : cluster
                }
            });
    }

    createExpert(codebaseName, expertName) {
        return this.axios.post("/codebase/" + codebaseName + "/expert/create", {
            codebaseName: codebaseName,
            name: expertName
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
    getDendrogramNames(codebaseName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogramNames");
    }

    getDendrograms(codebaseName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrograms");
    }

    getDendrogram(codebaseName, dendrogramName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName);
    }

    deleteDendrogram(codebaseName, dendrogramName) {
        return this.axios.delete("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/delete");
    }
    
    createDendrogram(codebaseName, dendrogramName, linkageType, accessMetricWeight, writeMetricWeight, readMetricWeight, sequenceMetric1Weight, sequenceMetric2Weight, profiles) {
        const dendrogramData = {
            codebaseName: codebaseName,
            name: dendrogramName,
            linkageType: linkageType,
            accessMetricWeight: accessMetricWeight,
            writeMetricWeight: writeMetricWeight,
            readMetricWeight: readMetricWeight,
            sequenceMetric1Weight: sequenceMetric1Weight,
            sequenceMetric2Weight: sequenceMetric2Weight,
            profiles: profiles
        };
        
        return this.axios.post("/codebase/" + codebaseName + "/dendrogram/create", dendrogramData);
    }

    cutDendrogram(codebaseName, dendrogramName, cutValue, cutType) {
        const graphData = {
            dendrogramName: dendrogramName,
            cutValue: cutValue,
            cutType: cutType
        };
        return this.axios.post("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/cut", graphData);
    }






    //Graph
    getGraphs(codebaseName, dendrogramName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graphs");
    }

    getGraph(codebaseName, dendrogramName, graphName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName);
    }

    renameGraph(codebaseName, dendrogramName, graphName, newName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/rename", 
            {
                params: {
                    "newName" : newName
                }
            });
    }

    deleteGraph(codebaseName, dendrogramName, graphName) {
        return this.axios.delete("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/delete");
    }

    

    

    



    //Cluster
    mergeClusters(codebaseName, dendrogramName, graphName, clusterName, otherCluster, newName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/merge", 
            {
                params: {
                    "otherCluster" : otherCluster,
                    "newName" : newName
                }
            });
    }

    renameCluster(codebaseName, dendrogramName, graphName, clusterName, newName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/rename", 
            {
                params: {
                    "newName" : newName
                }
            });
    }

    splitCluster(codebaseName, dendrogramName, graphName, clusterName, newName, entities) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/split", 
            {
                params: {
                    "newName" : newName,
                    "entities" : entities
                }
            });
    }

    transferEntities(codebaseName, dendrogramName, graphName, clusterName, toCluster, entities) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/graph/" + graphName + "/cluster/" + clusterName + "/transferEntities", 
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



    //Controller
    getControllers(codebaseName, dendrogramName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/controllers");
    }

    getController(codebaseName, dendrogramName, controllerName) {
        return this.axios.get("/codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/controller/" + controllerName);
    }
}