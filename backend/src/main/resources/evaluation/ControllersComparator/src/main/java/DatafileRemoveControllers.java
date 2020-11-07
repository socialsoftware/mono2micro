import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatafileRemoveControllers {

    private static String CODEBASE_PATH = "/home/samuel/ProjetoTese/mono2micro/backend/src/main/resources/codebases/bwRebaixadoADinamicaExpert/";

    public static void main(String[] args) throws IOException {
        List<String> list = new ArrayList<String>();
        list.add("GoalModelController.cleanGoalModel");
        list.add("ConditionModelController.createAttributeAchieveCondition");
        list.add("SpecificationController.createSpec");
        list.add("GoalModelController.associateDefPathConditionToGoalAct");
        list.add("ActivityModelController.associateDefPathToActivityPre");
        list.add("ConditionModelController.cleanConditionModel");
        list.add("ActivityModelController.associateEntityAchieveConditionToActivityPost");
        list.add("DataModelController.getProduct");
        list.add("ConditionModelController.createEntityDependenceCondition");
        list.add("ActivityModelController.createActivity");
        list.add("ActivityModelController.associateMultiplicityToActivityPost");
        list.add("ConditionModelController.createEntityInvariantCondition");
        list.add("GoalModelController.associateEntityAchieveConditionToGoalSuc");
        list.add("ActivityModelController.addActivity");
        list.add("InstanceController.getMandatoryEntityInstance");
        list.add("GoalModelController.associateAttributeAchieveConditionToGoalSuc");
        list.add("ActivityModelController.cleanActivityModel");
        list.add("ConditionModelController.createAttributeInvariantCondition");
        list.add("ActivityModelController.checkActivityModel");
        list.add("ActivityModelController.associateAttributeInvariantConditionActivityPost");
        list.add("ActivityModelController.associateAttributeAchieveConditionToActivityPost");
        list.add("SpecificationController.deleteSpecification");
        list.add("ExternalIdAPIController.getEntityInstanceByExternalId");
        list.add("DataModelController.deleteDependence");
        list.add("ExternalIdAPIController.getEntityInstancesForDependence");
        list.add("ExportController.exportSpecification");
        list.add("GoalModelController.associateAttributeInvariantConditionToGoal");
        list.add("ConditionModelController.createEntityAchieveCondition");
        list.add("GoalModelController.getGoalByName");
        list.add("ConditionModelController.createAttributeDependenceCondition");
        list.add("DataModelController.getAttributeByExtId");
        list.add("DataModelController.getEntityByName");
        list.add("GoalModelController.associateEntityInvariantConditionToGoal");


        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, List<List<String>>> datafile = mapper.reader().readValue(
                new File(CODEBASE_PATH + "datafile.json"),
                HashMap.class
        );

        for (String controller : list) {
            datafile.remove(controller);
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(
                new File(CODEBASE_PATH + "changedDatafile.json"),
                datafile);

    }
}
