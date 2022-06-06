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
    TRANSFER_ENTITY: 7,
    EXPAND: 8,
    COLLAPSE: 9,
    TOGGLE_PHYSICS: 10,
    SAVE: 11,
    RESTORE: 12,
    CANCEL: 13,
};