package collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Container {
    List<MySourcePosition> list;

    public Container(List<MySourcePosition> list) {
        this.list = list;
    }

    public Container(Container id) {
        List<MySourcePosition> l = new ArrayList<>();
        for (MySourcePosition entry : id.list) {
            l.add(new MySourcePosition(entry));
        }
        this.list = l;
    }

    @Override
    public String toString() {
        return list.get(list.size()-1).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container container = (Container) o;
        return list.equals(container.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    public void add(MySourcePosition item) {
        list.add(item);
    }
}
