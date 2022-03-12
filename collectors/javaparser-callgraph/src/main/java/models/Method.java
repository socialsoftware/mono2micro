package models;

import java.util.List;
import java.util.ArrayList;
import org.json.*;

public class Method {

	private String _signature;
	private List<Float> _codeVector;
	private String _type;
	private List<MethodCall> _methodCalls;

	public Method(String signature, List<Float> codeVector, String type) {
		_signature = signature;
		_codeVector = codeVector;
		_type = type;
		_methodCalls = new ArrayList<MethodCall>();
	}

	public void addMethodCall(String packageName, String className, String signature) {
		_methodCalls.add(new MethodCall(packageName, className, signature));
	}

	private List<JSONObject> methodCallsToJson() {
		List<JSONObject> res = new ArrayList<JSONObject>();
		for (MethodCall call : _methodCalls) {
			res.add(call.toJson());
		}
		return res;
	}

	private List<Float> codeVectorToJson() {
		List<Float> res = new ArrayList<Float>();
		for (Float num : _codeVector) {
			res.add(num);
		}
		return res;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("signature", _signature);
		json.put("codeVector", codeVectorToJson());
		json.put("type", _type);
		json.put("methodCalls", methodCallsToJson());
		return json;
	}
}