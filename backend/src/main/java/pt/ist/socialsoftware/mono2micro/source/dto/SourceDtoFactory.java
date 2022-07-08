package pt.ist.socialsoftware.mono2micro.source.dto;

import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;
import pt.ist.socialsoftware.mono2micro.source.domain.TranslationSource;

import java.util.ArrayList;
import java.util.List;

public class SourceDtoFactory {
    private static SourceDtoFactory factory = null;

    public static SourceDtoFactory getFactory() {
        if (factory == null)
            factory = new SourceDtoFactory();
        return factory;
    }

    public SourceDto getSourceDto(Source source) {
        if (source == null)
            return null;
        switch (source.getType()) {
            case AccessesSource.ACCESSES:
                return new AccessesSourceDto((AccessesSource) source);
            case TranslationSource.TRANSLATION:
                return new TranslationSourceDto((TranslationSource) source);
            default:
                throw new RuntimeException("The type \"" + source.getType() + "\" is not a valid source type.");
        }
    }

    public List<SourceDto> getSourceDtos(List<Source> sources) {
        if (sources == null)
            return null;
        List<SourceDto> sourceDtos = new ArrayList<>();
        for (Source source : sources)
            sourceDtos.add(getSourceDto(source));
        return sourceDtos;
    }
}
