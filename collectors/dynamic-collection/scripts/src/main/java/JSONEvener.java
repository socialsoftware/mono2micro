import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.*;
import dto.AccessDto;
import dto.ReducedTraceElementDto;
import dto.RuleDto;
import dto.TraceDto;
import org.apache.commons.cli.*;
import utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class JSONEvener {
	private static int traceCounter = 0;

	private static List<ReducedTraceElementDto> evenTraceElements(
		List<ReducedTraceElementDto> elements,
		int from,
		int to,
		Set<Short> entitiesToBeRemoved
	) {
		if (elements == null || elements.size() == 0) return elements;

		List<ReducedTraceElementDto> newElements = new ArrayList<>();

		int i = from;

		while (i < to) {
			ReducedTraceElementDto element = elements.get(i);

			if (element instanceof RuleDto) {
				RuleDto r = (RuleDto) element;

				int initialCount = r.getCount();

				List<ReducedTraceElementDto> newSequenceElements = evenTraceElements(
					elements,
					i + 1,
					i + 1 + r.getCount(),
					entitiesToBeRemoved
				);

				// doesnt hurt setting the count if the elements inside the sequence havent changed

				if (newSequenceElements.size() > 0) {
					r.setCount(newSequenceElements.size());

					newElements.add(r);
					newElements.addAll(newSequenceElements);
				}


				i += 1 + initialCount;

			} else {
				AccessDto a = (AccessDto) element;

				if (!entitiesToBeRemoved.contains(a.getEntityID()))
					newElements.add(a);

				i++;
			}

		}

		return newElements;
	}

	private static TraceDto evenTrace(TraceDto t, Set<Short> entitiesToBeRemoved) {
		t.setElements(
			evenTraceElements(
				t.getElements(),
				0,
				t.getElements() == null ? 0 : t.getElements().size(),
				entitiesToBeRemoved
			)
		);

		return t;
	}

	public static Options getCommandLineParserConfig() {
		Options options = new Options();

		return options
				.addRequiredOption(
					"i",
					"inputDir",
					true,
					"directory of the file to be evened"
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
			CommandLine cmdline = cmdLineparser.parse(getCommandLineParserConfig(), args);

			String inputFileDir = cmdline.getOptionValue("inputDir");
			String outputFileDir = cmdline.hasOption("outputDir")
				? cmdline.getOptionValue("outputDir")
				: "evened.json";

			File jsonFile = new File(inputFileDir);

			ObjectMapper mapper = new ObjectMapper();

			JsonFactory jsonfactory = mapper.getFactory();
			JsonGenerator jGenerator = jsonfactory.createGenerator(
				new FileOutputStream(outputFileDir),
				JsonEncoding.UTF8
			);

			Set<String> controllersToBeRemoved = new HashSet<String>() {
				{
				}
			};

			Set<Short> entitiesToBeRemoved = new HashSet<Short>(){
				{
					add((short) 39);
					add((short) 59);
				}
			};

//			jGenerator.useDefaultPrettyPrinter(); // for testing purposes
			jGenerator.writeStartObject();

			JsonParser jsonParser = jsonfactory.createParser(jsonFile);
			JsonToken jsonToken = jsonParser.nextValue(); // JsonToken.START_OBJECT

			if (jsonToken != JsonToken.START_OBJECT) {
				System.err.println("Json must start with a left curly brace");
				System.exit(-1);
			}

			while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
				if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
					Utils.print("Functionality name: " + jsonParser.getCurrentName(), Utils.lineno());

					if (controllersToBeRemoved.contains(jsonParser.getCurrentName()))
						jsonParser.skipChildren();

					else {
						jGenerator.writeObjectFieldStart(jsonParser.getCurrentName());

						while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
//							Utils.print("field name: " + jsonParser.getCurrentName(), Utils.lineno());

							switch (jsonParser.getCurrentName()) {
								case "f":
									jGenerator.writeNumberField(jsonParser.getCurrentName(), jsonParser.getIntValue());
									break;
								case "t":
									jGenerator.writeArrayFieldStart(jsonParser.getCurrentName());

									while (jsonParser.nextValue() != JsonToken.END_ARRAY) {
										TraceDto t = jsonParser.readValueAs(TraceDto.class);

										Utils.print("Evening Trace with id: " + t.getId(), Utils.lineno());

										jGenerator.writeObject(evenTrace(t, entitiesToBeRemoved));
										jGenerator.flush();

										traceCounter++;
									}

									jGenerator.writeEndArray();
									jGenerator.flush();

									break;

								default:
									Utils.print("Unexpected field name : " + jsonParser.getCurrentName(), Utils.lineno());
									System.exit(-1);
							}
						}

						jGenerator.writeEndObject();
					}
				}
			}

			jGenerator.writeEndObject();
			jGenerator.flush();

			jGenerator.close();

			Utils.print("#Evened traces: " + traceCounter, Utils.lineno());



		} catch(ParseException exp) {
			System.err.println( "Compaction failed.  Reason: " + exp.getMessage() );

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
