package collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"id", "frequency","accesses" })
public class Trace {
    protected int id;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Access> accesses;

    public Trace(){}

    public Trace(int id) {
        this.id = id;
        this.accesses = new ArrayList<>();
    }

    public Trace(Trace t) {
        this.id = t.getId();
        this.accesses = new ArrayList<>(t.getAccesses());
    }

    @JsonCreator
    public Trace(
            @JsonProperty("id") int id,
            @JsonProperty("accs") List<Access> accesses)
    {
        this.id = id;
        this.accesses = accesses;
    }

    public int getId() {
        return this.id;
    }

    @JsonProperty("accs")
    public List<Access> getAccesses() { return this.accesses; }

    public void addSingleAccess(Access access) {
        this.accesses.add(access);
    }

    public void addMultipleAccesses(ArrayList<Access> accesses) {
        this.accesses.addAll(accesses);
    }

    public void setAccesses(ArrayList<Access> accesses) { this.accesses = accesses; }
}