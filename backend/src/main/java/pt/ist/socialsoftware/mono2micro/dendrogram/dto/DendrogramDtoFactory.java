package pt.ist.socialsoftware.mono2micro.dendrogram.dto;

import pt.ist.socialsoftware.mono2micro.dendrogram.domain.AccessesSciPyDendrogram;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;

import java.util.ArrayList;
import java.util.List;

public class DendrogramDtoFactory {
    private static DendrogramDtoFactory factory = null;

    public static DendrogramDtoFactory getFactory() {
        if (factory == null)
            factory = new DendrogramDtoFactory();
        return factory;
    }

    public DendrogramDto getDendrogramDto(Dendrogram dendrogram) {
        if (dendrogram == null)
            return null;
        switch (dendrogram.getType()) {
            case AccessesSciPyStrategy.ACCESSES_SCIPY:
                return new AccessesSciPyDendrogramDto((AccessesSciPyDendrogram) dendrogram);
            default:
                throw new RuntimeException("The type \"" + dendrogram.getType() + "\" is not a valid dendrogram type.");
        }
    }

    public List<DendrogramDto> getDendrogramDtos(List<Dendrogram> dendrograms) {
        if (dendrograms == null)
            return null;
        List<DendrogramDto> dendrogramDtos = new ArrayList<>();
        for (Dendrogram dendrogram : dendrograms)
            dendrogramDtos.add(getDendrogramDto(dendrogram));
        return dendrogramDtos;
    }
}
