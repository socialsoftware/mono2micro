export const URL = "http://localhost:8080/mono2micro/";

export const REFACTORIZATION_TOOL_URL = "http://localhost:5001/api/v1/";

export const DEFAULT_REDESIGN_NAME = "Monolith Trace";

export const OPERATION = {
    NONE: 0,
    RENAME: 1,
    ONLY_NEIGHBOURS: 2,
    SHOW_ALL: 3,
    MERGE: 4,
    SPLIT: 5,
    TRANSFER: 6,
    FORM_CLUSTER: 7,
    EXPAND: 8,
    EXPAND_ALL: 9,
    COLLAPSE: 10,
    COLLAPSE_ALL: 11,
    TOGGLE_PHYSICS: 12,
    SAVE: 13,
    SNAPSHOT: 14,
    RESTORE: 15,
    CANCEL: 16,
};