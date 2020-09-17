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
import java.util.List;

public class JSONRLESeqCompactor {
	private static int traceCounter = 0;

	public static CompactedTrace compactTraceConsecutiveEqualSequences(TraceWithAccesses trace) {
		final Sequitur seg = new Sequitur();
//		seg.addElements(trace.getAccesses());

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seg);
		runLengthEncodingSequitur.reduce();

		final List<ReducedTraceElement> compactedAccessesList = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace);

		return new CompactedTrace(trace.getId(), trace.getFrequency(), compactedAccessesList);
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
								CompactedTrace tws = compactTraceConsecutiveEqualSequences(t);
//								Utils.print("COMPACTED trace:\n" + tws, Utils.lineno());

								Utils.print("WRITING COMPACTED trace...", Utils.lineno());
								jGenerator.writeObject(tws);
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
