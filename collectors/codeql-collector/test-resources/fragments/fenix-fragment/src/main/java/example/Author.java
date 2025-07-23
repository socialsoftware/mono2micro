package example;

import pt.ist.fenixframework.DomainRoot;

public class Author extends Author_Base {
    public Author(String name, DomainRoot root) {
        setName(name);
        setRoot(root);
    }
}
