package pt.ist.socialsoftware.mono2micro.functionality;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.AccessesInformation;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.representation.domain.Representation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import javax.naming.NameAlreadyBoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.jgrapht.Graphs.successorListOf;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.Representation.ACCESSES_TYPE;

@Service
public class FunctionalityService {
    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    FunctionalityRepository functionalityRepository;

    @Autowired
    GridFsService gridFsService;

    public static FunctionalityRedesign createFunctionalityRedesign(
            GridFsService gridFsService,
            Decomposition decomposition,
            Functionality functionality,
            String name,
            boolean usedForMetrics,
            DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph
    ) throws Exception {
        AccessesInformation accessesInformation = (AccessesInformation) decomposition.getRepresentationInformationByType(ACCESSES_TYPE);
        FunctionalityRedesign functionalityRedesign = new FunctionalityRedesign(name);
        if (usedForMetrics)
            functionality.setFunctionalityRedesignNameUsedForMetrics(name);

        LocalTransaction graphRootLT = new LocalTransaction(0, "-1");

        graphRootLT.setName(functionality.getName());

        Iterator<LocalTransaction> iterator = new BreadthFirstIterator<>(
                localTransactionsGraph,
                graphRootLT
        );

        while (iterator.hasNext()) {
            LocalTransaction lt = iterator.next();
            lt.setRemoteInvocations(new ArrayList<>());

            List<LocalTransaction> graphChildrenLTs = successorListOf(
                    localTransactionsGraph,
                    lt
            );

            for (LocalTransaction childLT : graphChildrenLTs) {
                lt.addRemoteInvocations(childLT.getId());
                childLT.setName(childLT.getId() + ": " + childLT.getClusterName());
            }

            functionalityRedesign.getRedesign().add(lt);
            if(lt.getId() != 0){
                for(AccessDto accessDto : lt.getClusterAccesses()){
                    if(functionality.getEntitiesPerCluster().containsKey(lt.getClusterName())){
                        functionality.getEntitiesPerCluster().get(lt.getClusterName()).add(accessDto.getEntityID());
                    } else {
                        Set<Short> entities = new HashSet<>();
                        entities.add(accessDto.getEntityID());
                        functionality.getEntitiesPerCluster().put(lt.getClusterName(), entities);
                    }
                }
            }
        }

        functionalityRedesign.calculateMetrics(gridFsService, decomposition, accessesInformation, functionality);
        gridFsService.saveFile(getFunctionalityRedesignAsJSON(functionalityRedesign), functionality.getId() + functionalityRedesign.getName());
        functionality.addFunctionalityRedesign(functionalityRedesign.getName(), functionality.getId() + functionalityRedesign.getName());
        return functionalityRedesign;
    }
    public void saveFunctionality(Functionality functionality) {
        functionalityRepository.save(functionality);
    }

    public void saveFunctionalityRedesign(Functionality functionality, FunctionalityRedesign functionalityRedesign) throws IOException {
        gridFsService.saveFile(
                getFunctionalityRedesignAsJSON(functionalityRedesign),
                functionality.getId() + functionalityRedesign.getName()
        );
    }

    public void updateFunctionalityRedesign(Functionality functionality, FunctionalityRedesign functionalityRedesign) throws IOException {
        gridFsService.replaceFile(
                getFunctionalityRedesignAsJSON(functionalityRedesign),
                functionality.getId() + functionalityRedesign.getName()
        );
    }

    public static InputStream getFunctionalityRedesignAsJSON(FunctionalityRedesign functionalityRedesign) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new ObjectMapper().writeValue(outputStream, functionalityRedesign);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public FunctionalityRedesign getFunctionalityRedesign(Functionality functionality, String redesignName) throws IOException {
        return getFunctionalityRedesign(gridFsService.getFile(functionality.getFunctionalityRedesignFileName(redesignName)));
    }

    public FunctionalityRedesign getFunctionalityRedesign(InputStream inputStream) throws IOException {
        ObjectReader reader = new ObjectMapper().readerFor(FunctionalityRedesign.class);
        return reader.readValue(inputStream);
    }

    public List<FunctionalityRedesign> getFunctionalityRedesigns(Functionality functionality) {
        return functionality.getFunctionalityRedesigns().values().stream().map(fileName -> {
            try {
                return getFunctionalityRedesign(gridFsService.getFile(fileName));
            } catch (IOException e) { throw new RuntimeException(e); }
        }).collect(Collectors.toList());
    }

