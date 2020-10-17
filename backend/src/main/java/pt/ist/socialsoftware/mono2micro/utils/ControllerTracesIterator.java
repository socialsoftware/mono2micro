package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ControllerTracesIterator {
	private JsonParser jsonParser;
	private int limit = 0; // 0 means no limit aka all traces will be parsed
	private int tracesCounter; // #traces
	String filePath;

	public ControllerTracesIterator(
		String filePath,
		int limit
	) throws IOException {
		this.limit = limit;
		this.filePath = filePath;

		init();
	}

	private void init() throws IOException {
		this.tracesCounter = 0;

		ObjectMapper mapper = new ObjectMapper();
		JsonFactory jsonfactory = mapper.getFactory();

		jsonParser = jsonfactory.createParser(new FileInputStream(filePath));

		jsonParser.nextToken();

		if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
			System.err.println("Json must start with a left curly brace");
			System.exit(-1);
		}

		jsonParser.nextValue();

	}

	public void reset() throws IOException {
		if (!jsonParser.isClosed())
			jsonParser.close();

		init();
	}

	public TraceDto nextTrace() throws IOException {
		if (jsonParser.getCurrentToken() == JsonToken.END_ARRAY || jsonParser.getCurrentToken() == JsonToken.END_OBJECT)
			return null;

		TraceDto t = jsonParser.readValueAs(TraceDto.class);
		jsonParser.nextValue();

		if (t.getElements() != null && t.getElements().size() > 0)
			tracesCounter++;

		return t;
	}

	public void nextControllerWithName(
		String controllerName
	)
		throws IOException
	{
		tracesCounter = 0;

		while (true) {
			if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
				if (
					jsonParser.getCurrentName() != null &&
					(controllerName == null || jsonParser.getCurrentName().equals(controllerName)) // null means every controller is acceptable
				) {
					break;
				}

				jsonParser.skipChildren();

			} else if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
				jsonParser.skipChildren();
			}

			jsonParser.nextValue();
		}

		findTraceFieldLoop:
		while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
			switch (jsonParser.getCurrentName()) {
				case "t":
					if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
						System.err.println("Json must start with a left bracket");
						System.exit(-1);
					}

					jsonParser.nextValue();
					break findTraceFieldLoop;
				case "id":
				case "f":
					break;

				default:
					throw new IOException();
			}
		}
	}

	public boolean hasMoreControllers() {
		return jsonParser.getCurrentToken() != JsonToken.END_OBJECT; // FIXME TEST ME
	}

	public boolean hasMoreTraces() {
		return (
			jsonParser.getCurrentToken() != JsonToken.END_ARRAY &&
			jsonParser.getCurrentToken() != JsonToken.END_OBJECT &&
			(limit == 0 || tracesCounter < limit)
		);
	}

	public TraceDto getLongestTrace() throws IOException {
		if (this.hasMoreTraces()) {
			TraceDto t1 = this.nextTrace();
			int t1UncompressedSize = t1.getUncompressedSize();

			while (this.hasMoreTraces()) {
				TraceDto t2 = this.nextTrace();
				int t2UncompressedSize = t2.getUncompressedSize();

				if (t2UncompressedSize > t1UncompressedSize) {
					t1 = t2;
					t1UncompressedSize = t2UncompressedSize;
				}
			}

			return t1;
		}

		return null;
	}

	public TraceDto getTraceWithMoreDifferentAccesses() throws IOException {
		if (this.hasMoreTraces()) {
			TraceDto t1 = this.nextTrace();
			int t1AccessesSetSize = t1.getAccessesSet().size();

			while (this.hasMoreTraces()) {
				TraceDto t2 = this.nextTrace();
				int t2AccessesSetSize = t2.getAccessesSet().size();

				if (t2AccessesSetSize > t1AccessesSetSize) {
					t1 = t2;
					t1AccessesSetSize = t2AccessesSetSize;
				}
			}

			return t1;
		}

		return null;
	}

	public Set<String> getRepresentativeTraces() throws IOException {
		Map<String, HashSet<String>> traceIdToAccessesMap = new HashMap<>();

		if (this.hasMoreTraces()) {
			TraceDto t1 = this.nextTrace();
			traceIdToAccessesMap.put(String.valueOf(t1.getId()), t1.getAccessesSet());

			while (this.hasMoreTraces()) {
				TraceDto t2 = this.nextTrace();
				HashSet<String> t2AccessesSet = t2.getAccessesSet();

				Iterator<Map.Entry<String, HashSet<String>>> iter = traceIdToAccessesMap.entrySet().iterator();
				boolean t2IsRepresentative = true;

				while (iter.hasNext()) {
					Map.Entry<String, HashSet<String>> entry = iter.next();

					if (entry.getValue().containsAll(t2AccessesSet)) { // t2 C t[i] => t2 is not representative
						t2IsRepresentative = false;
						break;
					}

					else if(t2AccessesSet.containsAll(entry.getValue())) { // t[i] C t2 => t2 is representative and t[i] isn't
						iter.remove();
					}

					// unnecessary else statement cuz both t[i] and t2 are representative
				}

				if (t2IsRepresentative)
					traceIdToAccessesMap.put(String.valueOf(t2.getId()), t2AccessesSet);
			}
		}

		return traceIdToAccessesMap.keySet();
	}
}
