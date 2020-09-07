import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.cli.*;
import utils.*;

import java.io.*;
import java.util.*;

public class ControllerEntitiesJSONMerger {


    public static Options getCommandLineParserConfig() {
        // create Options object
        Options options = new Options();

        options.addRequiredOption(
            "j1",
            "json1",
            true,
            "first json file"
        );
        options.addRequiredOption(
            "j2",
            "json2",
            true,
            "second json file"
        );
        options.addOption(
            "o",
            "outputDir",
            true,
            "final json"
        );

        return options;
    }

    public static void main( String[] args ) {
        try {
            CommandLineParser cmdLineparser = new DefaultParser();

            CommandLine cmdline = cmdLineparser.parse(getCommandLineParserConfig(), args );

            String json1Dir = cmdline.getOptionValue("json1");
            String json2Dir = cmdline.getOptionValue("json2");
            String outputFileDir = cmdline.hasOption("outputDir")
                ? cmdline.getOptionValue("outputDir")
                : "mergedControllerEntities.json";

            HashMap<String, HashMap<String, String>> JSON2functionalitiesAccesses; // <controllerName, <entityName, accesses>>
            HashMap<String, HashMap<String, String>> finalJSONFunctionalitiesAccesses; // <controllerName, <entityName, accesses>>

            ObjectMapper mapper = new ObjectMapper();
            finalJSONFunctionalitiesAccesses = mapper.readValue(new FileInputStream(json1Dir), new TypeReference<HashMap<String, HashMap<String, String>>>(){});
            JSON2functionalitiesAccesses = mapper.readValue(new FileInputStream(json2Dir), new TypeReference<HashMap<String, HashMap<String, String>>>(){});

            for (Map.Entry<String, HashMap<String, String>> kvp1 : JSON2functionalitiesAccesses.entrySet()) {
                String JSON2functionalityName = kvp1.getKey();
                HashMap<String, String> JSON2functionalityAccesses = kvp1.getValue();

                HashMap<String, String> finalFunctionalityAccesses = finalJSONFunctionalitiesAccesses.get(JSON2functionalityName);

                if (finalFunctionalityAccesses == null) {
                    finalJSONFunctionalitiesAccesses.put(JSON2functionalityName, JSON2functionalityAccesses);
                }

                else {
                    for (Map.Entry<String, String> kvp2 : JSON2functionalityAccesses.entrySet()) {
                        String JSON2AccessedEntity = kvp2.getKey();
                        String JSON2AccessMode = kvp2.getValue();

                        String finalMode = finalFunctionalityAccesses.get(JSON2AccessedEntity);

                        if (finalMode != null) {
                            if (!finalMode.equals(JSON2AccessMode))
                                finalFunctionalityAccesses.put(JSON2AccessedEntity, "RW");

                        } else {
                            finalFunctionalityAccesses.put(JSON2AccessedEntity, JSON2AccessMode);
                        }
                    }
                }
            }

            Utils.print("Writing JSON to file " + outputFileDir, Utils.lineno());

            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(outputFileDir), finalJSONFunctionalitiesAccesses);

            Utils.print("Writing phase has ended.", Utils.lineno());

            System.out.println("Processed " + finalJSONFunctionalitiesAccesses.keySet().size() + " different functionalities");

        } catch(ParseException exp) {
            System.err.println( "Something went wrong.  Reason: " + exp.getMessage() );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    
}
