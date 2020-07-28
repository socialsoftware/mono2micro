package collectors;

import spoon.reflect.cu.SourcePosition;

import java.util.Objects;

public class MySourcePosition {
    private final SourcePosition sourcePosition;
    private final boolean isEnd;
    private final String label;
    private final boolean isSuper;

    public MySourcePosition(MySourcePosition msp) {
        this.sourcePosition = msp.sourcePosition;
        this.isEnd = msp.isEnd;
        this.isSuper = msp.isSuper;
        this.label = msp.label;
    }

    public MySourcePosition(SourcePosition sourcePosition, boolean isEnd, boolean isSuper, String label) {
        this.sourcePosition = sourcePosition;
        this.isEnd = isEnd;
        this.isSuper = isSuper;
        this.label = label;
    }

    public SourcePosition getSourcePosition() {
        return sourcePosition;
    }

    public boolean isEnd() {
        return isEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MySourcePosition that = (MySourcePosition) o;
        return isEnd == that.isEnd &&
                sourcePosition.equals(that.sourcePosition) &&
                isSuper == that.isSuper;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourcePosition, isEnd, isSuper);
    }

    @Override
    public String toString() {
        return label;
    }
}
