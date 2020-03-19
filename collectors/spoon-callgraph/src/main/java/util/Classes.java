package util;

import java.util.ArrayList;
import java.util.List;

public class Classes {
    private List<String> listOfClasses;

    public Classes() {
        listOfClasses = new ArrayList<>();
    }

    public List<String> getListOfClasses() {
        return listOfClasses;
    }

    public void addClass(String s) {
        this.listOfClasses.add(s);
    }
}
