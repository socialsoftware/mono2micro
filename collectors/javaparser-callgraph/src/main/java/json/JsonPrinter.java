package ist.meic.vascofaria.json;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.json.*;

public class JsonPrinter {

	private StringBuilder jsonCompilationUnits = new StringBuilder("{\n");
	private Integer ident = 2;
	private String outputFile = "/home/vascoffaria/Desktop/Thesis/result.json";

	public JsonPrinter() {}

	public void addXmlCompilationUnit(String path, Integer level, String xml) {
		try {
			if (xml.length() > 0) {
				JSONObject json = XML.toJSONObject(xml);
				String jsonString = json.toString(this.ident);
				this.jsonCompilationUnits.append("  \"class\": ");
				this.jsonCompilationUnits.append(jsonString);
				this.jsonCompilationUnits.append(",\n");
			}
		} catch (JSONException e) {
			System.out.println(e.toString());
		}
	}

	public void addJsonCompilationUnit(String path, Integer level, String json) {
		if (json.length() > 0) {
			this.jsonCompilationUnits.append("  \"class\": ");
			this.jsonCompilationUnits.append(json);
			this.jsonCompilationUnits.append(",\n");
		}
	}

	public void addJsonCompilationUnit(String json) {
		if (json.length() > 0) {
			this.jsonCompilationUnits.append("  \"class\": ");
			this.jsonCompilationUnits.append(json);
			this.jsonCompilationUnits.append(",\n");
		}
	}

	public String xmlToJson(String xml) {
		try {
			JSONObject json = XML.toJSONObject(xml);
			String jsonString = json.toString(4);
			return jsonString;
		} catch (JSONException e) { 
			System.out.println("[-] " + e.toString());
		}
		return "";
	}

	public void saveToFile() {
		String str = this.jsonCompilationUnits.toString();
		this.jsonCompilationUnits.deleteCharAt(str.length() - 2);
		this.jsonCompilationUnits.append("}");
		try {
			File newFile = new File(this.outputFile);
			if (newFile.createNewFile()) {
				System.out.println("File created: " + newFile.getName());
				FileWriter fileWriter = new FileWriter(this.outputFile);
				fileWriter.write(this.jsonCompilationUnits.toString());
				fileWriter.close();
				System.out.println("Successfully wrote to the file.");
			} else {
				System.out.println("File already exists.");
			}
		} catch (IOException e) {
			System.out.println("[-] " + e.toString());
		}
	}

}