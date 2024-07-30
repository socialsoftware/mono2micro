package pt.ist.socialsoftware.cml.converter;

import org.contextmapper.discovery.ContextMapDiscoverer;
import org.contextmapper.discovery.ContextMapSerializer;
import org.contextmapper.discovery.model.ContextMap;
import pt.ist.socialsoftware.cml.converter.strategies.Mono2MicroBoundedContextDiscoveryStrategy;
import pt.ist.socialsoftware.cml.converter.strategies.Mono2MicroRelationshipDiscoveryStrategy;

import java.io.File;
import java.io.IOException;

public class Converter {

    private static final String DEFAULT_OUT_NAME = "m2m_decomposition";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            runDiscoverer("./src/test/resources/test-contract", DEFAULT_OUT_NAME, 0);
        } else if (args.length == 3) {
            runDiscoverer(args[0], args[1], Integer.parseInt(args[2]));
        } else {
            System.err.println("ERROR : Invalid program start arguments");
            System.exit(1);
        }
    }

    public static void runDiscoverer(String sourcesPath, String outputName, int namingMode) throws IOException {
        ContextMapDiscoverer discoverer = new ContextMapDiscoverer()
                .usingBoundedContextDiscoveryStrategies(
                        new Mono2MicroBoundedContextDiscoveryStrategy(new File(sourcesPath), namingMode)
                ).usingRelationshipDiscoveryStrategies(
                        new Mono2MicroRelationshipDiscoveryStrategy(new File(sourcesPath))
                );

        ContextMap contextmap = discoverer.discoverContextMap();
        new ContextMapSerializer().serializeContextMap(contextmap, new File("./out/" + outputName + ".cml"));
    }
}
