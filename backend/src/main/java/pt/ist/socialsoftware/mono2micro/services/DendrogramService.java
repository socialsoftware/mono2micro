package pt.ist.socialsoftware.mono2micro.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Acumulator;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;

@Service
public class DendrogramService {

    @Autowired
    ClusterService clusterService;

    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

    public Dendrogram createDendrogramByFeatures(
            String codebaseName,
            Dendrogram dendrogram,
            Boolean analysisMode,
            Integer threadNumber
    ) {
        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);

            if (codebase.getDendrogram(dendrogram.getName()) != null)
                throw new KeyAlreadyExistsException();

            File dendrogramPath = new File(CODEBASES_PATH + codebase.getName() + "/" + dendrogram.getName());
            if (!dendrogramPath.exists()) {
                dendrogramPath.mkdir();
            }

            if (dendrogram.getFeatureVectorizationStrategy().equals("methodCalls")) {
                this.createDendrogramByMethodCallsStrategy(codebase, dendrogram, analysisMode, threadNumber);
            } else if (dendrogram.getFeatureVectorizationStrategy().equals("entitiesTraces")) {
                this.createDendrogramByEntitiesTracesStrategy(codebase, dendrogram, analysisMode);
            } else {
                this.createDendrogramByMixedStrategy(codebase, dendrogram, analysisMode, threadNumber);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dendrogram;
    }

    public void createDendrogramByEntitiesStrategy(
            String codebaseName,
            Dendrogram dendrogram,
            Boolean analysisMode
    )
            throws Exception
    {
        Codebase codebase = codebaseManager.getCodebase(codebaseName);

        if (codebase.getDendrogram(dendrogram.getName()) != null)
            throw new KeyAlreadyExistsException();

        File dendrogramPath = new File(CODEBASES_PATH + codebase.getName() + "/" + dendrogram.getName());
        if (!dendrogramPath.exists()) {
            dendrogramPath.mkdir();
        }

        List<HashMap> entitiesVectors = new ArrayList<>();
        HashMap<String, Object> entitiesJson = new HashMap<String, Object>();
        Integer numberOfEntities = 0;
        JSONObject codeEmbeddings = codebaseManager.getCodeEmbeddings(codebase.getName());
        JSONArray packages = codeEmbeddings.getJSONArray("packages");
        entitiesJson.put("name", codeEmbeddings.getString("name"));
        entitiesJson.put("linkageType", dendrogram.getLinkageType());

        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);
            JSONArray classes = pack.optJSONArray("classes");

