export const URL = "http://localhost:8080/mono2micro/";

export const REFACTORIZATION_TOOL_URL = "http://localhost:5001/api/v1/";

export const DEFAULT_REDESIGN_NAME = "Monolith Trace";

export const CLUSTERING_ALGORITHMS = {
    "SCIPY": { name: "SciPy", value: "SCIPY", hasDendrograms: true }
    // Add more Clustering Algorithms here
}

export const SIMILARITY_GENERATORS = {
    "ACCESSES_LOG": { name: "Accesses Log", value: "ACCESSES_LOG" }
}

export const POSSIBLE_DECOMPOSITIONS = {
    "ACCESSES_LOG": {
        "SCIPY": {
            properties: {

            }
        }
    }
}