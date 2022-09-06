package pt.ist.socialsoftware.mono2micro.source.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;


@Document("source")
public class TranslationSource extends Source {

    public static final String TRANSLATION = "IDToEntity";

    public TranslationSource() {}

    @Override
    public String init(Codebase codebase, byte[] sourceFile) {
        this.name = codebase.getName() + " & " + getType();
        this.codebase = codebase;
        return name;
    }

    @Override
    public String getType() {
        return TRANSLATION;
    }
}
