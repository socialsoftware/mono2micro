package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class FunctionalityTracesIterator {
	private JsonParser jsonParser;
	private int limit = 0; // 0 means no limit aka all traces will be parsed
	private int tracesCounter; // #traces
	String filePath;

	public FunctionalityTracesIterator(
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

	public void jumpToNextFunctionality() throws IOException {
		while (jsonParser.getCurrentName() != null && jsonParser.getCurrentToken() != JsonToken.START_OBJECT ||					// Either finds beginning of other functionality
				jsonParser.getCurrentName() == null && jsonParser.getCurrentToken() != JsonToken.END_OBJECT) { 					// Or finds the end of the file
			jsonParser.nextValue();
		}
	}

	public String nextFunctionalityWithName(
		String functionalityName
	)
		throws IOException
	{
		tracesCounter = 0;
		String name;

		while (true) {
			if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
				if (
					jsonParser.getCurrentName() != null &&
					(functionalityName == null || jsonParser.getCurrentName().equals(functionalityName)) // null means every functionality is acceptable
				) {
					name = jsonParser.getCurrentName();
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
		return name;
	}

	// Be sure this iterator is already positioned to read the traces of a certain functionality
	public List<TraceDto> getTracesByType(Constants.TraceType traceType) throws IOException {
		List<TraceDto> traceDtos = new ArrayList<>();

		// Get traces according to trace type
		switch(traceType) {
			case LONGEST:
				traceDtos.add(this.getLongestTrace());
				break;
			case WITH_MORE_DIFFERENT_ACCESSES:
				traceDtos.add(this.getTraceWithMoreDifferentAccesses());
				break;
			default:
				while (this.hasMoreTraces())
					traceDtos.add(this.nextTrace());
		}
		if (traceDtos.size() == 0)
			throw new IOException("Functionality does not contain any trace.");

		return traceDtos;
	}

	public boolean hasMoreFunctionalities() throws IOException {
		return !(jsonParser.getCurrentName() == null && jsonParser.getCurrentToken() == JsonToken.END_OBJECT);
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
}