            for (int j = 0; j < classes.length(); j++) {
                JSONObject cls = classes.getJSONObject(j);
                JSONArray methods = cls.optJSONArray("methods");
                String classType = cls.getString("type");
                String className = cls.getString("name");

                if (classType.equals("Entity") && !className.endsWith("_Base")) {
                    numberOfEntities++;

                    Acumulator acumulator = new Acumulator();
                    getAscendedClassesMethodsCodeVectors(packages, acumulator, cls.getString("superQualifiedName"));

                    for (int k = 0; k < methods.length(); k++) {
                        JSONObject method = methods.getJSONObject(k);
                        JSONArray code_vector = method.getJSONArray("codeVector");
                        acumulator.addVector(code_vector);
                    }

                    if (acumulator.getCount() > 0) {
                        HashMap<String, Object> entityEmbeddings = new HashMap<String, Object>();
                        entityEmbeddings.put("package", pack.getString("name"));
                        entityEmbeddings.put("name", className);
                        entityEmbeddings.put("translationID", cls.getInt("translationID"));
                        entityEmbeddings.put("codeVector", acumulator.getMeanVector());
                        entitiesVectors.add(entityEmbeddings);
                    }

                }

            }

        }

        entitiesJson.put("numberOfEntities", numberOfEntities);
        entitiesJson.put("entities", entitiesVectors);

        codebaseManager.writeEntitiesCodeVectorsFile(codebase.getName(), entitiesJson);

        if (!analysisMode) {
            codebase.addDendrogram(dendrogram);
            clusterService.executeCreateDendrogram(codebase.getName(), dendrogram, "/entities");
            codebaseManager.writeCodebase(codebase);
        }
    }

    private Acumulator getAscendedClassesMethodsCodeVectors(
        JSONArray packages,
        Acumulator acumulator,
        String qualifiedName
    )
        throws JSONException
    {
        if (qualifiedName.isEmpty()) return acumulator;

        String[] splittedStr = qualifiedName.split("[.]");
        String packageName = splittedStr[0];
        for (int i = 1; i < splittedStr.length-1; i++) {
            packageName += "." + splittedStr[i];
        }
        String className = splittedStr[splittedStr.length - 1];

        JSONObject cls = getClassMethodsCodeVectors(acumulator, packages, packageName, className);
        if (cls == null) return acumulator;

        return getAscendedClassesMethodsCodeVectors(packages, acumulator, cls.getString("superQualifiedName"));
    }

    public void createDendrogramByClassesStrategy(
            String codebaseName,
            Dendrogram dendrogram,
            Boolean analysisMode
    )
            throws Exception
    {
        Codebase codebase = codebaseManager.getCodebase(codebaseName);

        if (codebase.getDendrogram(dendrogram.getName()) != null)
            throw new KeyAlreadyExistsException();

        File dendrogramPath = new File(CODEBASES_PATH + codebase.getName() + "/" + dendrogram.getName());
        if (!dendrogramPath.exists()) {
            dendrogramPath.mkdir();
        }

        JSONObject codeEmbeddings = codebaseManager.getCodeEmbeddings(codebase.getName());
        Integer numberOfEntities = 0;
        HashMap<String, Object> classesJson = new HashMap<String, Object>();
        classesJson.put("name", codeEmbeddings.getString("name"));

        List<HashMap> classesVectors = new ArrayList<>();

        JSONArray packages = codeEmbeddings.getJSONArray("packages");

        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);
            JSONArray classes = pack.optJSONArray("classes");

            for (int j = 0; j < classes.length(); j++) {
                JSONObject cls = classes.getJSONObject(j);
                String classType = cls.getString("type");
                String className = cls.getString("name");
                JSONArray methods = cls.optJSONArray("methods");
                List<List<Double>> classMethodsVectors = new ArrayList<List<Double>>();

                if (!className.endsWith("_Base")) {
                    if (classType.equals("Entity")) {
                        numberOfEntities++;
                    }

                    for (int k = 0; k < methods.length(); k++) {
                        JSONObject method = methods.getJSONObject(k);
                        JSONArray codeVectorArray = method.optJSONArray("codeVector");
                        List<Double> codeVector = new ArrayList<Double>();

                        for (int l = 0; l < codeVectorArray.length(); l++) {
                            codeVector.add(codeVectorArray.getDouble(l));
                        }
                        classMethodsVectors.add(codeVector);
                    }

                    if (!classMethodsVectors.isEmpty()) {
                        List<Double> classVector = calculateClassVector(classMethodsVectors);
                        HashMap<String, Object> classEmbeddings = new HashMap<String, Object>();
                        classEmbeddings.put("package", pack.getString("name"));
                        classEmbeddings.put("name", className);
                        classEmbeddings.put("type", cls.getString("type"));
                        if (cls.has("translationID")) classEmbeddings.put("translationID", cls.getInt("translationID"));
                        classEmbeddings.put("codeVector", classVector);
                        classesVectors.add(classEmbeddings);
                    }
                }
            }
        }

        classesJson.put("numberOfEntities", numberOfEntities);
        classesJson.put("linkageType", dendrogram.getLinkageType());
        classesJson.put("classes", classesVectors);

        codebaseManager.writeClassesCodeVectorsFile(codebase.getName(), classesJson);

        if (!analysisMode) {
            codebase.addDendrogram(dendrogram);
            clusterService.executeCreateDendrogram(codebase.getName(), dendrogram, "/classes");
            codebaseManager.writeCodebase(codebase);
        }
    }

    public void createDendrogramByMethodCallsStrategy(
            Codebase codebase,
            Dendrogram dendrogram,
            Boolean analysisMode,
            Integer threadNumber
    )
            throws JSONException, IOException
    {

        HashMap<String, Object> featuresJson = generateMethodCallsFeaturesVectors(codebase, dendrogram, analysisMode);
        codebaseManager.writeFeaturesCodeVectorsFile(codebase.getName(), featuresJson, threadNumber);

        if (!analysisMode) {
            codebase.addDendrogram(dendrogram);
            clusterService.executeCreateDendrogram(codebase.getName(), dendrogram, "/features/methodsCalls");
            codebaseManager.writeCodebase(codebase);
        }
    }

    private HashMap<String, Object> generateMethodCallsFeaturesVectors(
        Codebase codebase,
        Dendrogram dendrogram,
        Boolean analysisMode
    )
        throws IOException, JSONException
    {
        List<HashMap> featuresVectors = new ArrayList<>();
        Integer numberOfEntities = 0;
        HashMap<String, Object> featuresJson = new HashMap<String, Object>();
        JSONObject codeEmbeddings = codebaseManager.getCodeEmbeddings(codebase.getName());
        JSONArray packages = codeEmbeddings.getJSONArray("packages");
        featuresJson.put("name", codeEmbeddings.getString("name"));

        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);
            JSONArray classes = pack.optJSONArray("classes");

            for (int j = 0; j < classes.length(); j++) {
                JSONObject cls = classes.getJSONObject(j);
                JSONArray methods = cls.optJSONArray("methods");
                String className = cls.getString("name");
                String classType = cls.getString("type");

                if (classType.equals("Entity")) {
                    numberOfEntities++;
                }

                for (int k = 0; k < methods.length(); k++) {
                    JSONObject method = methods.getJSONObject(k);

                    if (method.getString("type").equals("Controller")) {
                        // System.out.println("Controller: " + method.getString("signature"));

                        Acumulator acumulator = getMethodCallsVectors(dendrogram, packages, className, classType, method, dendrogram.getMaxDepth(), analysisMode);

                        if (acumulator.getCount() > 0) {
                            vectorDivision(acumulator.getSum(), acumulator.getCount());

                            HashMap<String, Object> featureEmbeddings = new HashMap<String, Object>();
                            featureEmbeddings.put("package", pack.getString("name"));
                            featureEmbeddings.put("class", className);
                            featureEmbeddings.put("signature", method.getString("signature"));
                            featureEmbeddings.put("codeVector", acumulator.getSum());
                            featuresVectors.add(featureEmbeddings);
                        }
                    }
                }
            }
        }

        featuresJson.put("maxDepth", dendrogram.getMaxDepth());
        featuresJson.put("controllersWeight", dendrogram.getControllersWeight());
        featuresJson.put("servicesWeight", dendrogram.getServicesWeight());
        featuresJson.put("intermediateMethodsWeight", dendrogram.getIntermediateMethodsWeight());
        featuresJson.put("entitiesWeight", dendrogram.getEntitiesWeight());
        featuresJson.put("linkageType", dendrogram.getLinkageType());
        featuresJson.put("numberOfEntities", numberOfEntities);
        featuresJson.put("features", featuresVectors);

        return featuresJson;
    }

    public void createDendrogramByEntitiesTracesStrategy(
            Codebase codebase,
            Dendrogram dendrogram,
            Boolean analysisMode
    )
            throws JSONException, IOException
    {
        HashMap<String, Object> tracesJson = generateEntitiesTracesFeaturesVectors(codebase, dendrogram);
        codebaseManager.writeEntitiesTracesCodeVectorsFile(codebase.getName(), tracesJson);

        if (!analysisMode) {
            codebase.addDendrogram(dendrogram);
            clusterService.executeCreateDendrogram(codebase.getName(), dendrogram, "/features/entitiesTraces");
            codebaseManager.writeCodebase(codebase);
        }
    }

    private HashMap<String, Object> generateEntitiesTracesFeaturesVectors(Codebase codebase, Dendrogram dendrogram) throws IOException, JSONException {
        List<HashMap> entitiesVectors = new ArrayList<>();
        Integer numberOfEntities = 0;
        HashMap<String, Object> entitiesJson = new HashMap<String, Object>();
        JSONObject codeEmbeddings = codebaseManager.getCodeEmbeddings(codebase.getName());
        JSONArray packages = codeEmbeddings.getJSONArray("packages");
        entitiesJson.put("name", codeEmbeddings.getString("name"));
        entitiesJson.put("linkageType", dendrogram.getLinkageType());

        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);
            JSONArray classes = pack.optJSONArray("classes");

            for (int j = 0; j < classes.length(); j++) {
                JSONObject cls = classes.getJSONObject(j);
                JSONArray methods = cls.optJSONArray("methods");
                String classType = cls.getString("type");
                String className = cls.getString("name");

                if (classType.equals("Entity") && !className.endsWith("_Base")) {
                    numberOfEntities++;

                    Acumulator acumulator = new Acumulator();
                    getAscendedClassesMethodsCodeVectors(packages, acumulator, cls.getString("superQualifiedName"));

                    for (int k = 0; k < methods.length(); k++) {
                        JSONObject method = methods.getJSONObject(k);
                        JSONArray code_vector = method.getJSONArray("codeVector");
                        acumulator.addVector(code_vector);
                    }

                    if (acumulator.getCount() > 0) {
                        HashMap<String, Object> entityEmbeddings = new HashMap<String, Object>();
                        entityEmbeddings.put("package", pack.getString("name"));
                        entityEmbeddings.put("name", cls.getString("name"));
                        entityEmbeddings.put("translationID", cls.getInt("translationID"));
                        entityEmbeddings.put("codeVector", acumulator.getMeanVector());
                        entitiesVectors.add(entityEmbeddings);
                    }
                }
            }
        }

        entitiesJson.put("entities", entitiesVectors);

        codebaseManager.writeEntitiesCodeVectorsFile(codebase.getName(), entitiesJson);

        List<HashMap> tracesVectors = new ArrayList<>();
        HashMap<String, Object> tracesJson = new HashMap<String, Object>();
        tracesJson.put("name", codeEmbeddings.getString("name"));
        tracesJson.put("linkageType", dendrogram.getLinkageType());
        JSONObject functionalityTraces = codebaseManager.getFunctionalityTraces(codebase.getName());
        Iterator<String> keys = functionalityTraces.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            if (functionalityTraces.get(key) instanceof JSONObject) {
                JSONObject traces = (JSONObject) functionalityTraces.get(key);
                JSONArray trace = traces.getJSONArray("t");
                JSONObject trace_obj  = trace.getJSONObject(0);
                JSONArray accesses = trace_obj.getJSONArray("a");

                float count = 0;
                ArrayList<Double> vector = new ArrayList<Double>();
                for (int idx = 0; idx < 384; idx++) {
                    vector.add(0.0);
                }

                for (int i = 0; i < accesses.length(); i++) {
                    JSONArray access = accesses.getJSONArray(i);
                    String accessType = access.getString(0);
                    Integer entityId = access.getInt(1);
                    float weight = 0;

                    if (accessType.equals("R")) {
                        weight = dendrogram.getReadMetricWeight();
                    } else {
                        weight = dendrogram.getWriteMetricWeight();
                    }

                    for (HashMap entity : entitiesVectors) {
                        if (entity.get("translationID") == entityId) {
                            ArrayList<Double> code_vector = (ArrayList<Double>) entity.get("codeVector");
                            for (int idx = 0; idx < 384; idx++) {
                                vector.set(idx, vector.get(idx) + weight * code_vector.get(idx));
                            }

                            count += weight;
                            break;
                        }
                    }
                }
                if (count > 0) {
                    vectorDivision(vector, count);
                    HashMap<String, Object> traceEmbeddings = new HashMap<String, Object>();
                    traceEmbeddings.put("name", key);
                    traceEmbeddings.put("codeVector", vector);
                    tracesVectors.add(traceEmbeddings);
                }
            }
        }

        tracesJson.put("writeMetricWeight", dendrogram.getWriteMetricWeight());
        tracesJson.put("readMetricWeight", dendrogram.getReadMetricWeight());
        tracesJson.put("numberOfEntities", numberOfEntities);
        tracesJson.put("traces", tracesVectors);

        return tracesJson;
    }

    public void createDendrogramByMixedStrategy(Codebase codebase, Dendrogram dendrogram, Boolean analysisMode, Integer threadNumber)
            throws JSONException, IOException
    {
        HashMap<String, Object> mixedJson = new HashMap<String, Object>();
        List<HashMap> mixedVectors = new ArrayList<>();
        HashMap<String, Object> featuresJson = generateMethodCallsFeaturesVectors(codebase, dendrogram, analysisMode);
        HashMap<String, Object> tracesJson = generateEntitiesTracesFeaturesVectors(codebase, dendrogram);

        mixedJson.put("linkageType", dendrogram.getLinkageType());

        mixedJson.put("name", featuresJson.get("name"));
        mixedJson.put("maxDepth", featuresJson.get("maxDepth"));
        mixedJson.put("controllersWeight", featuresJson.get("controllersWeight"));
        mixedJson.put("servicesWeight", featuresJson.get("servicesWeight"));
        mixedJson.put("intermediateMethodsWeight", featuresJson.get("intermediateMethodsWeight"));
        mixedJson.put("entitiesWeight", featuresJson.get("entitiesWeight"));
        mixedJson.put("linkageType", featuresJson.get("linkageType"));
        List<HashMap<String, Object>> featuresVectorsHM = (List<HashMap<String, Object>>) featuresJson.get("features");
        JSONArray featuresVectors = new JSONArray();
        for (int i = 0; i < featuresVectorsHM.size(); i++) {
            JSONObject newObj = new JSONObject();
            newObj.put("signature", featuresVectorsHM.get(i).get("signature"));
            newObj.put("class", featuresVectorsHM.get(i).get("class"));
            List<Double> list = (List<Double>) featuresVectorsHM.get(i).get("codeVector");
            JSONArray ja = new JSONArray();
            for (Double d : list) { ja.put(d); }
            newObj.put("codeVector", ja);
            featuresVectors.put(newObj);
        }

        mixedJson.put("writeMetricWeight", tracesJson.get("writeMetricWeight"));
        mixedJson.put("readMetricWeight", tracesJson.get("readMetricWeight"));
        mixedJson.put("numberOfEntities", tracesJson.get("numberOfEntities"));
        List<HashMap<String, Object>> tracesVectorsHM = (List<HashMap<String, Object>>) tracesJson.get("traces");
        JSONArray tracesVectors = new JSONArray();
        for (int i = 0; i < tracesVectorsHM.size(); i++) {
            JSONObject newObj = new JSONObject();
            newObj.put("name", tracesVectorsHM.get(i).get("name"));
            List<Double> list = (List<Double>) tracesVectorsHM.get(i).get("codeVector");
            JSONArray ja = new JSONArray();
            for (Double d : list) { ja.put(d); }
            newObj.put("codeVector", ja);
            tracesVectors.put(newObj);
        }

        mixedJson.put("methodsCallsWeight", dendrogram.getMethodsCallsWeight());
        mixedJson.put("entitiesTracesWeight", dendrogram.getEntitiesTracesWeight());
        int fvLength = featuresVectors.length();
        int etLength = tracesVectors.length();
        for (int f = 0; f < fvLength; f++) {
            JSONObject feature = featuresVectors.getJSONObject(f);

            String[] splitStr = feature.getString("signature").split("\\(")[0].split("\\.");
            String name = feature.getString("class") + "." + splitStr[splitStr.length - 1];

            JSONArray jsonVector = feature.getJSONArray("codeVector");
            ArrayList<Double> vector = new ArrayList<Double>();
            int len = jsonVector.length();
            for (int i = 0; i < len; i++) {
                vector.add(dendrogram.getMethodsCallsWeight() * jsonVector.getDouble(i));
            }

            for (int t = 0; t < etLength; t++) {
                JSONObject trace = tracesVectors.getJSONObject(t);

                if (name.equals(trace.getString("name"))) {

                    JSONArray jsonTraceVector = feature.getJSONArray("codeVector");
                    ArrayList<Double> traceVector = new ArrayList<Double>();
                    for (int i = 0; i < len; i++) {
                        traceVector.add(dendrogram.getEntitiesTracesWeight() * jsonTraceVector.getDouble(i));
                    }
                    vectorSum(vector, traceVector);
                    vectorDivision(vector, len * (dendrogram.getMethodsCallsWeight() + dendrogram.getEntitiesTracesWeight()));
                    HashMap<String, Object> mixedEmbeddings = new HashMap<String, Object>();
                    mixedEmbeddings.put("name", name);
                    mixedEmbeddings.put("codeVector", vector);
                    mixedVectors.add(mixedEmbeddings);
                }
            }
        }

        mixedJson.put("features", mixedVectors);
        codebaseManager.writeMixedCodeVectorsFile(codebase.getName(), mixedJson, threadNumber);

        if (!analysisMode) {
            codebase.addDendrogram(dendrogram);
            clusterService.executeCreateDendrogram(codebase.getName(), dendrogram, "/features/mixed");
            codebaseManager.writeCodebase(codebase);
        }
    }

    // ----------------------------------------------------------------------------------------------
    // ---                                AUXILIARY FUNCTIONS                                     ---
    // ----------------------------------------------------------------------------------------------

    public List<Double> calculateClassVector(List<List<Double>> class_methods_vectors) {
        List<Double> class_vector = new ArrayList<Double>();

        int len = class_methods_vectors.get(0).size();

        Double sum = 0.0;

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < class_methods_vectors.size(); j++) {
                sum += class_methods_vectors.get(j).get(i);
            }
            class_vector.add(sum / class_methods_vectors.size());
            sum = 0.0;
        }

        return class_vector;
    }

    public JSONObject getClassMethodsCodeVectors(
            Acumulator acumulator,
            JSONArray packages,
            String packageName,
            String className
    )
            throws JSONException
    {
        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);

            if (pack.getString("name").equals(packageName)) {
                JSONArray classes = pack.optJSONArray("classes");

                for (int j = 0; j < classes.length(); j++) {
                    JSONObject cls = classes.getJSONObject(j);

                    if (cls.getString("name").equals(className)) {
                        JSONArray methods = cls.getJSONArray("methods");

                        for (int k = 0; k < methods.length(); k++) {
                            JSONObject method = methods.getJSONObject(k);
                            JSONArray codeVector = method.getJSONArray("codeVector");
                            acumulator.addVector(codeVector);
                        }

                        return cls;
                    }
                }
            }
        }
        return null;
    }

    public Acumulator getMethodCallsVectors(
            Dendrogram dendrogram,
            JSONArray packages,
            String className,
            String classType,
            JSONObject method,
            int maxDepth,
            Boolean analysisMode
    )
            throws JSONException
    {
        float count = 0;
        ArrayList<Double> vector = new ArrayList<Double>();
        JSONArray code_vector = method.getJSONArray("codeVector");
        JSONArray methodCalls = method.optJSONArray("methodCalls");
        String methodType = method.getString("type");

        if (methodType.equals("Controller")) {
            count = dendrogram.getControllersWeight();
        } else if (classType.equals("Service") || methodType.equals("Service")) {
            count = dendrogram.getServicesWeight();
        } else if (classType.equals("Entity")) {
            count = dendrogram.getEntitiesWeight();
        } else {
            count = dendrogram.getIntermediateMethodsWeight();
        }

        for (int idx = 0; idx < 384; idx++) {
            vector.add(count * code_vector.getDouble(idx));
        }

        if (maxDepth == 0 || methodCalls.length() == 0) {
            return new Acumulator(vector, count);
        }

        for (int l = 0; l < methodCalls.length(); l++) {
            JSONObject methodCall = methodCalls.getJSONObject(l);

            try {
                JSONObject met = getMethodCall(
                        packages,
                        methodCall.getString("packageName"),
                        methodCall.getString("className"),
                        methodCall.getString("signature")
                );

                if (met != null) {

                    Acumulator acum = getMethodCallsVectors(
                            dendrogram,
                            packages,
                            methodCall.getString("className"),
                            met.getString("classType"),
                            met,
                            maxDepth - 1,
                            analysisMode
                    );

                    vectorSum(vector, acum.getSum());
                    count += acum.getCount();

                } else {
                    if (!analysisMode) {
                        System.err.println("[ - ] Cannot get method call for method: " + methodCall.getString("signature"));
                    }
                }
            } catch (JSONException je) {
                if (!analysisMode) {
                    System.err.println("[ - ] Cannot get method call for method: " + methodCall.getString("signature"));
                }
            }
        }

        return new Acumulator(vector, count);
    }

    public JSONObject getMethodCall(JSONArray packages, String callPackage, String callClass, String callSignature)
            throws JSONException
    {
        for (int i = 0; i < packages.length(); i++) {
            JSONObject pack = packages.getJSONObject(i);

            if (pack.getString("name").equals(callPackage)) {
                JSONArray classes = pack.optJSONArray("classes");

                for (int j = 0; j < classes.length(); j++) {
                    JSONObject cls = classes.getJSONObject(j);
                    String classType = cls.getString("type");

                    if (cls.getString("name").equals(callClass)) {
                        JSONArray methods = cls.optJSONArray("methods");

                        for (int k = 0; k < methods.length(); k++) {
                            JSONObject method = methods.getJSONObject(k);

                            if (method.getString("signature").equals(callSignature)) {
                                method.put("classType", classType);
                                return method;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void vectorSum(ArrayList<Double> vector, ArrayList<Double> array)
    {
        for (int i = 0; i < array.size(); i++) {
            vector.set(i, vector.get(i) + array.get(i));
        }
    }

    public void vectorDivision(ArrayList<Double> vector, float count) {
        for (int i = 0; i < vector.size(); i++) {
            vector.set(i, vector.get(i) / count);
        }
    }

}
