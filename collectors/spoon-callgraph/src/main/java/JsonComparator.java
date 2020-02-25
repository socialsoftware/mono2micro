import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonComparator {

    private static String SPOONCALLGRAPHPATH = System.getProperty("user.dir") + "/" + "spoon_callSequence.json";
    //public static String JDTCALLGRAPHPATH = System.getProperty("user.dir") + "/../../data/icsa2020/collection/plugin-eclipse/ldod.json";
    public static String JDTCALLGRAPHPATH = "/home/samuel/ProjetoTese/edition-ldod_callSequence_eclipse.json";

    public static void main(String[] args) throws FileNotFoundException {
        Gson gson = new Gson();

        LinkedTreeMap spoon = (LinkedTreeMap) gson.fromJson(new FileReader(SPOONCALLGRAPHPATH), Object.class);
        LinkedTreeMap jdt = (LinkedTreeMap) gson.fromJson(new FileReader(JDTCALLGRAPHPATH), Object.class);

        for (Object jdtElement : jdt.entrySet()) {
            Map.Entry<String, ArrayList> jdtElement1 = (Map.Entry<String, ArrayList>) jdtElement;

            for (Object spoonElement : spoon.entrySet()) {
                Map.Entry<String, ArrayList> spoonElement1 = (Map.Entry<String, ArrayList>) spoonElement;

                if (jdtElement1.getKey().equals(spoonElement1.getKey())) {
                    if (jdtElement1.getValue().size() != spoonElement1.getValue().size()) {
                        System.out.println("Size\t - " + jdtElement1.getValue().size() + "/" + spoonElement1.getValue().size() + "\t" + jdtElement1.getKey());
                    }
                    else {
                        if (!jdtElement1.getValue().equals(spoonElement1.getValue())) {
                            System.out.println("Content\t - " + jdtElement1.getValue().size() + "\t" + jdtElement1.getKey());
                            int i = 0;
                            for (Object a : jdtElement1.getValue()) {
                                Object b = spoonElement1.getValue().get(i);
                                ArrayList a1 = (ArrayList) a;
                                ArrayList b1 = (ArrayList) b;

                                if (!a1.get(0).equals(b1.get(0)) ||
                                    !a1.get(1).equals(b1.get(1))) {
                                    System.out.println(a1.get(0) + "/" + b1.get(0));
                                }

                                i++;
                            }
                        }
                    }
                }
            }
        }
    }
}
