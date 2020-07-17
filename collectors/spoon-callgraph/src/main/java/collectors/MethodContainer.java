package collectors;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class MethodContainer implements Serializable {
    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getArgumentTypes() {
        return argumentTypes;
    }

    public void setArgumentTypes(List<String> argumentTypes) {
        this.argumentTypes = argumentTypes;
    }

    public String getDeclaringType() {
        return declaringType;
    }

    public void setDeclaringType(String declaringType) {
        this.declaringType = declaringType;
    }

    String returnType;
    String methodName;
    List<String> argumentTypes;
    String declaringType;

    public MethodContainer() {
    }

    public MethodContainer(String returnType, String methodName, List<String> argumentTypes, String declaringType) {
        this.returnType = returnType;
        this.methodName = methodName;
        this.argumentTypes = argumentTypes;
        this.declaringType = declaringType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, methodName, argumentTypes, declaringType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodContainer) {
            return this.methodName.equals(((MethodContainer) obj).methodName)
                    && this.argumentTypes.equals(((MethodContainer) obj).argumentTypes)
                    && this.declaringType.equals(((MethodContainer) obj).declaringType)
                    && this.returnType.equals(((MethodContainer) obj).returnType);
        }
        return false;
    }
}
