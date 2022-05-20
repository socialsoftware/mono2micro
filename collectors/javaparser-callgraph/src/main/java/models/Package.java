package models;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;

import org.json.*;

public class Package {

	private String _name;
	private Map<String, Class> _classes;

	public Package(String name) {
		_name = name;
		_classes = new HashMap<String, Class>();
	}

	private Boolean hasClass(String className) {
		return _classes.containsKey(className);
	}

	private Class getClass(String className) {
		return _classes.get(className);
	}

	public void addClass(String className, String classType, String superQualifiedName) {
		if (!hasClass(className)) {
			_classes.put(className, new Class(className, classType, superQualifiedName));
		}
	}

	public void addMethod(String className, String signature, List<Float> codeVector, String type) {
		if (!hasClass(className)) return;
		getClass(className).addMethod(signature, codeVector, type);
	}

	public void addMethodCallToLast(String className, String mcPackName, String mcClsName, String mcSignature) {
		if (!hasClass(className)) return;
		getClass(className).addMethodCallToLast(mcPackName, mcClsName, mcSignature);
	}

	private List<JSONObject> classesToJson() {
		List<JSONObject> res = new ArrayList<JSONObject>();
		for (Class c : _classes.values()) {
			res.add(c.toJson());
		}
		return res;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("name", _name);
		json.put("classes", classesToJson());
		return json;
	}
}