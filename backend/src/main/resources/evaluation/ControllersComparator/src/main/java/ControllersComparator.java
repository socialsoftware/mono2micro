import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ControllersComparator {

    private static String CONTROLLER_JSON_PATH_1 = "/home/samuel/ProjetoTese/mono2micro/backend/src/main/resources/codebases/ldodStatic-LdoDExpertUsage/controllerEntities.json"; // static
    private static String CONTROLLER_JSON_PATH_2 = "/home/samuel/ProjetoTese/mono2micro/backend/src/main/resources/codebases/LdoD-ExpertUsage-5fragments/controllerEntities.json"; // dynamic

    private void run() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, HashMap<String, String>> staticJson = mapper.reader().readValue(new File(CONTROLLER_JSON_PATH_1), HashMap.class);
        HashMap<String, HashMap<String, String>> dynamicJson = mapper.reader().readValue(new File(CONTROLLER_JSON_PATH_2), HashMap.class);

        float sumEntityCoverageOfStatic = 0;
        float sumEntityCoverageOfDynamic = 0;
        int commonControllerCount = 0;

        Set<Map.Entry<String, HashMap<String, String>>> staticJsonEntry = staticJson.entrySet();
        for (Map.Entry<String, HashMap<String, String>> staticController : staticJsonEntry) {
            System.out.println(staticController.getKey());
            HashMap<String, String> staticControllerEntities = staticController.getValue();
            HashMap<String, String> dynamicControllerEntities = dynamicJson.get(staticController.getKey());

            if (dynamicControllerEntities != null) {
                // ver diferenças entre entidades
                for (Map.Entry<String, String> staticControllerEntity : staticControllerEntities.entrySet()) {
                    String staticControllerEntityName = staticControllerEntity.getKey();
                    String dynamicControllerEntityMode = dynamicControllerEntities.get(staticControllerEntityName);

                    if (dynamicControllerEntityMode != null) {
                        // see diferences between modes
                        boolean alreadyPrintedEntity = false;

                        String staticControllerEntityMode = staticControllerEntity.getValue();

                        String dynamicControllerEntityModeCopy = dynamicControllerEntityMode;
                        for (int i = 0 ; i < staticControllerEntityMode.length(); i++) {
                            char mode = staticControllerEntityMode.charAt(i);
                            dynamicControllerEntityModeCopy = dynamicControllerEntityModeCopy.replace(
                                    String.valueOf(mode),
                                    ""
                            );
                        }

                        String staticControllerEntityModeCopy = staticControllerEntityMode;
                        for (int i = 0 ; i < dynamicControllerEntityMode.length(); i++) {
                            char mode = dynamicControllerEntityMode.charAt(i);
                            staticControllerEntityModeCopy = staticControllerEntityModeCopy.replace(
                                    String.valueOf(mode),
                                    ""
                            );
                        }

                        if (staticControllerEntityModeCopy.length() > 0) {
                            // static has more modes than dynamic

                            System.out.print(staticControllerEntityName + " ");
                            alreadyPrintedEntity = true;

                            System.out.print("s: ");
                            System.out.print(staticControllerEntityModeCopy + "\t");
                        }

                        if (dynamicControllerEntityModeCopy.length() > 0) {
                            // dynamic has more modes than static

                            if (!alreadyPrintedEntity)
                                System.out.print(staticControllerEntityName + " ");

                            System.out.print("d: ");
                            System.out.print(dynamicControllerEntityModeCopy);
                        }

                        if (staticControllerEntityModeCopy.length() > 0 || dynamicControllerEntityModeCopy.length() > 0)
                            System.out.println();
                    }
                }

                System.out.println("Entities that Static has and Dynamic hasn't:");
                ArrayList<String> dynamicMissingEntities = new ArrayList<String>(staticControllerEntities.keySet());
                dynamicMissingEntities.removeAll(dynamicControllerEntities.keySet());
                System.out.println(Arrays.toString(dynamicMissingEntities.toArray()));

                System.out.println("Entities that Dynamic has and Static hasn't:");
                ArrayList<String> staticMissingEntities = new ArrayList<String>(dynamicControllerEntities.keySet());
                staticMissingEntities.removeAll(staticControllerEntities.keySet());
                System.out.println(Arrays.toString(staticMissingEntities.toArray()));

                int totalEntities = staticMissingEntities.size() + staticControllerEntities.keySet().size();
                float percentageCoverageEntitiesStatic = (float) staticControllerEntities.keySet().size() / totalEntities;
                float percentageCoverageEntitiesDynamic = (float) dynamicControllerEntities.keySet().size() / totalEntities;
                sumEntityCoverageOfStatic += percentageCoverageEntitiesStatic;
                sumEntityCoverageOfDynamic += percentageCoverageEntitiesDynamic;
                commonControllerCount++;
            }
        }

        System.out.println("Controllers that Static has and Dynamic hasn't:");
        ArrayList<String> dynamicMissingControllers = new ArrayList<String>(staticJson.keySet());
        dynamicMissingControllers.removeAll(dynamicJson.keySet());
        System.out.println(Arrays.toString(dynamicMissingControllers.toArray()));

        System.out.println("Controllers that Dynamic has and Static hasn't:");
        ArrayList<String> staticMissingControllers = new ArrayList<String>();
        for (Map.Entry<String, HashMap<String, String>> entry : dynamicJson.entrySet()) {
            if (entry.getValue().keySet().size() > 0) {
                staticMissingControllers.add(entry.getKey());
            }
        }

        staticMissingControllers.removeAll(staticJson.keySet());
        System.out.println(Arrays.toString(staticMissingControllers.toArray()));

        int totalControllers = staticMissingControllers.size() + staticJson.keySet().size();


        System.out.println("Average percentage of coveraged entities (static):");
        System.out.println(sumEntityCoverageOfStatic / commonControllerCount);
        System.out.println("Average percentage of coveraged entities (dynamic):");
        System.out.println(sumEntityCoverageOfDynamic / commonControllerCount);

        System.out.println("Percentage of coveraged controllers (static):"); // so considera os controladores que têm acessos
        System.out.println(1 - ((float) staticMissingControllers.size() / totalControllers));

        System.out.println("Percentage of coveraged controllers (dynamic):"); // so considera os controladores que têm acessos
        System.out.println(1 - ((float) dynamicMissingControllers.size() / totalControllers));
    }

    public static void main(String[] args) throws IOException {
        new ControllersComparator().run();
    }
}
