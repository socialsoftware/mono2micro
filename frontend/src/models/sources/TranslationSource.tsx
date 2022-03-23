import Source from "./Source";
import React from "react";

export default class TranslationSource extends Source {

    constructor(source: TranslationSource) {
        super(source.type, source.inputFilePath, source.codebaseName);
    }
}
