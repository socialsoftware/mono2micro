package pt.ist.socialsoftware.mono2micro.domain;

import org.json.JSONArray;
import org.json.JSONException;
import pt.ist.socialsoftware.mono2micro.exceptions.BadConstructedRedesignException;
import pt.ist.socialsoftware.mono2micro.utils.LocalTransactionTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionalityRedesign {

    private String name;
    private List<LocalTransaction> redesign = new ArrayList<>();
    private int systemComplexity;
    private int functionalityComplexity;
    private String pivotTransaction = "";

    public FunctionalityRedesign(){}

    public FunctionalityRedesign(String name){
        this.name = name;
    }

    public int getSystemComplexity() {
        return systemComplexity;
    }

    public void setSystemComplexity(int systemComplexity) {
        this.systemComplexity = systemComplexity;
    }

    public int getFunctionalityComplexity() {
        return functionalityComplexity;
    }

    public void setFunctionalityComplexity(int functionalityComplexity) {
        this.functionalityComplexity = functionalityComplexity;
    }

    public String getPivotTransaction() {
        return pivotTransaction;
    }

    public void setPivotTransaction(String pivotTransaction) {
        this.pivotTransaction = pivotTransaction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LocalTransaction> getRedesign() {
        return redesign;
    }

    public void setRedesign(List<LocalTransaction> redesign) {
        this.redesign = redesign;
    }

    public List<LocalTransaction> addCompensating(String clusterName, String entities, String fromID) {

        List<Integer> usedIDs = this.redesign.stream().map(lt -> Integer.parseInt(lt.getId())).collect(Collectors.toList());
        int i;
        for(i = 0; i < this.redesign.size(); i++){
            if(!usedIDs.contains(i))
                break;
        }
        LocalTransaction newLT = new LocalTransaction(String.valueOf(i),clusterName,entities,new ArrayList<Integer>(), i + ": " + clusterName);
        LocalTransaction caller = this.redesign.stream().filter(lt -> lt.getId().equals(fromID)).findFirst().orElse(null);

        if(caller != null){
            this.redesign.add(newLT);
            caller.getRemoteInvocations().add(i);
            return this.redesign;
        } else {
            return null;
        }
    }

    public List<LocalTransaction> sequenceChange(String localTransactionID, String newCaller) {
        LocalTransaction oldCallerLT = this.redesign.stream().filter(lt -> lt.getRemoteInvocations().contains(Integer.parseInt(localTransactionID)))
                .findFirst().orElse(null);
        LocalTransaction newCallerLT = this.redesign.stream().filter(lt -> lt.getId().equals(newCaller))
                .findFirst().orElse(null);

        if(oldCallerLT != null && newCallerLT != null){
            oldCallerLT.getRemoteInvocations().remove((Integer) Integer.parseInt(localTransactionID));
            newCallerLT.getRemoteInvocations().add(Integer.parseInt(localTransactionID));
            return this.redesign;
        } else {
            return null;
        }
    }

    public List<LocalTransaction> dcgi(String fromCluster, String toCluster, String localTransactions) throws JSONException {
        JSONArray lts = new JSONArray(localTransactions);

        HashMap<String, String> fromAccessedEntities = new HashMap<>();
        HashMap<String, String> toAccessedEntities = new HashMap<>();
        List<Integer> ltsBeingMergedIDs = new ArrayList<>();

        for(int i = 0; i < lts.length(); i++){
            int id = Integer.parseInt(lts.getString(i));
            ltsBeingMergedIDs.add(id);

            LocalTransaction lt = this.redesign.stream().filter(e -> e.getId().equals(String.valueOf(id))).findFirst().orElse(null);
            if(lt != null){
                JSONArray sequence = new JSONArray(lt.getAccessedEntities());

                for(int j=0; j < sequence.length(); j++){
                    if(lt.getCluster().equals(fromCluster)){
                        if(!fromAccessedEntities.containsKey(sequence.getJSONArray(j).getString(0))){
                            fromAccessedEntities.put(sequence.getJSONArray(j).getString(0), sequence.getJSONArray(j).getString(1));
                        } else if(!fromAccessedEntities.get(sequence.getJSONArray(j).getString(0)).contains(sequence.getJSONArray(j).getString(1))){
                            fromAccessedEntities.put(sequence.getJSONArray(j).getString(0), "RW");
                        }
                    }
                    else if (lt.getCluster().equals(toCluster)){
                        if(!toAccessedEntities.containsKey(sequence.getJSONArray(j).getString(0))){
                            toAccessedEntities.put(sequence.getJSONArray(j).getString(0), sequence.getJSONArray(j).getString(1));
                        } else if(!toAccessedEntities.get(sequence.getJSONArray(j).getString(0)).contains(sequence.getJSONArray(j).getString(1))){
                            toAccessedEntities.put(sequence.getJSONArray(j).getString(0), "RW");
                        }
                    }
                }
            }
        }

        List<Integer> fromRemoteInvocations = new ArrayList<>();
        List<Integer> toRemoteInvocations = new ArrayList<>();

        for (Integer i : ltsBeingMergedIDs) {
            LocalTransaction lt = this.redesign.stream().filter(e -> e.getId().equals(String.valueOf(i))).findFirst().orElse(null);
            if(lt != null){
                for (Integer integer : lt.getRemoteInvocations()) {
                    if(!ltsBeingMergedIDs.contains(integer))
                        toRemoteInvocations.add(integer);
                }
                this.redesign.remove(lt);
            }
        }

        int min = ltsBeingMergedIDs.get(0);
        int secondMin = ltsBeingMergedIDs.get(1);
        boolean secondMinFlag = false;

        List<Integer> usedIDs = this.redesign.stream().map(lt -> Integer.parseInt(lt.getId())).collect(Collectors.toList());
        int i;
        for(i = 0; i < this.redesign.size(); i++){
            if(!secondMinFlag) {
                if (!usedIDs.contains(i)) {
                    min = i;
                    secondMinFlag = true;
                }
            } else {
                if (!usedIDs.contains(i)) {
                    secondMin = i;
                    break;
                }
            }
        }

        for (LocalTransaction lt : this.redesign) {
            for (Integer ri : lt.getRemoteInvocations()) {
                if(ltsBeingMergedIDs.contains(ri)){
                    lt.getRemoteInvocations().remove(ri);
                    lt.getRemoteInvocations().add(min);
                }
            }
        }

        fromRemoteInvocations.add(secondMin);
        LocalTransaction newFromLT = new LocalTransaction(String.valueOf(min),fromCluster,constructSequence(fromAccessedEntities),fromRemoteInvocations, min + ": " + fromCluster);
        LocalTransaction newToLT = new LocalTransaction(String.valueOf(secondMin),toCluster,constructSequence(toAccessedEntities),toRemoteInvocations, secondMin + ": " + toCluster);
        this.redesign.add(newFromLT);
        this.redesign.add(newToLT);

        return this.redesign;
    }

    private String constructSequence(HashMap<String, String> hashMapSequence){
        StringBuilder sequence = new StringBuilder();
        sequence.append("[");
        for (String s : hashMapSequence.keySet()) {
            sequence.append("[\"").append(s).append("\",\"").append(hashMapSequence.get(s)).append("\"],");
        }
        sequence.deleteCharAt(sequence.length() - 1).append("]");
        return sequence.toString();
    }


    public void definePivotTransaction(String pivotID) throws JSONException {
        pivotID = checkForRemoteInvocationsValidity(pivotID);
        this.setPivotTransaction(pivotID);

        List<LocalTransaction> localTransactionsSequence = new ArrayList<>();
        for (LocalTransaction lt : this.redesign) {
            if(lt.getId().equals(String.valueOf(-1))){
                localTransactionsSequence.add(lt);
            }
        }

        List<String> retriableTouchedEntities = new ArrayList<>();
        List<String> compensatableTouchedEntities = new ArrayList<>();

        List<LocalTransaction> retriableLTs = new ArrayList<>();
        for(int i = 0; i < localTransactionsSequence.size(); i++){
            LocalTransaction lt = localTransactionsSequence.get(i);

            for (Integer id : lt.getRemoteInvocations()) {
                for (LocalTransaction localTransaction : this.redesign) {
                    if(localTransaction.getId().equals(String.valueOf(id)))
                        localTransactionsSequence.add(localTransaction);
                }
            }

            if(lt.getId().equals(pivotID)) {
                lt.setType(LocalTransactionTypes.PIVOT);
                for (Integer id : lt.getRemoteInvocations()) {
                    for (LocalTransaction localTransaction : this.redesign) {
                        if(localTransaction.getId().equals(String.valueOf(id))){
                            retriableLTs.add(localTransaction);
                        }
                    }
                }
                identifyTouchedEntities(lt, retriableTouchedEntities);
            }
            else {
                lt.setType(LocalTransactionTypes.COMPENSATABLE);
                if(!lt.getId().equals(Integer.toString(-1)))
                    identifyTouchedEntities(lt, compensatableTouchedEntities);
            }
        }

        for (int i = 0; i < retriableLTs.size(); i++) {
            LocalTransaction lt = retriableLTs.get(i);
            for (Integer id : lt.getRemoteInvocations()) {
                for (LocalTransaction localTransaction : this.redesign) {
                    if(localTransaction.getId().equals(String.valueOf(id))){
                        retriableLTs.add(localTransaction);
                    }
                }
            }
            lt.setType(LocalTransactionTypes.RETRIABLE);
            identifyTouchedEntities(lt, retriableTouchedEntities);
        }

        /*if(!retriableTouchedEntities.containsAll(compensatableTouchedEntities)){
            for (LocalTransaction lt : this.redesign) {
                lt.setType(LocalTransactionTypes.COMPENSATABLE);
            }
            throw new BadConstructedRedesignException("In the specified redesign there are entities that are written in " +
                    "compensatable transactions that are not updated in a retriable transaction");
        }*/
    }

    private void identifyTouchedEntities(LocalTransaction lt, List<String> touchedEntities) throws JSONException {
        JSONArray sequence = new JSONArray(lt.getAccessedEntities());
        for(int j=0; j < sequence.length(); j++){
            if(sequence.getJSONArray(j).getString(1).contains("W") &&
                    !touchedEntities.contains(sequence.getJSONArray(j).getString(0)))
                touchedEntities.add(sequence.getJSONArray(j).getString(0));
        }
    }

    private String checkForRemoteInvocationsValidity(String pivotID) throws JSONException {
        String newPivotID = pivotID;
        for (int i = 0; i < this.redesign.size(); i++) {
            LocalTransaction lt1 = this.redesign.get(i);
            for(Integer id : lt1.getRemoteInvocations()){
                LocalTransaction lt2 = this.redesign.stream().filter(e -> e.getId().equals(String.valueOf(id))).findFirst().orElse(null);

                if(lt2 != null){
                    if(lt1.getCluster().equals(lt2.getCluster())){
                        String newID = mergeTwoLocalTransactions(lt1, lt2);

                        if(lt1.getId().equals(pivotID) || lt2.getId().equals(pivotID))
                            newPivotID = newID;
                    }
                }
            }
        }
        return newPivotID;
    }

    private String mergeTwoLocalTransactions(LocalTransaction lt1, LocalTransaction lt2) throws JSONException {
        String min = Integer.parseInt(lt1.getId()) <= Integer.parseInt(lt2.getId()) ? lt1.getId() : lt2.getId();

        HashMap<String, String> accessedEntities = new HashMap<>();

        JSONArray sequence = new JSONArray(lt1.getAccessedEntities());
        for(int j=0; j < sequence.length(); j++){
            accessedEntities.put(sequence.getJSONArray(j).getString(0), sequence.getJSONArray(j).getString(1));
        }

        sequence = new JSONArray(lt2.getAccessedEntities());
        for(int j=0; j < sequence.length(); j++){
            if(!accessedEntities.containsKey(sequence.getJSONArray(j).getString(0))){
                accessedEntities.put(sequence.getJSONArray(j).getString(0), sequence.getJSONArray(j).getString(1));
            } else if(!accessedEntities.get(sequence.getJSONArray(j).getString(0)).contains(sequence.getJSONArray(j).getString(1))){
                accessedEntities.put(sequence.getJSONArray(j).getString(0), "RW");
            }
        }

        List<Integer> remoteInvocations = new ArrayList<>();
        for(Integer id : lt1.getRemoteInvocations()){
            if(id != Integer.parseInt(lt2.getId()))
                remoteInvocations.add(id);
        }
        remoteInvocations.addAll(lt2.getRemoteInvocations());

        for(LocalTransaction lt : this.redesign){
            if(lt.getRemoteInvocations().contains(Integer.parseInt(lt1.getId()))){
                lt.getRemoteInvocations().remove((Integer) Integer.parseInt(lt1.getId()));
                lt.getRemoteInvocations().add(Integer.parseInt(min));
            }
        }

        LocalTransaction newLT = new LocalTransaction(min,
                lt1.getCluster(),
                constructSequence(accessedEntities),
                remoteInvocations,
                min + ": " + lt1.getCluster());

        for(int i = 0; i < this.redesign.size() ; i++){
            if(this.redesign.get(i).getId().equals(lt1.getId())) {
                this.redesign.remove(i);
                continue;
            }
            if(this.redesign.get(i).getId().equals(lt2.getId())) {
                this.redesign.remove(i);
            }
        }

        this.redesign.add(newLT);
        return min;
    }


    public List<String> semanticLockEntities() throws JSONException {
        if(this.pivotTransaction.equals(""))
            return new ArrayList<>();

        List<String> slEntities = new ArrayList<>();
        for (LocalTransaction lt : this.redesign) {
            if(!lt.getId().equals(String.valueOf(-1))) {
                if (lt.getType() == LocalTransactionTypes.COMPENSATABLE) {
                    JSONArray sequence = new JSONArray(lt.getAccessedEntities());
                    for (int j = 0; j < sequence.length(); j++) {
                        String entity = sequence.getJSONArray(j).getString(0);
                        String accessMode = sequence.getJSONArray(j).getString(1);

                        if (accessMode.contains("W")) {
                            slEntities.add(entity);
                        }
                    }
                }
            }
        }
        return slEntities;
    }

    public void changeLTName(String ltID, String newName){
        for(LocalTransaction lt : this.redesign){
            if(lt.getId().equals(ltID)){
                lt.setName(newName);
            }
        }
    }
}
