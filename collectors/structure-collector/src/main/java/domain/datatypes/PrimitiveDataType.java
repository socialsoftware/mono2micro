package domain.datatypes;

public class PrimitiveDataType extends DataType {

    public PrimitiveDataType(String name) {
        super(name);
    }

    @Override
    public String toStringKey() {
        return getName();
    }
}
