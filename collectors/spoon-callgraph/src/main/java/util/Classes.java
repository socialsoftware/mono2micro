package util;

import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.List;

public class Classes {
    private List<CtType> listOfClasses;

    public Classes() {
        listOfClasses = new ArrayList<>();
    }

    public List<CtType> getListOfClasses() {
        return listOfClasses;
    }

    public void addClass(CtType c) {
        this.listOfClasses.add(c);
    }
}
