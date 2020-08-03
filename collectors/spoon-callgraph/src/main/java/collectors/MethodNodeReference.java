package collectors;

import java.util.List;

public class MethodNodeReference {
    private Node beginNode;
    private Node endNode;
    private List<Access> methodSubTraces;

    public MethodNodeReference() {}

    public Node getBeginNode() {
        return beginNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setBeginNode(Node beginNode) {
        this.beginNode = beginNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    public List<Access> getMethodSubTraces() {
        return methodSubTraces;
    }

    public void setMethodSubTraces(List<Access> subtraces) {
        this.methodSubTraces = subtraces;
    }
}
