package collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static collector.Constants.JSON_PATH;

public class CodeQLQueryExecutor {

    private String codeQLDbPath;

    public CodeQLQueryExecutor(String codeQLDbPath) {
        this.codeQLDbPath = codeQLDbPath;
    }

    /**
     * Executes CodeQL query in queryPath and outputs to /json
     * @param queryPath path to query
     */
    public void runAndDecodeCodeQLQuery(Path queryPath) {
        try {
            // Construct the output file paths for the .bqrs and .json files
            String queryFileName = queryPath.getFileName().toString();
            String baseFileName = queryFileName.replace(".ql", "");
            Path bqrsOutputFile = Paths.get(JSON_PATH, baseFileName + ".bqrs");
            Path jsonOutputFile = Paths.get(JSON_PATH, baseFileName + ".json");

            // Run the CodeQL query to generate the .bqrs file
            ProcessBuilder queryProcessBuilder = new ProcessBuilder(
                    "codeql", "query", "run",
                    queryPath.toString(),
                    "--database", codeQLDbPath,
                    "--output", bqrsOutputFile.toString()
            );

            queryProcessBuilder.redirectErrorStream(true);
            Process queryProcess = queryProcessBuilder.start();

            // Print output from the query process
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(queryProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int queryExitCode = queryProcess.waitFor();
            if (queryExitCode != 0) {
                System.err.println("CodeQL query execution failed for " + queryPath + " with exit code: " + queryExitCode);
                return; // Skip decoding if query fails
            } else {
                System.out.println("CodeQL query executed successfully for " + queryPath);
            }

            // Decode the .bqrs file to JSON format
            ProcessBuilder decodeProcessBuilder = new ProcessBuilder(
                    "codeql", "bqrs", "decode",
                    bqrsOutputFile.toString(),
                    "--format", "json"
            );

            decodeProcessBuilder.redirectErrorStream(true);
            Process decodeProcess = decodeProcessBuilder.start();

            // Capture and save the JSON output
            StringBuilder jsonOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(decodeProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonOutput.append(line);
                }
            }

            int decodeExitCode = decodeProcess.waitFor();
            if (decodeExitCode != 0) {
                System.err.println("CodeQL BQRS decoding failed for " + bqrsOutputFile + " with exit code: " + decodeExitCode);
            } else {
                // Write the JSON output to the .json file
                Files.write(jsonOutputFile, jsonOutput.toString().getBytes());
                System.out.println("Decoded JSON written to " + jsonOutputFile);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error processing query: " + queryPath);
            e.printStackTrace();
        }
    }


}
