import axios from 'axios';
import { URL } from '../constants/constants';

export class RepositoryService {
    constructor() {
        var headers = {
            'X-Custom-Header': 'Mono2Micro',
        };

        this.axios = axios.create({
            baseURL: URL,
            timeout: 10000,
            headers: headers,
        });
    }

    // Graphs
    getGraphs() {
        return this.axios.get("/graphs");
    }

    getGraph(name) {
        return this.axios.get("/graph/" + name);
    }

    loadGraph(name) {
        return this.axios.post("/load", 
            {
                "name" : name
            });
    }

    createDendrogram(dataFile) {
        const config = {
            headers: {
                'content-type': 'multipart/form-data'
            }
        }
        return this.axios.post("/createDendrogram", dataFile, config);
    }

    deleteGraph(name) {
        return this.axios.delete(name);
    }

    loadDendrogram() {
        return this.axios.get("/loadDendrogram");
    }

    cutDendrogram(cutValue) {
        return this.axios.get("/cutDendrogram", 
            {
                params: {
                    "cutValue" : cutValue
                }
            });
    }
 }