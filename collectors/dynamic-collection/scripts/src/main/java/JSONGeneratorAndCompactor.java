import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import domain.*;
import org.apache.commons.cli.*;
import requitur.RunLengthEncodingSequitur;
import requitur.Sequitur;
import requitur.TraceStateTester;
import utils.Utils;

import java.io.*;
import java.util.*;

public class JSONGeneratorAndCompactor {
    static HashMap<String, Functionality> json = new HashMap<>();
    static HashMap<String, List<Access>> cache = new HashMap<>();
    static HashMap<String, Short> domainEntities;

    static CompactedTrace currentTrace = null;
    static Sequitur sequitur = null;
    final static RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur();

    static int currentKiekerTraceID = -1;
    static String currentFunctionalityLabel = "";

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
            if (currentTrace != null) {
                if (!sequitur.isEmpty()) {
                    runLengthEncodingSequitur.setSequitur(sequitur);
                    runLengthEncodingSequitur.reduce();

                    currentTrace.setElements(runLengthEncodingSequitur.getReadableRLETrace());
                } else {
                    currentTrace.setElements(new ArrayList<>());
                }

                json.get(currentFunctionalityLabel).addTrace(currentTrace);

                currentTrace = null;
            }

//            Utils.print("log: " + Arrays.toString(log), Utils.lineno());
            currentKiekerTraceID = traceID;
            currentFunctionalityLabel = declaringType + "." + methodName;
            Utils.print("Functionality: " + currentFunctionalityLabel, Utils.lineno());

            Utils.print("Trace: " + traceID, Utils.lineno());

            if (!json.containsKey(currentFunctionalityLabel)) {
                json.put(currentFunctionalityLabel, new Functionality(currentFunctionalityLabel));

                currentTrace = new CompactedTrace(
                    0,
                    1
                );

            } else {
                currentTrace = new CompactedTrace(
                    json.get(currentFunctionalityLabel).getFrequency(),
                    1
                );
            }

            sequitur = new Sequitur();
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

                List<Access> cachedAccessesList = cache.get(splitTrace[3]);
                if (cachedAccessesList != null) {
                    sequitur.addAccessElements(cachedAccessesList);
                    return;
                }

    //            call_order, stackDepth = getMethodCallOrderAndStackDepth(split_trace)
                List<Access> accesses = Utils.getMethodEntityAccesses(
                    declaringType,
                    methodName,
                    callerDynamicType,
                    argumentTypes,
                    dynamicReturnType,
                    domainEntities
                );

//                Utils.print("Accesses: " + accesses.toString(), Utils.lineno());

                cache.put(splitTrace[3], accesses);
                sequitur.addAccessElements(accesses);

