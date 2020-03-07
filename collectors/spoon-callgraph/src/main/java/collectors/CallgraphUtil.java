package collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CallgraphUtil {

    public static List<CtAbstractInvocation> getCalleesOf(CtExecutable method) {
        Map<Integer,CtAbstractInvocation> orderedCallees = new TreeMap<>();
        List<CtAbstractInvocation> methodCalls = method.getElements(new TypeFilter<>(CtAbstractInvocation.class));
        for (CtAbstractInvocation i : methodCalls)
            try {
                orderedCallees.put(i.getPosition().getSourceEnd(), i);
            } catch (Exception ignored) {} // "super" fantasmas em construtores
        return new ArrayList<>(orderedCallees.values()); // why are we ordering the calls?
    }

    public static boolean existsAnnotation(List<CtAnnotation<? extends Annotation>> annotations, String annotationName) {
        for (CtAnnotation<? extends Annotation> a : annotations) {
            if ( a.getAnnotationType().getSimpleName().equals(annotationName) )
                return true;
        }
        return false;
    }

    public static void storeJsonFile(String filepath, JsonObject callSequence) {
        if (filepath != null) {
            try {
                FileWriter file = new FileWriter(filepath);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                file.write(gson.toJson(callSequence));
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
