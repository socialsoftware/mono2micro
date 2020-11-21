import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entities {

    private static String CODEBASE_PATH = "/home/samuel/ProjetoTese/mono2micro/backend/src/main/resources/codebases/bwRebaixadoADynamicExpert/";

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, List<List<String>>> datafile = mapper.reader().readValue(
                new File(CODEBASE_PATH + "datafile.json"),
                HashMap.class
        );

        HashMap<String, HashMap<String, String>> controllerEntities = new HashMap<String, HashMap<String, String>>();

        for (Map.Entry<String, List<List<String>>> controllerEntry : datafile.entrySet()) {
            String controllerName = controllerEntry.getKey();

            HashMap<String, String> entityToModeMap = new HashMap<String, String>();

            List<List<String>> accessesList = controllerEntry.getValue();
            int count = 0;
            for (List<String> access : accessesList) {
                System.out.println(count++);
                String entityName = access.get(0);
                String mode = access.get(1);

                if (!entityToModeMap.containsKey(entityName))
                    entityToModeMap.put(entityName, "");

                String modes = entityToModeMap.get(entityName);
                if (modes.length() == 0) {
                    modes = mode;
                }
                else {
                    if (!modes.contains(mode)) {
                        modes = "RW";
                    }
                }

                entityToModeMap.put(entityName, modes);
            }

            controllerEntities.put(controllerName, entityToModeMap);
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(
                new File(CODEBASE_PATH + "controllerEntitiesWholeDatafile.json"),
                controllerEntities);
    }
}
