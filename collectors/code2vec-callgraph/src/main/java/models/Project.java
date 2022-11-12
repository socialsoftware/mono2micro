package models;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.json.*;

public class Project {

	private String _name;
	private String _outputFile;
	private Map<String, Package> _packages; // key: packageName

	public Project(String repo) {
		_name = repo;
		File dir = new File("data/");
		dir.mkdir();
		dir = new File("data/collection/");
		dir.mkdir();
		_outputFile = "data/collection/" + repo + "_code_embeddings.json";
		_packages = new HashMap<String, Package>();
	}

	private Boolean hasPackage(String packageName) {
		return _packages.containsKey(packageName);
	}

	private Package getPackage(String packageName) {
		return _packages.get(packageName);
	}

	private void addPackage(String packageName) {
		_packages.put(packageName, new Package(packageName));
	}

	public void addClass(String packageName, String className, String classType, String superQualifiedName) {
		if (!hasPackage(packageName)) {
			addPackage(packageName);
		}
		getPackage(packageName).addClass(className, classType, superQualifiedName);
	}

	public void addMethod(String packageName, String className, String signature, List<Float> codeVector, String type) {
		if (!hasPackage(packageName)) {
			addPackage(packageName);
		}
		getPackage(packageName).addMethod(className, signature, codeVector, type);
	}

	public void addMethodCallToLast(String packageName, String className, String mcPackName, String mcClsName, String mcSignature) {
		if (!hasPackage(packageName)) return;
		getPackage(packageName).addMethodCallToLast(className, mcPackName, mcClsName, mcSignature);
	}

	public void saveToFile() {
		try {
			File newFile = new File(_outputFile);
			if (newFile.createNewFile()) {
				FileWriter fileWriter = new FileWriter(this._outputFile);
				fileWriter.write(this.toJson().toString());
				fileWriter.close();
			} else {
				System.out.println("[-] File already exists.");
			}
		} catch (IOException e) {
			System.out.println("[-] IOException : " + e.toString());
		}
	}

	private List<JSONObject> packagesToJson() {
		List<JSONObject> res = new ArrayList<JSONObject>();
		for (Package pack : _packages.values()) {
			res.add(pack.toJson());
		}
		return res;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("name", _name);
		json.put("packages", packagesToJson());
		return json;
	}
}