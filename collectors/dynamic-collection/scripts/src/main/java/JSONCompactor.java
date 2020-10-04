import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import domain.*;
import utils.*;

import java.io.*;
import java.util.*;

public class JSONCompactor {
	private static int traceCounter = 0;

	public static List<AccessWithFrequency> compactConsecutiveEqualAccessesWithFrequency(
		List<AccessWithFrequency> accesses
	) {
		if (accesses.size() > 1) {
			ArrayList<AccessWithFrequency> compactedAccesses = new ArrayList<>();
			compactedAccesses.add(accesses.get(0));

			for (int i = 1; i < accesses.size(); i++) {
				AccessWithFrequency currentAccess = accesses.get(i);
				AccessWithFrequency lastCompactedAccess = compactedAccesses.get(compactedAccesses.size() - 1);

//				System.out.println("Current access: " + currentAccess);
//				System.out.println("Last compacted: " + lastCompactedAccess);

				if (currentAccess.equals(lastCompactedAccess)) {
					lastCompactedAccess.setFrequency(lastCompactedAccess.getFrequency() + currentAccess.getFrequency());
//					System.out.println("Accesses are equal! Increasing frequency!");
				} else {
//					System.out.println("Different accesses!");
					compactedAccesses.add(currentAccess);
				}
			}

			if (compactedAccesses.size() < accesses.size()) {
				accesses = compactedAccesses;
//				System.out.println("Trace got compacted: " + trace.getAccesses());
			}

		}

		return accesses;
	}

