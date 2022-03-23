import Source from "./Source";
import React from "react";

export default class AccessesSource extends Source {
    profiles!: Map<string, string>;

    constructor(source: AccessesSource) {
        super(source.type, source.inputFilePath, source.codebaseName);
        this.profiles = source.profiles;
    }
}
