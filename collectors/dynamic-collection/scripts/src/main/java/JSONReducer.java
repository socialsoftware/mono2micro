import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Trace;
import org.apache.commons.cli.*;
import domain.Functionality;
import domain.TraceWithAccesses;
import utils.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JSONReducer {
	static HashMap<String, Functionality> json;

	public static void reduceExecutionTraces() {
		for (HashMap.Entry<String, Functionality> entry : json.entrySet()) {

			System.out.println("Functionality: " + entry.getKey());
			Functionality f = entry.getValue();

			List<Trace> functionalityTraces = f.getTraces();

			boolean[] representativeTraces = new boolean[functionalityTraces.size()];
			Arrays.fill(representativeTraces, true);

			if (functionalityTraces.size() > 1) {
				for (int i = 0; i < functionalityTraces.size(); i++) {
					for (int j = i + 1; j < functionalityTraces.size(); j++) {
						if (representativeTraces[i] || representativeTraces[j]) {
							TraceWithAccesses currentTrace = (TraceWithAccesses) functionalityTraces.get(i);
							TraceWithAccesses otherTrace = (TraceWithAccesses) functionalityTraces.get(j);

							if (currentTrace.getAccesses() == null) {
								representativeTraces[i] = false;

							} else {
								if (representativeTraces[i] && currentTrace.isSubsequence(otherTrace)) {
									Utils.print(currentTrace + " is a subsequence of\n" + otherTrace, Utils.lineno());
									representativeTraces[i] = false;
										continue;
								}

								if (representativeTraces[j] && otherTrace.isSubsequence(currentTrace)) {
									Utils.print(otherTrace + " is a subsequence of\n" + currentTrace, Utils.lineno());
									representativeTraces[j] = false;
								}
							}
						}
					}
				}
				// delete traces that are not representative (backwards deletion)
				for (int i = functionalityTraces.size() - 1; i >= 0; i--) {
					if (!representativeTraces[i]) {
						Utils.print("Trace " + functionalityTraces.get(i).getId() + " is NOT representative", Utils.lineno());
						functionalityTraces.remove(i);
					} else {
						Utils.print("Trace " + functionalityTraces.get(i).getId() + " is representative", Utils.lineno());
					}
				}
			}

		}
	}

	public static Options getCommandLineParserConfig() {
		Options options = new Options();

		return options
			.addRequiredOption(
				"i",
				"inputDir",
				true,
				"directory of the file to be reduced"
			)
			.addOption(
				"o",
				"outputDir",
				true,
				"directory of the generated file"
			);
	}

	public static void main( String[] args ) {
		try {
			CommandLineParser cmdLineparser = new DefaultParser();
			CommandLine cmdline = cmdLineparser.parse(getCommandLineParserConfig(), args );

			String inputFileDir = cmdline.getOptionValue("inputDir");
			String outputFileDir = cmdline.hasOption("outputDir")
				? cmdline.getOptionValue("outputDir")
				: "reduced.json";

			ObjectMapper mapper = new ObjectMapper();
			json = mapper.readValue(new FileInputStream(inputFileDir), new TypeReference<HashMap<String, Functionality>>(){});

			reduceExecutionTraces();

			Utils.print("Reduction phase has ended.\n Writing JSON to file...", Utils.lineno());
			mapper.writeValue(new FileOutputStream(outputFileDir), json);

			Utils.print("Writing phase has ended.", Utils.lineno());

		} catch(ParseException exp) {
			Utils.print("Reduction failed.  Reason: " + exp.getMessage(), Utils.lineno());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
