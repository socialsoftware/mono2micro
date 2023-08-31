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

export const ENTITY_ACCESS_TYPE = {
    READ: 1,
    UPDATE: 2,
    CREATE: 4,
    DELETE: 8,
    READ_UPDATE: 1 + 2,
    READ_CREATE: 1 + 4,
    READ_DELETE: 1 + 8,

    fromNumberToString: function (number) {
        switch (number) {
            case this.READ:
                return "R";
            case this.UPDATE:
                return "U";
            case this.CREATE:
                return "C";
            case this.DELETE:
                return "D";
            case this.READ_UPDATE:
                return "RU";
            case this.READ_CREATE:
                return "RC";
            case this.READ_DELETE:
                return "RD";
            default:
                return "?";
        }
    }
};