                return;
            }

            Utils.print("[ERROR]: Unknown declaring type: " + declaringType, Utils.lineno());
            System.exit(-1);
        }
    }

    public static void deleteFunctionalitiesWithNoAccesses() {
        Iterator<Map.Entry<String, Functionality>> it = json.entrySet().iterator();
        Map.Entry<String, Functionality> kvp;

        while (it.hasNext()) {
            kvp = it.next();

            if (kvp.getValue().getTraces().size() < 1)
                it.remove();
        }
    }

    public static void generateStatistics(String dir) throws IOException {
        Functionality functionalityWithHighestFrequency;
        Functionality functionalityWithMoreDifferentTraces;
        Functionality functionalityWithTheLongestTrace;
        int totalAmountOfProcessedTraces = 0;
        int totalAmountOfUniqueProcessedTraces = 0;

        Iterator<Map.Entry<String, Functionality>> it = json.entrySet().iterator();

        if (it.hasNext()) {
            Map.Entry<String, Functionality> kvp = it.next();
            functionalityWithHighestFrequency = kvp.getValue();
            functionalityWithMoreDifferentTraces = kvp.getValue();
            functionalityWithTheLongestTrace = kvp.getValue();
            totalAmountOfProcessedTraces += kvp.getValue().getFrequency();
            totalAmountOfUniqueProcessedTraces += kvp.getValue().getTraces().size();

            int accessesListMaximumLength = 0;
            int longestTraceID = -1;

            for (Trace t : functionalityWithTheLongestTrace.getTraces()) {
                int traceAccessesListSize = ((CompactedTrace) t).getUncompressedSize();

                if (traceAccessesListSize > accessesListMaximumLength) {
                    accessesListMaximumLength = traceAccessesListSize;
                    longestTraceID = t.getId();
                }
            }

            while (it.hasNext()) {
                kvp = it.next();

                totalAmountOfProcessedTraces += kvp.getValue().getFrequency();
                totalAmountOfUniqueProcessedTraces += kvp.getValue().getTraces().size();

                if (kvp.getValue().getFrequency() > functionalityWithHighestFrequency.getFrequency()) {
                    functionalityWithHighestFrequency = kvp.getValue();
                }

                if (kvp.getValue().getTraces().size() > functionalityWithMoreDifferentTraces.getTraces().size()) {
                    functionalityWithMoreDifferentTraces = kvp.getValue();
                }

                for (Trace t : kvp.getValue().getTraces()) {
                    int traceAccessesListSize = ((CompactedTrace) t).getUncompressedSize();

                    if (traceAccessesListSize > accessesListMaximumLength) {
                        functionalityWithTheLongestTrace = kvp.getValue();
                        accessesListMaximumLength = traceAccessesListSize;
                        longestTraceID = t.getId();
                    }
                }
            }

            String str = "Processed " + json.keySet().size() + " different functionalities" +
                '\n' +
                "Processed a total of " + totalAmountOfProcessedTraces + " traces" +
                '\n' +
                "Processed a total of " + totalAmountOfUniqueProcessedTraces + " unique traces" +
                '\n' +
                "Functionality with highest frequency: " + functionalityWithHighestFrequency.getLabel() + " (executed " + functionalityWithHighestFrequency.getFrequency() + " times)" +
                '\n' +
                "Functionality with more different traces: " + functionalityWithMoreDifferentTraces.getLabel() + " (" + functionalityWithMoreDifferentTraces.getTraces().size() + " different traces)" +
                '\n' +
                "Functionality with longest trace: " + functionalityWithTheLongestTrace.getLabel() + " (traceId: " + longestTraceID + ") (#accesses: " + accessesListMaximumLength + ")";

            System.out.println(str);

            BufferedWriter writer = new BufferedWriter(new FileWriter(dir + "resume.txt"));
            writer.write(str);
            writer.close();

            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(dir + "funcWithHighestFrequency.json"), functionalityWithHighestFrequency);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(dir + "funcWithMoreDifferentTraces.json"), functionalityWithMoreDifferentTraces);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(dir + "funcWithTheLongestTrace.json"), functionalityWithTheLongestTrace);
        }
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

            String domainEntitiesFileDir = cmdline.getOptionValue("domainEntities");

            domainEntities = mapper.readValue(new FileInputStream(domainEntitiesFileDir), new TypeReference<HashMap<String, Short>>(){});

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
            }


            if (currentTrace != null) {
                if (!sequitur.isEmpty()) {
                    runLengthEncodingSequitur.setSequitur(sequitur);
                    runLengthEncodingSequitur.reduce();
                    currentTrace.setElements(runLengthEncodingSequitur.getReadableRLETrace());
                } else {
                    currentTrace.setElements(new ArrayList<>());
                }

                json.get(currentFunctionalityLabel).addTrace(currentTrace);
            }

            cache.clear();

            Utils.print("Processing phase has ended.", Utils.lineno());

            if (cmdline.hasOption("stats")) {
                File directory = new File(cmdline.getOptionValue("stats"));
                if (!directory.exists()){
                    directory.mkdir();
                }

                mapper.setFilterProvider(new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAll()));
                Utils.print("Generating statistics into folder " + cmdline.getOptionValue("stats"), Utils.lineno());
                generateStatistics(cmdline.getOptionValue("stats"));
            }

//            Utils.print("Deleting functionalities with no accesses...", Utils.lineno());
//            deleteFunctionalitiesWithNoAccesses();

            Utils.print("Writing JSON to file " + outputFileDir, Utils.lineno());

//            DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
//            pp.indentArraysWith( DefaultIndenter.SYSTEM_LINEFEED_INSTANCE );
//            ObjectWriter writer = mapper.writer(pp);

            mapper.writeValue(new FileOutputStream(outputFileDir), json);

            Utils.print("Writing phase has ended.", Utils.lineno());

        } catch(ParseException exp) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    
}
