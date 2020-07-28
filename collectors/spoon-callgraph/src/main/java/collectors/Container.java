package collectors;

import java.util.List;
import java.util.Objects;

public class Container<T> {
    List<T> list;

    public Container(List<T> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "Container{" +
                "last=" + list.get(list.size()-1).toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container<?> container = (Container<?>) o;
        return list.equals(container.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }
}
