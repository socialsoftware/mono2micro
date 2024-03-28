package pt.ist.socialsoftware.mono2micro.utils.export;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.io.IOException;
import java.io.InputStream;

/**
 * Used to create a contract containing decomposition information for external use.
 * Currently, only used for creating a contract for Context Mapper.
 */
public abstract class DecompositionContractBuilder {

    protected Decomposition decomposition;
    protected JSONObject accessesRepresentationAsJSON;
    protected JSONObject structureRepresentationAsJSON;
    protected JSONObject sagaRefactorizationAsJSON;

    public DecompositionContractBuilder(Decomposition decomposition) {
        this.decomposition = decomposition;
    }

    public DecompositionContractBuilder addAccessRepresentationData(InputStream accessRepresentationData) throws IOException, JSONException {
        this.accessesRepresentationAsJSON = new JSONObject(new String(IOUtils.toByteArray(accessRepresentationData)));
        accessRepresentationData.close();
        return this;
    }

    public DecompositionContractBuilder addStructureRepresentationData(InputStream structureRepresentationData) throws IOException, JSONException {
        this.structureRepresentationAsJSON = new JSONObject(new String(IOUtils.toByteArray(structureRepresentationData)));
        structureRepresentationData.close();
        return this;
    }

    public DecompositionContractBuilder addSagaRefactorizationData(InputStream sagaRefactorizationData) throws IOException, JSONException {
        this.sagaRefactorizationAsJSON = new JSONObject(new String(IOUtils.toByteArray(sagaRefactorizationData)));
        sagaRefactorizationData.close();
        return this;
    }

    public String buildContract() throws IOException, JSONException {
        if (isMissingContractData()) {
            throw new IOException();
        }
        return parseContractData();
    }

    protected abstract boolean isMissingContractData();

    protected abstract String parseContractData() throws IOException, JSONException;
}
