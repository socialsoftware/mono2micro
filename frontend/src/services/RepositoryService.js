import axios from 'axios';
import { URL } from '../constants/constants';
import { ProfileGroup } from './../components/graph/ProfileGroup';

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

    //Profile Groups
    createProfileGroup(dataFile) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        return this.axios.post("/createProfileGroup", dataFile, config);
    }

    getProfileGroupNames() {
        return this.axios.get("/profileGroupNames");
    }

    getProfileGroups() {
        return this.axios.get("/profileGroups");
    }

    getProfileGroup(profileGroupName) {
        return this.axios.get("/profileGroup", 
            {
                params: {
                    "profileGroupName" : profileGroupName
                }
            });
    }

    deleteProfileGroup(profileGroupName) {
        return this.axios.delete("/deleteProfileGroup", 
            {
                params: {
                    "profileGroupName" : profileGroupName
                }
            });
    }


    //Profile
    addProfile(profileGroupName, profile) {
        return this.axios.get("/addProfile", 
            {
                params: {
                    "profileGroupName" : profileGroupName,
                    "profile" : profile
                }
            });
    }

    moveController(profileGroupName, moveController, moveToProfile) {
        return this.axios.get("/moveController", 
            {
                params: {
                    "profileGroupName" : profileGroupName,
                    "moveController" : moveController,
                    "moveToProfile" : moveToProfile
                }
            });
    }

    deleteProfile(profileGroupName, profileName) {
        return this.axios.delete("/deleteProfile", 
            {
                params: {
                    "profileGroupName" : profileGroupName,
                    "profileName" : profileName
                }
            });
    }



    //Dendrogram
    getDendrogramNames() {
        return this.axios.get("/dendrogramNames");
    }

    getDendrograms() {
        return this.axios.get("/dendrograms");
    }

    deleteDendrogram(dendrogramName) {
        return this.axios.delete("/deleteDendrogram", 
            {
                params: {
                    "dendrogramName" : dendrogramName
                }
            });
    }
    
    createDendrogram(dendrogramName, linkageType, accessMetricWeight, readWriteMetricWeight, sequenceMetricWeight, profileGroupName, profiles) {
        return this.axios.get("/createDendrogram", 
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "linkageType" : linkageType,
                    "accessMetricWeight" : accessMetricWeight,
                    "readWriteMetricWeight" : readWriteMetricWeight,
                    "sequenceMetricWeight" : sequenceMetricWeight,
                    "profileGroupName" : profileGroupName,
                    "profiles" : profiles
                }
            });
    }

    getDendrogram(dendrogramName) {
        return this.axios.get("/dendrogram", 
            {
                params: {
                    "dendrogramName" : dendrogramName
                }
            });
    }

    cutDendrogram(dendrogramName, cutValue) {
        return this.axios.get("/cutDendrogram",
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "cutValue" : cutValue
                }
            });
    }



    //Graph
    deleteGraph(dendrogramName, graphName) {
        return this.axios.delete("/deleteGraph", 
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "graphName" : graphName
                }
            });
    }

    getGraphs(dendrogramName) {
        return this.axios.get("/graphs", 
            {
                params: {
                    "dendrogramName" : dendrogramName
                }
            });
    }

    getGraph(dendrogramName, graphName) {
        return this.axios.get("/graph", 
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "graphName" : graphName
                }
            });
    }

    renameGraph(dendrogramName, graphName, newName) {
        return this.axios.get("/renameGraph", 
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "graphName" : graphName,
                    "newName" : newName
                }
            });
    }



    //Cluster
    mergeClusters(dendrogramName, graphName, cluster1, cluster2, newName) {
        return this.axios.get("/mergeClusters", 
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "graphName" : graphName,
                    "cluster1" : cluster1,
                    "cluster2" : cluster2,
                    "newName" : newName
                }
            });
    }

    renameCluster(dendrogramName, graphName, clusterName, newName) {
        return this.axios.get("/renameCluster", 
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "graphName" : graphName,
                    "clusterName" : clusterName,
                    "newName" : newName
                }
            });
    }

    splitCluster(dendrogramName, graphName, clusterName, newName, entitiesToExtract) {
        return this.axios.get("/splitCluster", 
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "graphName" : graphName,
                    "clusterName" : clusterName,
                    "newName" : newName,
                    "entities" : entitiesToExtract
                }
            });
    }

    transferEntities(dendrogramName, graphName, fromCluster, toCluster, entities) {
        return this.axios.get("/transferEntities", 
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "graphName" : graphName,
                    "fromCluster" : fromCluster,
                    "toCluster" : toCluster,
                    "entities" : entities
                }
            });
    }

    getControllerClusters(dendrogramName, graphName) {
        return this.axios.get("/controllerClusters", 
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "graphName" : graphName
                }
            });
    }

    getClusterControllers(dendrogramName, graphName) {
        return this.axios.get("/clusterControllers", 
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "graphName" : graphName
                }
            });
    }



    //Controller
    getControllers(dendrogramName) {
        return this.axios.get("/controllers", 
            {
                params: {
                    "dendrogramName" : dendrogramName
                }
            });
    }

    getController(dendrogramName, controllerName) {
        return this.axios.get("/controller", 
            {
                params: {
                    "dendrogramName" : dendrogramName,
                    "controllerName" : controllerName
                }
            });
    }
 }