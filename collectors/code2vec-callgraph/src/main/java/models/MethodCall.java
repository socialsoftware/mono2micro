package models;

import org.json.*;

public class MethodCall {

	private String _packageName;
	private String _className;
	private String _signature;

	public MethodCall(String packageName, String className, String signature) {
		_packageName = packageName;
		_className = className;
		_signature = signature;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("packageName", _packageName);
		json.put("className", _className);
		json.put("signature", _signature);
		return json;
	}

}