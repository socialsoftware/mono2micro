package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;

import java.io.FileInputStream;
import java.io.IOException;

public class ControllerTracesIterator {
	private JsonParser jsonParser;

	public ControllerTracesIterator(
		String filePath,
		String controllerName
	) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonFactory jsonfactory = mapper.getFactory();

		jsonParser = jsonfactory.createParser(new FileInputStream(filePath));

		jsonParser.nextToken();

		if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
			System.err.println("Json must start with a left curly brace");
			System.exit(-1);
		}

		jsonParser.nextValue();

		controllersLoop:
		while (jsonParser.getCurrentToken() != JsonToken.END_OBJECT) {
			if (!jsonParser.getCurrentName().equals(controllerName)) {
				jsonParser.skipChildren();
			}

			else {
				jsonParser.nextToken();

				while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
					switch (jsonParser.getCurrentName()) {
						case "traces":
							if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
								System.err.println("Json must start with a left bracket");
								System.exit(-1);
							}

							jsonParser.nextToken();
							break controllersLoop;

						case "id":
						case "f":
							break;

						default:
							throw new IOException();
					}
				}

				break;
			}

			jsonParser.nextValue();
		}
	}

	public TraceDto nextTrace() throws IOException {
		if (jsonParser.getCurrentToken() == JsonToken.END_ARRAY || jsonParser.getCurrentToken() == JsonToken.END_OBJECT)
			return null;

		TraceDto t = jsonParser.readValueAs(TraceDto.class);
		jsonParser.nextToken();

		return t;
	}

	public boolean hasMoreTraces() {
		return jsonParser.getCurrentToken() != JsonToken.END_ARRAY && jsonParser.getCurrentToken() != JsonToken.END_OBJECT;
	}
}
