package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Utils {

    public static Integer lineno() { return new Throwable().getStackTrace()[1].getLineNumber(); }

    public static void print(String message, Integer lineNumber) { System.out.println("[" + lineNumber + "] " + message); }

    public static Set<String> getJsonFileKeys(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonfactory = mapper.getFactory();

        JsonParser jsonParser = jsonfactory.createParser(is);
        JsonToken jsonToken = jsonParser.nextValue(); // JsonToken.START_OBJECT

        if (jsonToken != JsonToken.START_OBJECT) {
            System.err.println("Json must start with a left curly brace");
            System.exit(-1);
        }

        Set<String> keys = new HashSet<>();

        jsonParser.nextValue();

        while (jsonToken != JsonToken.END_OBJECT) {
            if (jsonParser.getCurrentName() != null) {
                String keyName = jsonParser.getCurrentName();
                System.out.println("Key name: " + keyName);
                keys.add(keyName);
                jsonParser.skipChildren();
            }

            jsonToken = jsonParser.nextValue();
        }

        is.close();

        return keys;
    }

    // FIXME better name for this function pls
    public static void  fillEntityDataStructures(
        Map<String,List<Pair<String,String>>> entityControllers,
        Map<String,Integer> e1e2PairCount,
        List<AccessDto> accessesList,
        String controllerName
    ) {
        System.out.println("Filling entity data structures...");

        for (int i = 0; i < accessesList.size(); i++) {
            AccessDto access = accessesList.get(i);
            String entity = access.getEntity();
            String mode = access.getMode();

            if (entityControllers.containsKey(entity)) {
                boolean containsController = false;

                for (Pair<String, String> controllerPair : entityControllers.get(entity)) {
                    if (controllerPair.getFirst().equals(controllerName)) {
                        containsController = true;

                        if (!controllerPair.getSecond().contains(mode))
                            controllerPair.setSecond("RW");
                        break;
                    }
                }

                if (!containsController) {
                    entityControllers.get(entity).add(new Pair<>(controllerName, mode));
                }

            } else {
                List<Pair<String, String>> controllersPairs = new ArrayList<>();
                controllersPairs.add(new Pair<>(controllerName, mode));
                entityControllers.put(entity, controllersPairs);
            }

            if (i < accessesList.size() - 1) {
                AccessDto nextAccess = accessesList.get(i + 1);
                String nextEntity = nextAccess.getEntity();

                if (!entity.equals(nextEntity)) {
                    String e1e2 = entity + "->" + nextEntity;
                    String e2e1 = nextEntity + "->" + entity;

                    int count = e1e2PairCount.getOrDefault(e1e2, 0);
                    e1e2PairCount.put(e1e2, count + 1);

                    count = e1e2PairCount.getOrDefault(e2e1, 0);
                    e1e2PairCount.put(e2e1, count + 1);
                }
            }
        }
    }

    public static int getMaxNumberOfPairs(Map<String,Integer> e1e2PairCount) {
        if (!e1e2PairCount.values().isEmpty())
            return Collections.max(e1e2PairCount.values());
        else
            return 0;
    }

    public static float[] calculateSimilarityMatrixMetrics(
        Map<String,List<Pair<String,String>>> entityControllers,
        Map<String,Integer> e1e2PairCount,
        String e1,
        String e2,
        int maxNumberOfPairs
    ) {

        float inCommon = 0;
        float inCommonW = 0;
        float inCommonR = 0;
        float e1ControllersW = 0;
        float e1ControllersR = 0;

        for (Pair<String,String> e1Controller : entityControllers.get(e1)) {
            for (Pair<String,String> e2Controller : entityControllers.get(e2)) {
                if (e1Controller.getFirst().equals(e2Controller.getFirst()))
                    inCommon++;
                if (e1Controller.getFirst().equals(e2Controller.getFirst()) && e1Controller.getSecond().contains("W") && e2Controller.getSecond().contains("W"))
                    inCommonW++;
                if (e1Controller.getFirst().equals(e2Controller.getFirst()) && e1Controller.getSecond().contains("R") && e2Controller.getSecond().contains("R"))
                    inCommonR++;
            }
            if (e1Controller.getSecond().contains("W"))
                e1ControllersW++;
            if (e1Controller.getSecond().contains("R"))
                e1ControllersR++;
        }

        float accessMetric = inCommon / entityControllers.get(e1).size();
        float writeMetric = e1ControllersW == 0 ? 0 : inCommonW / e1ControllersW;
        float readMetric = e1ControllersR == 0 ? 0 : inCommonR / e1ControllersR;

        String e1e2 = e1 + "->" + e2;
        float e1e2Count = e1e2PairCount.getOrDefault(e1e2, 0);

        float sequenceMetric;

        if (maxNumberOfPairs != 0)
            sequenceMetric = e1e2Count / maxNumberOfPairs;
        else // nao ha controladores a aceder a mais do que uma entidade
            sequenceMetric = 0;

        return new float[] {
            accessMetric,
            writeMetric,
            readMetric,
            sequenceMetric
        };
    }
}
