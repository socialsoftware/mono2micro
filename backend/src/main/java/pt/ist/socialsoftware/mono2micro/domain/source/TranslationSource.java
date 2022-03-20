package pt.ist.socialsoftware.mono2micro.domain.source;

import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import static pt.ist.socialsoftware.mono2micro.domain.source.Source.SourceType.TRANSLATION;

public class TranslationSource extends Source {

    @Override
    public void init(String codebaseName, Object inputFile) throws Exception {
        CodebaseManager codebaseManager = CodebaseManager.getInstance();
        this.codebaseName = codebaseName;
        this.inputFilePath = codebaseManager.writeInputFile(codebaseName, getType(), inputFile);
    }

    @Override
    public String getType() {
        return TRANSLATION;
    }
}
