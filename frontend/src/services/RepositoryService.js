import axios from 'axios';
import { URL } from '../constants/constants';

export class RepositoryService {
    constructor() {
        var headers = {
            'X-Custom-Header': 'Mono2Micro',
        };

        this.axios = axios.create({
            baseURL: URL,
            timeout: 1000,
            headers: headers,
        });
    }

    // Graphs
    getGraphs() {
        return this.axios.get();
    }

    getGraph(name) {
        return this.axios.get(name);
    }

    loadGraph(name) {
        return this.axios.post("/load", 
            {
                "name" : name
            });
    }

    deleteGraph(name) {
        return this.axios.delete(name);
    }

 }