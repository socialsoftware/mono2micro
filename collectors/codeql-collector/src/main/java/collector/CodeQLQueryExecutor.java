package collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static collector.Constants.GENERATED_QUERIES_PATH;
import static collector.Constants.JSON_PATH;
import static collector.Constants.QUERY_COLLECTION_PATH;
import static collector.Constants.TEMPLATE_QUERIES_PATH;

public class CodeQLQueryExecutor {

    private Configuration config;

    public CodeQLQueryExecutor(Configuration config) {
        this.config = config;
    }

    public void runAndDecodeCommonQueries() throws IOException {
        // Create the output directory if it doesn't exist
        Files.createDirectories(Paths.get(JSON_PATH));

        String lang = config.getProperties().getLanguage();
        String framework = config.getProperties().getFramework();

        // Replace LANGUAGE_NAME placeholder with actual language
        generateCommonQueryFiles("qlpack.yml", "{LANGUAGE_NAME}", lang);

        // Replace FRAMEWORK_NAME placeholder with actual language
        generateCommonQueryFiles(".ql", "{FRAMEWORK_NAME}", framework);

        // Run all queries
        runQueriesInWithLibrary(QUERY_COLLECTION_PATH + GENERATED_QUERIES_PATH, config.getProperties().getLanguageLibraryPath());
    }

    public void runQueriesInWithLibrary(String queriesPath, String library) {
        if (queriesPath.isEmpty()) {
            return;
        }

        try (Stream<Path> stream = Files.list(Paths.get(queriesPath))) {
            stream.filter(file -> file.toString().endsWith(".ql"))
                .forEach(file -> runAndDecodeCodeQLQuery(file, library));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateCommonQueryFiles(String fileTerm, String matchSubstring, String replaceWith) {
        try (Stream<Path> stream = Files.list(Paths.get(QUERY_COLLECTION_PATH + TEMPLATE_QUERIES_PATH))) {
            stream.filter(file -> file.toString().endsWith(fileTerm))
                .forEach(filePath -> generateNewFileWith(filePath,
                    matchSubstring,
                    replaceWith
                ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateNewFileWith(Path filePath, String matchSubstring, String replacement) {
        try {
            // Read the original lines
            List<String> updatedLines = Files.readAllLines(filePath).stream()
                    .map(line -> line.contains(matchSubstring) ? line.replace(matchSubstring, replacement) : line)
                    .collect(Collectors.toList());

            // Resolve new output directory: one level up + /generated
            Path parentDir = filePath.getParent();
            Path outputDir = parentDir.getParent().resolve(GENERATED_QUERIES_PATH);

            // Ensure the output directory exists
            Files.createDirectories(outputDir);

            // Create the new output file path with the same file name
            Path outputFile = outputDir.resolve(filePath.getFileName());

            // Write the updated lines to the new file
            Files.write(outputFile, updatedLines);

        } catch (IOException e) {
            throw new RuntimeException("Failed to replace content in file: " + filePath, e);
        }
    }


    public void runAndDecodeCodeQLQuery(Path queryPath, String library) {
        try {
            // Construct the output file paths for the .bqrs and .json files
            String queryFileName = queryPath.getFileName().toString();
            String baseFileName = queryFileName.replace(".ql", "");
            Path bqrsOutputFile = Paths.get(JSON_PATH, baseFileName + ".bqrs");
            Path jsonOutputFile = Paths.get(JSON_PATH, baseFileName + ".json");

            // Run the CodeQL query to generate the .bqrs file
            ProcessBuilder queryProcessBuilder;
            if (library.isEmpty()) {
                queryProcessBuilder = new ProcessBuilder(
                    "codeql", "query", "run",
                    queryPath.toString(),
                    "--database", config.getCodeQLDbPath(),
                    "--output", bqrsOutputFile.toString()
                );
            } else {
                queryProcessBuilder = new ProcessBuilder(
                    "codeql", "query", "run",
                    queryPath.toString(),
                    "--database", config.getCodeQLDbPath(),
                    "--output", bqrsOutputFile.toString(),
                    "--additional-packs", library
                );
            }

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
