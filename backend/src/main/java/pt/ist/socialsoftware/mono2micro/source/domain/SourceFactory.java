package pt.ist.socialsoftware.mono2micro.source.domain;

import static pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.source.domain.TranslationSource.TRANSLATION;

public class SourceFactory {
    private static SourceFactory factory = null;

    public static SourceFactory getFactory() {
        if (factory == null)
            factory = new SourceFactory();
        return factory;
    }

    public Source getSource(String sourceType) {
        switch (sourceType) {
            case ACCESSES:
                return new AccessesSource();
            case TRANSLATION:
                return new TranslationSource();
            default:
                throw new RuntimeException("The type \"" + sourceType + "\" is not a valid source type.");
        }
    }
}
