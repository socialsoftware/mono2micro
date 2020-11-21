package pt.ist.socialsoftware.mono2micro.domain;

import org.json.JSONArray;
import org.json.JSONException;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.exceptions.BadConstructedRedesignException;
import pt.ist.socialsoftware.mono2micro.utils.LocalTransactionTypes;

import java.util.*;
import java.util.stream.Collectors;

public class FunctionalityRedesign {

    private String name;
    private boolean usedForMetrics;
    private List<LocalTransaction> redesign = new ArrayList<>();
    private int systemComplexity;
    private int functionalityComplexity;
    private int inconsistencyComplexity;
    private int pivotTransaction = -1;

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

    public int getPivotTransaction() {
        return pivotTransaction;
    }

    public void setPivotTransaction(int pivotTransaction) {
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

    public List<LocalTransaction> addCompensating(
        String clusterName,
        List<Integer> entities,
        int fromID
    )
        throws Exception
    {
        List<Integer> usedIDs = this.redesign.stream().map(LocalTransaction::getId).collect(Collectors.toList());

        int i;

        for(i = 0; i < this.redesign.size(); i++){
            if(!usedIDs.contains(i))
                break;
        }

        Set<AccessDto> accesses = entities.stream().map(e -> {
            AccessDto accessDto = new AccessDto();
            accessDto.setEntityID((e.shortValue()));
            accessDto.setMode((byte) 2);
            return  accessDto;
        }).collect(Collectors.toSet());

        LocalTransaction newLT = new LocalTransaction(
            i,
            Short.parseShort(clusterName),
            accesses,
            new ArrayList<>(),
            i + ": " + clusterName
        );

        LocalTransaction caller = this.redesign
            .stream()
            .filter(lt -> lt.getId() == fromID)
            .findFirst()
            .orElseThrow(() -> new Exception("Transaction with id " + fromID + " not found"));

        this.redesign.add(newLT);
        caller.getRemoteInvocations().add(i);

        return this.redesign;
    }

    public List<LocalTransaction> sequenceChange(
        String localTransactionID,
        String newCaller
    )
        throws Exception
    {
        LocalTransaction oldCallerLT = this.redesign
            .stream()
            .filter(lt -> lt.getRemoteInvocations().contains(Integer.parseInt(localTransactionID)))
            .findFirst()
            .orElseThrow(() -> new Exception("Transaction with id " + localTransactionID + " not found"));

        LocalTransaction newCallerLT = this.redesign
            .stream()
            .filter(lt -> lt.getId() == Integer.parseInt(newCaller))
            .findFirst()
            .orElseThrow(() -> new Exception("Transaction with id " + newCaller + " not found"));

        oldCallerLT.getRemoteInvocations().remove((Integer) Integer.parseInt(localTransactionID));
        newCallerLT.getRemoteInvocations().add(Integer.parseInt(localTransactionID));

        return this.redesign;
    }

    public List<LocalTransaction> dcgi(
        String fromCluster,
        String toCluster,
        String localTransactions
    )
        throws Exception
    {
        short fromClusterID = Short.parseShort(fromCluster);
        short toClusterID = Short.parseShort(toCluster);

        JSONArray lts = new JSONArray(localTransactions);

        HashMap<Short, Byte> fromLTAccesses = new HashMap<>();
        HashMap<Short, Byte> toLTAccesses = new HashMap<>();
        // Mode: "R" -> 1, "W" -> 2, "RW" -> 3

        List<Integer> ltsBeingMergedIDs = new ArrayList<>();

        for(int i = 0; i < lts.length(); i++){
            int id = Integer.parseInt(lts.getString(i));
            ltsBeingMergedIDs.add(id);

            LocalTransaction lt = this.redesign
                .stream()
                .filter(e -> e.getId() == id)
                .findFirst()
                .orElseThrow(() -> new Exception("Transaction with id " + id + " not found"));

            Set<AccessDto> ltAccesses = lt.getClusterAccesses();

            for(AccessDto access : ltAccesses) {
                short accessedEntityID = access.getEntityID();
                byte accessedMode = access.getMode();

                if(lt.getClusterID() == fromClusterID) {
                    Byte mode = fromLTAccesses.get(accessedEntityID);

                    if(mode == null) { // map does not contain the entity
                        fromLTAccesses.put(accessedEntityID, accessedMode);
                    } else if(mode != accessedMode){
                        fromLTAccesses.put(accessedEntityID, (byte) 3); // 3 -> "RW"
                    }
                }

                else if (lt.getClusterID() == toClusterID){
                    Byte mode = toLTAccesses.get(accessedEntityID);

                    if(mode == null) { // map does not contain the entity
                        toLTAccesses.put(accessedEntityID, accessedMode);
                    } else if(mode != accessedMode){
                        toLTAccesses.put(accessedEntityID, (byte) 3); // 3 -> "RW"
                    }
                }
            }
        }

        List<Integer> fromRemoteInvocations = new ArrayList<>();
        List<Integer> toRemoteInvocations = new ArrayList<>();

        // Deleting the lts being merged

        for (Integer i : ltsBeingMergedIDs) {
            LocalTransaction lt = this.redesign
                .stream()
                .filter(e -> e.getId() == i)
                .findFirst()
                .orElseThrow(() -> new Exception("Transaction with id " + i + " not found"));

            for (Integer integer : lt.getRemoteInvocations()) {
                if(!ltsBeingMergedIDs.contains(integer))
                    toRemoteInvocations.add(integer);
            }
            this.redesign.remove(lt);
        }

        int min = ltsBeingMergedIDs.get(0);
        int secondMin = ltsBeingMergedIDs.get(1);
        boolean secondMinFlag = false;

        List<Integer> usedIDs = this.redesign
            .stream()
            .map(LocalTransaction::getId)
            .collect(Collectors.toList());

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

        LocalTransaction firstLT = firstLocalTransactionInAMerge(ltsBeingMergedIDs);
        firstLT.getRemoteInvocations().removeAll(ltsBeingMergedIDs);
        firstLT.getRemoteInvocations().add(min);

        for (LocalTransaction lt : this.redesign)
            if(lt.getId() != firstLT.getId())
                lt.getRemoteInvocations().removeIf(ltsBeingMergedIDs::contains);

        fromRemoteInvocations.add(secondMin);
        LocalTransaction newFromLT = new LocalTransaction(
            min,
            fromClusterID,
            constructSequence(fromLTAccesses),
            fromRemoteInvocations,
            min + ": " + fromCluster
        );

        LocalTransaction newToLT = new LocalTransaction(
            secondMin,
            toClusterID,
            constructSequence(toLTAccesses),
            toRemoteInvocations,
            secondMin + ": " + toCluster
        );

        this.redesign.add(newFromLT);
        this.redesign.add(newToLT);

        return this.redesign;
    }

    private LocalTransaction firstLocalTransactionInAMerge(List<Integer> ltsBeingMergedIDs){
        List<LocalTransaction> localTransactionsSequence = new ArrayList<>();
        for (LocalTransaction lt : this.redesign) {
            if(lt.getId() == 0) // ROOT
                localTransactionsSequence.add(lt);
        }

        for(int i = 0; i < localTransactionsSequence.size(); i++) {
            LocalTransaction lt = localTransactionsSequence.get(i);

            for (Integer id : lt.getRemoteInvocations()) {
                for (LocalTransaction localTransaction : this.redesign) {
                    if(localTransaction.getId() == id)
                        localTransactionsSequence.add(localTransaction);
                }
            }

            if(lt.getRemoteInvocations()
                .stream()
                .filter(ri -> ltsBeingMergedIDs.contains(ri))
                .findAny()
                .orElse(null) != null
            )
                return lt;
        }

        return null;
    }

    private Set<AccessDto> constructSequence(HashMap<Short, Byte> hashMapSequence){
        Set<AccessDto> accesses = new HashSet<>();

        for (Short s : hashMapSequence.keySet()) {
            AccessDto access = new AccessDto();

            access.setEntityID(s);
            access.setMode(hashMapSequence.get(s));

            accesses.add(access);
        }

        return accesses;
    }


    public void definePivotTransaction(
        int pivotID
    )
        throws JSONException
    {
        pivotID = checkForRemoteInvocationsValidity(pivotID);
        this.setPivotTransaction(pivotID);

        List<LocalTransaction> localTransactionsSequence = new ArrayList<>();
        for (LocalTransaction lt : this.redesign)
            if(lt.getId() == 0)
                localTransactionsSequence.add(lt);

        Set<Short> retriableTouchedEntities = new HashSet<>();
        Set<Short> compensatableTouchedEntities = new HashSet<>();

        List<LocalTransaction> retriableLTs = new ArrayList<>();

        for(int i = 0; i < localTransactionsSequence.size(); i++){
            LocalTransaction lt = localTransactionsSequence.get(i);

            for (Integer id : lt.getRemoteInvocations()) {
                for (LocalTransaction localTransaction : this.redesign) {
                    if(localTransaction.getId() == id)
                        localTransactionsSequence.add(localTransaction);
                }
            }

            if(lt.getId() == pivotID) {
                lt.setType(LocalTransactionTypes.PIVOT);

                for (Integer id : lt.getRemoteInvocations()) {
                    for (LocalTransaction localTransaction : this.redesign) {
                        if(localTransaction.getId() == id)
                            retriableLTs.add(localTransaction);
                    }
                }

                identifyTouchedEntities(lt, retriableTouchedEntities);
            }
            else {
                lt.setType(LocalTransactionTypes.COMPENSATABLE);

                if(lt.getId() != 0) // ROOT
                    identifyTouchedEntities(lt, compensatableTouchedEntities);
            }
        }

        for (int i = 0; i < retriableLTs.size(); i++) {
            LocalTransaction lt = retriableLTs.get(i);

            for (Integer id : lt.getRemoteInvocations()) {
                for (LocalTransaction localTransaction : this.redesign) {
                    if(localTransaction.getId() == id)
                        retriableLTs.add(localTransaction);
                }
            }

            lt.setType(LocalTransactionTypes.RETRIABLE);
            identifyTouchedEntities(lt, retriableTouchedEntities);
        }
    }

    private void identifyTouchedEntities(
        LocalTransaction lt,
        Set<Short> touchedEntitiesIDs
    ) {
        Set<AccessDto> clusterAccesses = lt.getClusterAccesses();

        for(AccessDto access: clusterAccesses) {
            short entityID = access.getEntityID();
            byte mode = access.getMode();

            if (mode >= 2) // 2 -> "W", 3 -> "RW"
                touchedEntitiesIDs.add(entityID);
        }
    }

    private int checkForRemoteInvocationsValidity(
        int pivotID
    ) {
        int newPivotID = pivotID;

        for (int i = 0; i < this.redesign.size(); i++) {
            LocalTransaction lt1 = this.redesign.get(i);

            for(Integer id : lt1.getRemoteInvocations()){
                LocalTransaction lt2 = this.redesign
                    .stream()
                    .filter(e -> e.getId() == id)
                    .findFirst()
                    .orElse(null);

                if(lt2 != null){
                    if(lt1.getClusterID() == lt2.getClusterID()){
                        int newID = mergeTwoLocalTransactions(lt1, lt2);

                        if(lt1.getId() == pivotID || lt2.getId() == pivotID)
                            newPivotID = newID;
                    }
                }
            }
        }
        return newPivotID;
    }

    private int mergeTwoLocalTransactions(
        LocalTransaction lt1,
        LocalTransaction lt2
    ) {
        int min = Math.min(lt1.getId(), lt2.getId());

        HashMap<Short, Byte> ltAccesses = new HashMap<>();

        Set<AccessDto> lt1ClusterAccesses = lt1.getClusterAccesses();

        for (AccessDto access : lt1ClusterAccesses)
            ltAccesses.put(access.getEntityID(), access.getMode());

        Set<AccessDto> lt2ClusterAccesses = lt2.getClusterAccesses();

        for (AccessDto access : lt2ClusterAccesses) {
            short accessedEntityID = access.getEntityID();
            byte accessedMode = access.getMode();

            Byte mode = ltAccesses.get(accessedEntityID);

            if(mode == null) { // map does not contain the entity
                ltAccesses.put(accessedEntityID, accessedMode);
            } else if(mode != accessedMode){
                ltAccesses.put(accessedEntityID, (byte) 3); // 3 -> "RW"
            }
        }

        List<Integer> remoteInvocations = new ArrayList<>();
        for(Integer id : lt1.getRemoteInvocations()){
            if(id != lt2.getId())
                remoteInvocations.add(id);
        }

        remoteInvocations.addAll(lt2.getRemoteInvocations());

        for(LocalTransaction lt : this.redesign){
            if(lt.getRemoteInvocations().contains(lt1.getId())){
                lt.getRemoteInvocations().remove((Integer) lt1.getId());
                lt.getRemoteInvocations().add(min);
            }
        }

        LocalTransaction newLT = new LocalTransaction(
            min,
            lt1.getClusterID(),
            constructSequence(ltAccesses),
            remoteInvocations,
            min + ": " + lt1.getClusterID()
        );

        for(int i = 0; i < this.redesign.size() ; i++){
            if(this.redesign.get(i).getId() == lt1.getId()) {
                this.redesign.remove(i);
                continue;
            }
            if(this.redesign.get(i).getId() == lt2.getId()) {
                this.redesign.remove(i);
            }
        }

        this.redesign.add(newLT);
        return min;
    }


    public Set<Short> semanticLockEntities() {
        if(this.pivotTransaction == -1)
            return new HashSet<>();

        Set<Short> slEntities = new HashSet<>();

        for (LocalTransaction lt : this.redesign) {
            if(lt.getId() != 0) { // ROOT
                if (lt.getType() == LocalTransactionTypes.COMPENSATABLE) {
                    Set<AccessDto> clusterAccesses = lt.getClusterAccesses();

                    for (AccessDto access : clusterAccesses) {
                        short entityID = access.getEntityID();
                        byte mode = access.getMode();

                        if (mode >= 2) // 2 -> "W", 3 -> "RW"
                            slEntities.add(entityID);
                    }
                }
            }
        }

        return slEntities;
    }

    public void changeLTName(String ltID, String newName){
        for(LocalTransaction lt : this.redesign)
            if(lt.getId() == Integer.parseInt(ltID))
                lt.setName(newName);
    }

    public boolean isUsedForMetrics() {
        return usedForMetrics;
    }

    public void setUsedForMetrics(boolean usedForMetrics) {
        this.usedForMetrics = usedForMetrics;
    }

    public int getInconsistencyComplexity() {
        return inconsistencyComplexity;
    }

    public void setInconsistencyComplexity(int inconsistencyComplexity) {
        this.inconsistencyComplexity = inconsistencyComplexity;
    }
}