	public static TraceWithSequences compactTraceConsecutiveEqualSequences(TraceWithAccesses trace) {
		if (trace.getAccesses() == null)
			return new TraceWithSequences(trace, new ArrayList<>());

		Utils.print("Trace size: " + trace.getAccesses().size() + " acc(s)", Utils.lineno());

		if (trace.getAccesses().size() == 1) {
			List<Sequence<AccessWithFrequency>> traceSequences = new ArrayList<>();
			Sequence<AccessWithFrequency> seq = new Sequence<>();

			seq.addSingleAccess(new AccessWithFrequency(trace.getAccesses().get(0), 1));
			traceSequences.add(seq);

			return new TraceWithSequences(trace, traceSequences);
		}

		List<Sequence<Access>> traceSequences = new ArrayList<>();

		if (trace.getAccesses().size() > 1499999) {
			Utils.print("------------------ SKIPPING SEQUENCES GENERATION --------------- ", Utils.lineno());
			traceSequences.add(new Sequence<>(trace.getAccesses(), 1));

		} else {
			Utils.print("------------------ GENERATING SEQUENCES --------------- ", Utils.lineno());

			int pivot = 0;
			int limit = 0;
			int i = 1;
			Sequence<Access> currentSequence = new Sequence<>();
			Sequence<Access> bestSequence = new Sequence<>(); // the longest one for a certain pivot

			List<Access> accessesToMatch;
			int totalNumberOfAccesses = trace.getAccesses().size();
			int lastAccessIndex = totalNumberOfAccesses - 1;

			while (true) {
	//			Utils.print("Pivot: " + pivot + " | Limit: " + limit + " | i: " + i, Utils.lineno());

				if (pivot > lastAccessIndex || limit > lastAccessIndex) {
	//				Utils.print("Pivot or limit are out of bounds", Utils.lineno());
					break;
				}

					accessesToMatch = trace.getAccesses().subList(pivot, limit + 1);
		//			Utils.print("Accesses to match: " + accessesToMatch, Utils.lineno());

				// it's unnecessary to match something if the remaining values aren't at least of the same size
				// consequently, pivot must change

					if (accessesToMatch.size() > (lastAccessIndex - limit)) { // optimization
	//				Utils.print("Remaining elements are insufficient after limit", Utils.lineno());

	//				Utils.print("Best sequence: " + bestSequence, Utils.lineno());
					if (bestSequence.getAccesses().size() > 0) { // if previously we had a matching sequence then we use it
	//					Utils.print("Adding best sequence: " + bestSequence, Utils.lineno());

						traceSequences.add(bestSequence);
						pivot += bestSequence.getAccesses().size() * bestSequence.getFrequency(); // we must increase the pivot as many times as the matched sequence

						bestSequence = new Sequence<>();

					} else {
	//					Utils.print("Adding pivot access", Utils.lineno());

						Sequence<Access> seq = new Sequence<>();
						seq.addSingleAccess(trace.getAccesses().get(pivot));
						traceSequences.add(seq);
						pivot++;
					}

	//				Utils.print("New trace sequences: " + traceSequences, Utils.lineno());

					limit = pivot;

					i = limit + 1;
					continue;
				}

				// if we can't match more sequences because there isn't enough elements to match then we can increase the limit
					if (accessesToMatch.size() > (totalNumberOfAccesses - i)) {
	//				Utils.print("Not enough elements to match after i", Utils.lineno());
					if (currentSequence.getAccesses().size() > 0) {
						// check if currentSequence is longer than best sequence
						int bestSequenceLength = bestSequence.getAccesses().size() * bestSequence.getFrequency();
						int currentSequenceLength = currentSequence.getAccesses().size() * currentSequence.getFrequency();

	//					Utils.print("Best sequence: " + bestSequence, Utils.lineno());
	//					Utils.print("Current sequence: " + currentSequence, Utils.lineno());

						if (currentSequenceLength > bestSequenceLength) {
							bestSequence.setAccesses(new ArrayList<>(currentSequence.getAccesses()));
							bestSequence.setFrequency(currentSequence.getFrequency());

	//						Utils.print("Current sequence is longer than best sequence", Utils.lineno());
	//						Utils.print("New best sequence: " + bestSequence, Utils.lineno());

						} else if (bestSequenceLength > 0 && currentSequenceLength == bestSequenceLength) {
							if (currentSequence.getAccesses().size() < bestSequence.getAccesses().size()) {
								bestSequence.setAccesses(new ArrayList<>(currentSequence.getAccesses()));
								bestSequence.setFrequency(currentSequence.getFrequency());

	//							Utils.print("Both sequences have the same size but current sequence has less accesses", Utils.lineno());
							}
						}

						currentSequence.reset();
					}

					limit++;
					i = limit + 1;
					continue;
				}


				List<Access> currentAccesses = trace.getAccesses().subList(
						i, i + accessesToMatch.size()
				);

				if (accessesToMatch.equals(currentAccesses)) {
	//				Utils.print("Accesses are equal", Utils.lineno());

					if (currentSequence.getAccesses().size() == 0) {
	//					Utils.print("First sequence found", Utils.lineno());
						currentSequence.addMultipleAccesses(currentAccesses);
					}

					currentSequence.increaseFrequency();

					i += accessesToMatch.size();

				} else {
					if (currentSequence.getAccesses().size() > 0) {
						// check if currentSequence is better than best sequence
						int bestSequenceLength = bestSequence.getAccesses().size() * bestSequence.getFrequency();
						int currentSequenceLength = currentSequence.getAccesses().size() * currentSequence.getFrequency();

	//					Utils.print("Best sequence: " + bestSequence, Utils.lineno());
	//					Utils.print("Current sequence: " + currentSequence, Utils.lineno());

						if (currentSequenceLength > bestSequenceLength) {
							bestSequence.setAccesses(new ArrayList<>(currentSequence.getAccesses()));
							bestSequence.setFrequency(currentSequence.getFrequency());

	//						Utils.print("Current sequence is longer than best sequence", Utils.lineno());
	//						Utils.print("New best sequence: " + bestSequence, Utils.lineno());

						} else if (bestSequenceLength > 0 && currentSequenceLength == bestSequenceLength) {
							if (currentSequence.getAccesses().size() < bestSequence.getAccesses().size()) {
								bestSequence.setAccesses(new ArrayList<>(currentSequence.getAccesses()));
								bestSequence.setFrequency(currentSequence.getFrequency());

	//							Utils.print("Both sequences have the same size but current sequence has less accesses", Utils.lineno());
							}
						}

						currentSequence.reset();
					}

					limit++;
					i = limit + 1;
				}
			}

			// release memory
			currentSequence = null;
			bestSequence = null;
			accessesToMatch = null;
		}

//		Utils.print("Generated sequences: " + traceSequences, Utils.lineno());

		Utils.print("------------------ TRANSFORMING TYPES ------------------", Utils.lineno());
		// transform each Access into one with frequency
		// and replace one-access sequences with frequency > 1 by increasing the access one
		List<Sequence<AccessWithFrequency>> traceSequencesWithAccessesWithFrequency = new ArrayList<>();

		for (Sequence<Access> s : traceSequences) {
			Sequence<AccessWithFrequency> newSeq = new Sequence<>();

			if (s.getAccesses().size() == 1 && s.getFrequency() > 1) {
				newSeq.addSingleAccess(new AccessWithFrequency(s.getAccesses().get(0), s.getFrequency()));
//				Utils.print("New sequence: " + newSeq, Utils.lineno());

			} else {
				newSeq.setFrequency(s.getFrequency());
				for (Access acc : s.getAccesses()) {
					newSeq.addSingleAccess(new AccessWithFrequency(acc, 1));
				}
			}

			traceSequencesWithAccessesWithFrequency.add(newSeq);

			// release memory
			s = null;
		}

		// Utils.print("Transformed sequences: " + traceSequencesWithAccessesWithFrequency, Utils.lineno());
		// release memory
		traceSequences = null;

		Utils.print("------------------ COMPACTING SEQUENCES ------------------", Utils.lineno());

		// Compact consecutive sequences with frequency set to 1
		if (traceSequencesWithAccessesWithFrequency.size() > 1) {
			Sequence<AccessWithFrequency> currentSeq;

			List<Sequence<AccessWithFrequency>> compactedSequences = new ArrayList<>();
			compactedSequences.add(traceSequencesWithAccessesWithFrequency.get(0));

			Sequence<AccessWithFrequency> lastCompactedSequence;

			for (int i = 1; i < traceSequencesWithAccessesWithFrequency.size(); i++) {
				currentSeq = traceSequencesWithAccessesWithFrequency.get(i);
				lastCompactedSequence = compactedSequences.get(compactedSequences.size() - 1);

//				Utils.print("Current sequence: " + currentSeq, Utils.lineno());
//				Utils.print("Last compacted sequence: " + lastCompactedSequence, Utils.lineno());

				if (lastCompactedSequence.getFrequency() == 1) {
					if (currentSeq.getFrequency() > 1 && currentSeq.getAccesses().size() == 1) {
//						Utils.print("Current sequence is expanding", Utils.lineno());

						List<AccessWithFrequency> expandedAccesses = new ArrayList<>();
						for (int j = 0; j < currentSeq.getFrequency(); j++) {
							expandedAccesses.add(currentSeq.getAccesses().get(0));
						}

						lastCompactedSequence.addMultipleAccesses(expandedAccesses);

					} else if (currentSeq.getFrequency() == 1) {
//						Utils.print("Current sequence is repeated only once", Utils.lineno());

						lastCompactedSequence.addMultipleAccesses(currentSeq.getAccesses());

					} else {
						compactedSequences.add(currentSeq);
//						Utils.print("Cannot compact sequences!", Utils.lineno());
					}

//					Utils.print("New last compacted sequence: " + lastCompactedSequence, Utils.lineno());

				} else {
					compactedSequences.add(currentSeq);
//					Utils.print("Cannot compact sequences!", Utils.lineno());
				}
			}

			// release memory
			currentSeq = null;
			lastCompactedSequence = null;

			if (compactedSequences.size() < traceSequencesWithAccessesWithFrequency.size()) {
				traceSequencesWithAccessesWithFrequency = compactedSequences;
			}

			// release memory
			compactedSequences = null;
		}

//		Utils.print("Compacted sequences: " + traceSequencesWithAccessesWithFrequency, Utils.lineno());

		Utils.print("------------------ COMPACTING ACCESSES WITHIN SEQUENCES ------------------", Utils.lineno());
		// Compact accesses within each sequence
		for (Sequence<AccessWithFrequency> s : traceSequencesWithAccessesWithFrequency) {
			s.setAccesses(compactConsecutiveEqualAccessesWithFrequency(s.getAccesses()));
		}

//		Utils.print("Compacted accesses within sequences: " + traceSequencesWithAccessesWithFrequency, Utils.lineno());

		return new TraceWithSequences(trace, traceSequencesWithAccessesWithFrequency);
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
								TraceWithSequences tws = compactTraceConsecutiveEqualSequences(t);
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
