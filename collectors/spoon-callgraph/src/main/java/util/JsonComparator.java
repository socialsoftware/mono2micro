package util;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;

public class JsonComparator {

    private static String TREE1 = System.getProperty("user.dir") + "/" + "spoon_callSequence_bw2.json";
    private static String TREE2 = System.getProperty("user.dir") + "/" + "bw_nuno.json";

    public static void main(String[] args) throws FileNotFoundException {
        Gson gson = new Gson();

        LinkedTreeMap tree1 = (LinkedTreeMap) gson.fromJson(new FileReader(TREE1), Object.class);
        LinkedTreeMap tree2 = (LinkedTreeMap) gson.fromJson(new FileReader(TREE2), Object.class);

        for (Object tree1ElementObject : tree1.entrySet()) {
            Map.Entry<String, ArrayList> tree1Element1 = (Map.Entry<String, ArrayList>) tree1ElementObject;

            boolean found = false;
//            System.out.println(tree1Element1.getKey());

            for (Object tree2ElementObject : tree2.entrySet()) {
                Map.Entry<String, ArrayList> tree2Element = (Map.Entry<String, ArrayList>) tree2ElementObject;

                if (tree1Element1.getKey().equals(tree2Element.getKey())) {
                    found = true;
                    if (tree1Element1.getValue().size() != tree2Element.getValue().size()) {
                        System.out.println("Size\t - " + tree1Element1.getValue().size() + "/" + tree2Element.getValue().size() + "\t" + tree1Element1.getKey());
                    }
                    else {
                        if (!tree1Element1.getValue().equals(tree2Element.getValue())) {
                            System.out.println("Content\t - " + tree1Element1.getValue().size() + "\t" + tree1Element1.getKey());
                            int i = 0;
                            for (Object a : tree1Element1.getValue()) {
                                Object b = tree2Element.getValue().get(i);
                                ArrayList a1 = (ArrayList) a;
                                ArrayList b1 = (ArrayList) b;

                                if (!a1.get(0).equals(b1.get(0)) ||
                                    !a1.get(1).equals(b1.get(1))) {
//                                    System.out.println(a1.get(0) + "/" + b1.get(0));
                                }

                                i++;
                            }
                        }
                    }
                }
            }
            if (found = false) {
                System.out.println("");
            }
        }
    }
}
