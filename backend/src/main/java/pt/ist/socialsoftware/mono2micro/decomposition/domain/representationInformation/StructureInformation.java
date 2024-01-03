package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.DecompositionMetricCalculator;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.representation.domain.Representation.STRUCTURE_TYPE;

public class StructureInformation extends RepresentationInformation {

    // A structural based decomposition strategy is not yet implemented.
    // Only the minimum functionality was added for two reasons:
    // 1. Allow a user to input structural data from the structure collector
    // 2. Allow a user to be able to export decompositions if this data is present (implicates having a strategy defined)

    // Feel free to modify this class and others related to structural data as needed.
    // Only the getType() method is used to show/hide the 'export to CML' button is the frontend

    @Override
    public String getType() {
        return STRUCTURE_TYPE;
    }

    @Override
    public void deleteProperties() {
    }

    @Override
    public void setup(Decomposition decomposition) throws Exception {
        this.decompositionName = decomposition.getName();
        decomposition.addRepresentationInformation(this);
    }

    @Override
    public void update(Decomposition decomposition) throws Exception {
    }

    @Override
    public void snapshot(Decomposition snapshotDecomposition, Decomposition decomposition) throws Exception {
        this.decompositionName = snapshotDecomposition.getName();
        snapshotDecomposition.addRepresentationInformation(this);
    }

    @Override
    public List<DecompositionMetricCalculator> getDecompositionMetrics() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getParameters() {
        return new ArrayList<>();
    }

    @Override
    public String getEdgeWeights(Decomposition decomposition) throws Exception {
        return "";
    }

    @Override
    public String getSearchItems(Decomposition decomposition) throws Exception {
        return "";
    }
}
