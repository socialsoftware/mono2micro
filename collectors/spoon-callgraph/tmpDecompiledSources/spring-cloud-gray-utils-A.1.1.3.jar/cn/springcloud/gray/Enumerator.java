/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class Enumerator<T>
implements Enumeration<T> {
    private Iterator<T> iterator = null;

    public Enumerator(Collection<T> collection) {
        this(collection.iterator());
    }

    public Enumerator(Collection<T> collection, boolean clone) {
        this(collection.iterator(), clone);
    }

    public Enumerator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    public Enumerator(Iterator<T> iterator, boolean clone) {
        if (!clone) {
            this.iterator = iterator;
        } else {
            ArrayList<T> list = new ArrayList<T>();
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
            this.iterator = list.iterator();
        }
    }

    public Enumerator(Map<?, T> map) {
        this(map.values().iterator());
    }

    public Enumerator(Map<?, T> map, boolean clone) {
        this(map.values().iterator(), clone);
    }

    @Override
    public boolean hasMoreElements() {
        return this.iterator.hasNext();
    }

    @Override
    public T nextElement() throws NoSuchElementException {
        return this.iterator.next();
    }
}

