package example;

import pt.ist.fenixframework.DomainRoot;

public class User extends User_Base {
    public User(String name, DomainRoot root) {
        setName(name);
        setRoot(root);
    }
}