package util;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class JsonComparator {

    private static String PATH1 = System.getProperty("user.dir") + "/data/collection";
    private static String PATH2 = System.getProperty("user.dir") + "/data/collection/Untitled Folder";

    public static void main(String[] args) throws FileNotFoundException {
        Gson gson = new Gson();

        File file = new File(PATH1);
        String[] list = file.list();
        for (String filename : list) {
            if (filename.contains("Untitled"))
                continue;
            System.out.println(filename);

            LinkedTreeMap tree1 = (LinkedTreeMap) gson.fromJson(new FileReader(new File(PATH1, filename)), Object.class);
            LinkedTreeMap tree2 = (LinkedTreeMap) gson.fromJson(new FileReader(new File(PATH2, filename)), Object.class);

            for (Object tree1ElementObject : tree1.entrySet()) {
                Map.Entry<String, ArrayList> tree1Element = (Map.Entry<String, ArrayList>) tree1ElementObject;

                Set<String> read1 = new HashSet<>();
                Set<String> read2 = new HashSet<>();
                Set<String> write1 = new HashSet<>();
                Set<String> write2 = new HashSet<>();
                boolean found = false;

                for (Object tree2ElementObject : tree2.entrySet()) {
                    Map.Entry<String, ArrayList> tree2Element = (Map.Entry<String, ArrayList>) tree2ElementObject;

                    if (tree1Element.getKey().equals(tree2Element.getKey())) {
                        System.out.println(tree1Element.getKey());
                        found = true;
                        for (Object a : tree1Element.getValue()) {
                            ArrayList a1 = (ArrayList) a;
                            if (a1.get(1).equals("R"))
                                read1.add((String) a1.get(0));
                            else if (a1.get(1).equals("W"))
                                write1.add((String) a1.get(0));
                        }
                        for (Object a : tree2Element.getValue()) {
                            ArrayList a1 = (ArrayList) a;
                            if (a1.get(1).equals("R"))
                                read2.add((String) a1.get(0));
                            else if (a1.get(1).equals("W"))
                                write2.add((String) a1.get(0));
                        }
                        ArrayList<String> read1Copy = new ArrayList<>(read1);
                        read1.removeAll(read2);
                        read2.removeAll(read1Copy);

                        ArrayList<String> write1Copy = new ArrayList<>(write1);
                        write1.removeAll(write2);
                        write2.removeAll(write1Copy);

                        if (read1.size() > 0)
                            System.out.println("Read 1 = " + Arrays.toString(read1.toArray()));
                        if (read2.size() > 0)
                            System.out.println("Read 2 = " + Arrays.toString(read2.toArray()));
                        if (write1.size() > 0)
                            System.out.println("Write 1 = " + Arrays.toString(write1.toArray()));
                        if (write2.size() > 0)
                            System.out.println("Write 2 = " + Arrays.toString(write2.toArray()));
                        break;
                    }
                }
                if (!found) {
                    System.out.println("New Controller: " + tree1Element.getKey());
                }
            }
        }

    }
}
