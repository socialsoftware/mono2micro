package pt.ist.socialsoftware.cml.converter.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.contextmapper.discovery.model.BoundedContext;
import org.contextmapper.discovery.model.Relationship;
import org.contextmapper.discovery.strategies.relationships.AbstractRelationshipDiscoveryStrategy;
import org.contextmapper.discovery.strategies.relationships.RelationshipDiscoveryStrategy;
import pt.ist.socialsoftware.cml.converter.domain.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Mono2MicroRelationshipDiscoveryStrategy extends AbstractRelationshipDiscoveryStrategy implements RelationshipDiscoveryStrategy {

    private File sourcePath;

    public Mono2MicroRelationshipDiscoveryStrategy(File sourcePath) {
        this.sourcePath = sourcePath;
    }

    @Override
    public Set<Relationship> discoverRelationships() {
        Set<Relationship> relationships = new HashSet<>();
        for (File m2mDecompositionFile : findM2MDecompositionFiles()) {
            relationships.addAll(discoverRelationships(m2mDecompositionFile));
        }
        return relationships;
    }

    private Set<Relationship> discoverRelationships(File m2mDecompositionFile) {
        Set<Relationship> relationships = new HashSet<>();
        Decomposition m2mDecomposition = parseM2MFile(m2mDecompositionFile);

        for (Functionality m2mFunctionality : m2mDecomposition.getFunctionalities()) {
            BoundedContext upstreamContext = discoverer.lookupBoundedContext(m2mFunctionality.getOrchestrator());
            for (FunctionalityStep m2mFunctionalityStep : m2mFunctionality.getSteps()) {
                BoundedContext downstreamContext = discoverer.lookupBoundedContext(m2mFunctionalityStep.getCluster());
                if (upstreamContext.equals(downstreamContext)) continue;
                Relationship relationship = new Relationship(upstreamContext, downstreamContext);
                relationships.add(relationship);
            }
        }

        return relationships;
    }

    protected Decomposition parseM2MFile(File m2mFile) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(m2mFile, Decomposition.class);

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("The file '" + m2mFile + "' does not exist!", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("The file '" + m2mFile + "' could not be parsed!", e);
        }
    }

    private Collection<File> findM2MDecompositionFiles() {
        return FileUtils.listFiles(sourcePath, new NameFileFilter("m2m_contract.json"), TrueFileFilter.INSTANCE);
    }
}
