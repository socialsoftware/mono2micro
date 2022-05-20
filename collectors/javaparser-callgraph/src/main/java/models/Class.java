package models;

import java.util.List;
import java.util.ArrayList;
import org.json.*;

public class Class {

	private String _name;
	private String _type; // interface or class
	private List<Method> _methods;

	private String _superQualifiedName;

	public Class(String name, String type, String superQualifiedName) {
		_name = name;
		_type = type;
		_superQualifiedName = superQualifiedName;
		_methods = new ArrayList<Method>();
	}

	public void addMethod(String signature, List<Float> codeVector, String type) {
		_methods.add(new Method(signature, codeVector, type));
	}

	public void addMethodCallToLast(String packageName, String className, String signature) {
		if (_methods.size() == 0) return;
		Method lastInsertedMethod = _methods.get(_methods.size() - 1);
		lastInsertedMethod.addMethodCall(packageName, className, signature);
	}

	private List<JSONObject> methodsToJson() {
		List<JSONObject> res = new ArrayList<JSONObject>();
		for (Method m : _methods) {
			res.add(m.toJson());
		}
		return res;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("name", _name);
		json.put("type", _type);
		json.put("superQualifiedName", _superQualifiedName);
		json.put("methods", methodsToJson());
		return json;
	}
}