    public Functionality addCompensating(String decompositionName, String functionalityName, String redesignName, Map<String, Object> data) throws Exception {
        int fromID = (Integer) data.get("fromID");
        String clusterName = (String) data.get("cluster");
        ArrayList<Integer> accesses = (ArrayList<Integer>) data.get("entities");
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesInformation accessesInformation = (AccessesInformation) decomposition.getRepresentationInformationByType(ACCESSES_TYPE);

        Functionality functionality = accessesInformation.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = getFunctionalityRedesign(functionality, redesignName);
        functionalityRedesign.addCompensating(clusterName, accesses, fromID);
        functionalityRedesign.calculateMetrics(gridFsService, decomposition, accessesInformation, functionality);

        updateFunctionalityRedesign(functionality, functionalityRedesign);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality sequenceChange(String decompositionName, String functionalityName, String redesignName, Map<String, String> data) throws Exception {
        String localTransactionID = data.get("localTransactionID");
        String newCaller = data.get("newCaller");

        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesInformation accessesInformation = (AccessesInformation) decomposition.getRepresentationInformationByType(ACCESSES_TYPE);
        Functionality functionality = accessesInformation.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = getFunctionalityRedesign(functionality, redesignName);
        functionalityRedesign.sequenceChange(localTransactionID, newCaller);
        functionalityRedesign.calculateMetrics(gridFsService, decomposition, accessesInformation, functionality);

        updateFunctionalityRedesign(functionality, functionalityRedesign);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality dcgi(String decompositionName, String functionalityName, String redesignName, Map<String, String> data) throws Exception {
        String fromClusterName = data.get("fromCluster");
        String toClusterName = data.get("toCluster");
        String localTransactions = data.get("localTransactions");

        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesInformation accessesInformation = (AccessesInformation) decomposition.getRepresentationInformationByType(ACCESSES_TYPE);
        Functionality functionality = accessesInformation.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = getFunctionalityRedesign(functionality, redesignName);
        functionalityRedesign.dcgi(fromClusterName, toClusterName, localTransactions);
        functionalityRedesign.calculateMetrics(gridFsService, decomposition, accessesInformation, functionality);

        updateFunctionalityRedesign(functionality, functionalityRedesign);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality pivotTransaction(String decompositionName, String functionalityName, String redesignName, String transactionID, Optional<String> newRedesignName)
            throws Exception
    {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        SimilarityScipy similarity = (SimilarityScipy) decomposition.getSimilarity();
        AccessesInformation accessesInformation = (AccessesInformation) decomposition.getRepresentationInformationByType(ACCESSES_TYPE);
        Functionality functionality = accessesInformation.getFunctionality(functionalityName);

        if(newRedesignName.isPresent() && functionality.containsFunctionalityRedesignName(newRedesignName.get()))
                throw new NameAlreadyBoundException();

        FunctionalityRedesign functionalityRedesign = getFunctionalityRedesign(functionality, redesignName);
        functionalityRedesign.definePivotTransaction(Integer.parseInt(transactionID));
        functionalityRedesign.calculateMetrics(gridFsService, decomposition, accessesInformation, functionality);

        if(newRedesignName.isPresent()) {
            gridFsService.deleteFile(functionality.getFunctionalityRedesigns().remove(redesignName));
            functionalityRedesign.setName(newRedesignName.get());
            saveFunctionalityRedesign(functionality, functionalityRedesign);
            functionality.addFunctionalityRedesign(functionalityRedesign.getName(), functionality.getId() + functionalityRedesign.getName());
            functionality.setFunctionalityRedesignNameUsedForMetrics(functionalityRedesign.getName());

            Representation representation = similarity.getStrategy().getCodebase().getRepresentationByFileType(ACCESSES);

            DirectedAcyclicGraph<LocalTransaction, DefaultEdge> functionalityLocalTransactionsGraph = accessesInformation.getFunctionality(functionalityName)
                    .createLocalTransactionGraphFromScratch(
                            gridFsService.getFile(representation.getName()),
                            similarity.getTracesMaxLimit(),
                            similarity.getTraceType(),
                            decomposition.getEntityIDToClusterName());

            createFunctionalityRedesign(
                    gridFsService,
                    decomposition,
                    functionality,
                    Constants.DEFAULT_REDESIGN_NAME,
                    false,
                    functionalityLocalTransactionsGraph
            );
        }
        else updateFunctionalityRedesign(functionality, functionalityRedesign);

        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality changeLTName(String decompositionName, String functionalityName, String redesignName, String transactionID, String newName)
            throws IOException
    {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesInformation accessesInformation = (AccessesInformation) decomposition.getRepresentationInformationByType(ACCESSES_TYPE);
        Functionality functionality = accessesInformation.getFunctionality(functionalityName);
        FunctionalityRedesign functionalityRedesign = getFunctionalityRedesign(functionality, redesignName);
        functionalityRedesign.changeLTName(transactionID, newName);
        updateFunctionalityRedesign(functionality, functionalityRedesign);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality deleteRedesign(String decompositionName, String functionalityName, String redesignName) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesInformation accessesInformation = (AccessesInformation) decomposition.getRepresentationInformationByType(ACCESSES_TYPE);
        Functionality functionality = accessesInformation.getFunctionality(functionalityName);
        functionality.removeFunctionalityRedesign(redesignName);
        gridFsService.deleteFile(functionality.getFunctionalityRedesignFileName(redesignName));
        functionalityRepository.save(functionality);
        return functionality;
    }

    public Functionality useForMetrics(String decompositionName, String functionalityName, String redesignName) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesInformation accessesInformation = (AccessesInformation) decomposition.getRepresentationInformationByType(ACCESSES_TYPE);
        Functionality functionality = accessesInformation.getFunctionality(functionalityName);
        functionality.setFunctionalityRedesignNameUsedForMetrics(redesignName);
        functionalityRepository.save(functionality);
        return functionality;
    }

    public void deleteFunctionalities(Iterable<Functionality> functionalities) {
        for (Functionality functionality: functionalities)
            functionality.getFunctionalityRedesigns().values().forEach(fileName -> gridFsService.deleteFile(fileName));
        functionalityRepository.deleteAll(functionalities);
    }
}
