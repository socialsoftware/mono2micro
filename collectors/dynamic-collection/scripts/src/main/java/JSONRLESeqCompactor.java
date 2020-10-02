import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.*;
import org.apache.commons.cli.*;
import requitur.ReducedTraceElement;
import requitur.RunLengthEncodingSequitur;
import requitur.Sequitur;
import utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class JSONRLESeqCompactor {
	private static int traceCounter = 0;

	public static CompactedTrace compactTrace(TraceWithAccesses trace) {
		final Sequitur seg = new Sequitur();

		final List<ReducedTraceElement> compactedAccessesList;
		List<Access> accesses = trace.getAccesses();

		if (accesses != null) {
			seg.addAccessElements(accesses);
			final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seg);
			runLengthEncodingSequitur.reduce();

			compactedAccessesList = runLengthEncodingSequitur.getReadableRLETrace();
//			 System.out.println("Compacted accesses: " + compactedAccessesList);

			return new CompactedTrace(
				trace.getId(),
				trace.getFrequency(),
				compactedAccessesList
			);
		}

		return new CompactedTrace(
			trace.getId(),
			trace.getFrequency(),
			new ArrayList<>()
		);

	}

	public static Options getCommandLineParserConfig() {
		Options options = new Options();

		return options
				.addRequiredOption(
					"i",
					"inputDir",
					true,
					"directory of the file to be compacted"
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
				: "compacted.json";

			File jsonFile = new File(inputFileDir);

			ObjectMapper mapper = new ObjectMapper();
			JsonFactory jsonfactory = mapper.getFactory();
			JsonGenerator jGenerator = jsonfactory.createGenerator(
				new FileOutputStream(outputFileDir),
				JsonEncoding.UTF8
			);

//			jGenerator.useDefaultPrettyPrinter(); // for testing purposes
			jGenerator.writeStartObject();

			JsonParser jsonParser = jsonfactory.createParser(jsonFile);
			JsonToken jsonToken = jsonParser.nextValue(); // JsonToken.START_OBJECT

			if (jsonToken != JsonToken.START_OBJECT) {
				System.err.println("Json must start with a left curly brace");
				System.exit(-1);
			}

			jsonParser.nextValue();

			while (jsonToken != JsonToken.END_OBJECT) {
				if (jsonToken == JsonToken.START_OBJECT) {
					Utils.print("Functionality name: " + jsonParser.getCurrentName(), Utils.lineno());
//					Utils.print("jsonToken: " + jsonToken, Utils.lineno());

					jGenerator.writeObjectFieldStart(jsonParser.getCurrentName());

					jsonToken = jsonParser.nextValue();

					while (jsonToken != JsonToken.END_OBJECT) {
//						Utils.print("field name: " + jsonParser.getCurrentName(), Utils.lineno());
//						Utils.print("jsonToken: " + jsonToken, Utils.lineno());

						if (jsonToken == JsonToken.VALUE_NUMBER_INT) {
							jGenerator.writeNumberField(jsonParser.getCurrentName(), jsonParser.getIntValue());
						}

						else if (jsonToken == JsonToken.START_ARRAY) {

							jGenerator.writeArrayFieldStart(jsonParser.getCurrentName());
							jsonParser.nextValue();
							while (jsonToken != JsonToken.END_ARRAY) {
								TraceWithAccesses t = jsonParser.readValueAs(TraceWithAccesses.class);

								Utils.print("COMPACTING Trace with id: " + t.getId(), Utils.lineno());
								CompactedTrace compactedTrace = compactTrace(t);
//								Utils.print("COMPACTED trace:\n" + compactedTrace, Utils.lineno());

								Utils.print("WRITING COMPACTED trace...", Utils.lineno());
								jGenerator.writeObject(compactedTrace);
								jGenerator.flush();

								Utils.print("#Compacted traces: " + traceCounter, Utils.lineno());
								traceCounter++;

								jsonToken = jsonParser.nextValue();
//								Utils.print("field name: " + jsonParser.getCurrentName(), Utils.lineno());
//								Utils.print("jsonToken: " + jsonToken, Utils.lineno());
							}

							jGenerator.writeEndArray();
						}

						else {
							Utils.print("Unexpected json token: " + jsonToken, Utils.lineno());
							System.exit(-1);
						}

						jsonToken = jsonParser.nextValue();
					}

					jGenerator.writeEndObject();
				}

				jsonToken = jsonParser.nextValue();
			}

			jGenerator.writeEndObject();
			jGenerator.close();


		} catch(ParseException exp) {
			System.err.println( "Compaction failed.  Reason: " + exp.getMessage() );

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
