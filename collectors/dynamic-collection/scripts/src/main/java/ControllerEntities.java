import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import domain.Access;
import org.apache.commons.cli.*;
import utils.*;


import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerEntities {
//    static HashMap<String, List<Access>> cache = new HashMap<>();
    static HashMap<String, Boolean> domainEntities;

    static HashMap<String, HashMap<String, String>> functionalitiesAccesses = new HashMap<>(); // <controllerName, <entityName, accesses>>
    static int currentKiekerTraceID = -1;
    static String currentFunctionalityLabel = "";

    static HashMap<String, Integer> functionalityToFrequency = new HashMap<>(); // <functionaliyName, freq>
    static HashMap<String, Integer> functionalityToLengthOfLongestTrace = new HashMap<>(); // <functionaliyName, length of longest trace>
    static int totalAmountOfProcessedTraces = 0;
    static int currentTraceLength = 0;
    static boolean processingTrace = false;

    static ObjectMapper mapper = new ObjectMapper();

    static FilenameFilter filenameFilter = (f, name) -> name.startsWith("executionTraces");

    public static void parseExecutionTrace(String trace) throws JsonProcessingException {
        String[] splitTrace = trace
            .replace("<", "")
            .replace(">", "")
            .split("\\s+", -1);

        if (splitTrace.length != 4) {
            Utils.print("[ERROR]: Incompatible trace: (+/- 4 fields) was found: " + Arrays.toString(splitTrace), Utils.lineno());
            System.exit(-1);
        }

        int traceID = Integer.parseInt(Utils.getTraceID(splitTrace));

        String[] log = splitTrace[3].split(":", -1);

        if (log.length != 5) {
            Utils.print("[ERROR]: A log with more than 5 fields was found: " + Arrays.toString(log), Utils.lineno());
            System.exit(-1);
        }

        String declaringType = log[0].isEmpty() ? null : log[0];
        String methodName = log[1].isEmpty() ? null : log[1];

        if (!(currentKiekerTraceID == traceID)) {
            if (processingTrace) {
                if (functionalityToLengthOfLongestTrace.get(currentFunctionalityLabel) < currentTraceLength) {
                    functionalityToLengthOfLongestTrace.put(
                        currentFunctionalityLabel,
                        currentTraceLength
                    );
                }

                currentTraceLength = 0;
            }

            totalAmountOfProcessedTraces++;
            processingTrace = true;
            // Utils.print("log: " + Arrays.toString(log), Utils.lineno());
            currentKiekerTraceID = traceID;
            currentFunctionalityLabel = declaringType + "." + methodName;
            Utils.print("Functionality: " + currentFunctionalityLabel + '\t' + "TraceID: " + traceID, Utils.lineno());

            if (!functionalitiesAccesses.containsKey(currentFunctionalityLabel)) {
                functionalitiesAccesses.put(currentFunctionalityLabel, new HashMap<>());
                functionalityToFrequency.put(currentFunctionalityLabel, 1);
                functionalityToLengthOfLongestTrace.put(currentFunctionalityLabel, 0);
            }
            else {
                functionalityToFrequency.put(
                    currentFunctionalityLabel,
                    functionalityToFrequency.get(currentFunctionalityLabel) + 1
                );
            }

        }

        else {
            if (
                (declaringType != null && methodName != null) && (
                    declaringType.contains("FenixFramework") ||
                    declaringType.contains("_Base") ||
                    declaringType.contains("AbstractDomainObject") ||
                    declaringType.contains("OneBoxDomainObject")
                )
            ) {

                String callerDynamicType = log[2].isEmpty() ? null : log[2];
                String[] argumentTypes = log[3].isEmpty() ? null : mapper.readValue(log[3], String[].class);
                String dynamicReturnType = log[4].isEmpty() ? null : log[4];

                List<Access> accesses = Utils.getMethodEntityAccesses(
                    declaringType,
                    methodName,
                    callerDynamicType,
                    argumentTypes,
                    dynamicReturnType,
                    domainEntities
                );

                currentTraceLength += accesses.size();

//                Utils.print("Accesses: " + accesses.toString(), Utils.lineno());

                HashMap<String, String> functionalityAccesses = functionalitiesAccesses.get(currentFunctionalityLabel);

                for (Access a : accesses) {
                    String accessedEntity = a.getEntity();
                    String accessMode = a.getType().name();

                    String savedEntityMode = functionalityAccesses.get(accessedEntity);

                    if (savedEntityMode != null) {
                        if (!savedEntityMode.equals(accessMode))
                            functionalityAccesses.put(accessedEntity, "RW");

                    } else {
                        functionalityAccesses.put(accessedEntity, accessMode);
                    }

                }

                return;
            }

            Utils.print("[ERROR]: Unknown declaring type: " + declaringType, Utils.lineno());
            System.exit(-1);

        }
    }

    public static void generateStatistics(String dir, String filename) throws IOException {
        String functionalityWithHighestFrequency = "NO_FUNCTIONALITY";
        int highestFunctionalityFrequency = 0;
        String functionalityWithTheLongestTrace = "NO_FUNCTIONALITY";
        int longestFunctionalityTraceLength = 0;

        for (Map.Entry<String, Integer> functionalityEntry : functionalityToFrequency.entrySet()) {
            String functionalityName = functionalityEntry.getKey();
            Integer frequency = functionalityEntry.getValue();
            Integer lengthOfLongestTrace = functionalityToLengthOfLongestTrace.get(functionalityName);

            if (frequency > highestFunctionalityFrequency) {
                highestFunctionalityFrequency = frequency;
                functionalityWithHighestFrequency = functionalityName;
            }

            if (lengthOfLongestTrace > longestFunctionalityTraceLength) {
                longestFunctionalityTraceLength = lengthOfLongestTrace;
                functionalityWithTheLongestTrace = functionalityName;
            }

        }

        String str = "Statistics about file: " + filename +
            '\n' +
            "Processed " + functionalitiesAccesses.keySet().size() + " different functionalities" +
            '\n' +
            "Processed a total of " + totalAmountOfProcessedTraces + " traces" +
            '\n' +
            "Functionality with highest frequency: " + functionalityWithHighestFrequency + " (executed " + highestFunctionalityFrequency + " times)" +
            '\n' +
            "Functionality with longest trace: " + functionalityWithTheLongestTrace + " (#accesses: " + longestFunctionalityTraceLength + ")";

        System.out.println(str);

        BufferedWriter writer = new BufferedWriter(new FileWriter(dir + "resumeControllerEntities.txt"));
        writer.write(str);
        writer.close();
    }

    public static Options getCommandLineParserConfig() {
        // create Options object
        Options options = new Options();

        options.addRequiredOption(
            "i",
            "inputDir",
            true,
            "directory of the file to be parsed"
        );
        options.addOption(
            "o",
            "outputDir",
            true,
            "directory of the generated file"
        );
        options.addRequiredOption(
            "de",
            "domainEntities",
            true,
            "directory of the json file containing all domain entities"
        );

        options.addOption(
            "s",
            "stats",
            true,
            "if present, statistics are displayed after the processing phase"
        );


        return options;
    }

    public static void main( String[] args ) {
        try {
            CommandLineParser cmdLineparser = new DefaultParser();

            CommandLine cmdline = cmdLineparser.parse(getCommandLineParserConfig(), args );

            String inputFilesDir = cmdline.getOptionValue("inputDir");
            String outputFileDir = cmdline.hasOption("outputDir")
                ? cmdline.getOptionValue("outputDir")
                : "parsed.json";

            if (cmdline.hasOption("stats")) {
                File directory = new File(cmdline.getOptionValue("stats"));
                if (!directory.exists()){
                    directory.mkdir();
                }
            }

            String domainEntitiesFileDir = cmdline.getOptionValue("domainEntities");

            domainEntities = mapper.readValue(new FileInputStream(domainEntitiesFileDir), new TypeReference<HashMap<String, Boolean>>(){});

            // Read all files starting with "executionTraces..."
            File f = new File(inputFilesDir);

            // Note that this time we are using a File class as an array,
            // instead of String
            File[] files = f.listFiles(filenameFilter);
            for (File file : files) {
                Utils.print("Reading file: " + file.getName(), Utils.lineno());

                BufferedReader lineReader = new BufferedReader(new FileReader(inputFilesDir + file.getName()));
                String line;

                while ((line = lineReader.readLine()) != null) {
                    if (line.length() < 3 || line.contains("TraceId"))
                        continue;

                    parseExecutionTrace(line);
                }

                lineReader.close();

                if (functionalityToLengthOfLongestTrace.get(currentFunctionalityLabel) < currentTraceLength) {
                    functionalityToLengthOfLongestTrace.put(
                        currentFunctionalityLabel,
                        currentTraceLength
                    );
                }

                if (cmdline.hasOption("stats")) {
                    Utils.print("Generating statistics into folder " + cmdline.getOptionValue("stats"), Utils.lineno());
                    generateStatistics(cmdline.getOptionValue("stats"), file.getName());
                }

                Utils.print("Writing JSON to file " + outputFileDir, Utils.lineno());

                mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
                mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(outputFileDir), functionalitiesAccesses);

                Utils.print("Writing phase has ended.", Utils.lineno());
            }

        } catch(ParseException exp) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    
}
