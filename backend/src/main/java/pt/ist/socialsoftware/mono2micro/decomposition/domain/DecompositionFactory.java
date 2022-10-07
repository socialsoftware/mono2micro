package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import pt.ist.socialsoftware.mono2micro.strategy.domain.AccAndRepoSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.RepositorySciPyStrategy;

public class DecompositionFactory {
    private static DecompositionFactory factory = null;

    public static DecompositionFactory getFactory() {
        if (factory == null)
            factory = new DecompositionFactory();
        return factory;
    }

    public Decomposition getDecomposition(String type) {
        switch (type) {
            case AccessesSciPyStrategy.ACCESSES_SCIPY:
                return new AccessesSciPy();
            case RepositorySciPyStrategy.REPOSITORY_SCIPY:
                return new RepositorySciPy();
            case AccAndRepoSciPyStrategy.ACC_AND_REPO_SCIPY:
                return new AccAndRepoSciPy();
            default:
                throw new RuntimeException("The type \"" + type + "\" is not a valid strategy type for the decomposition.");
        }
    }
}
