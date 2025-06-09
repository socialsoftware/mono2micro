import java

predicate isNamedQuery(Annotation a) {
    a.getType().hasQualifiedName("javax.persistence", "NamedQuery") or
    a.getType().hasQualifiedName("jakarta.persistence", "NamedQuery")
}

predicate isNamedNativeQuery(Annotation a) {
    a.getType().hasQualifiedName("javax.persistence", "NamedNativeQuery") or
    a.getType().hasQualifiedName("jakarta.persistence", "NamedNativeQuery")
}

class NamedQuery extends Annotation {
    NamedQuery() {
        isNamedQuery(this) or isNamedNativeQuery(this)
    }
}

class Query extends Annotation {
    Query() {
        this.getType().hasQualifiedName("org.springframework.data.jpa.repository", "Query")
    }
}

class ElementCollection extends Annotation {
    ElementCollection() {
        this.getType().hasQualifiedName("javax.persistence", "ElementCollection") or
        this.getType().hasQualifiedName("jakarta.persistence", "ElementCollection")
    }
}

class CollectionTable extends Annotation {
    CollectionTable() {
        this.getType().hasQualifiedName("javax.persistence", "CollectionTable") or
        this.getType().hasQualifiedName("jakarta.persistence", "CollectionTable")
    }
}

class JoinTable extends Annotation {
    JoinTable() {
        this.getType().hasQualifiedName("javax.persistence", "JoinTable") or
        this.getType().hasQualifiedName("jakarta.persistence", "JoinTable")
    }
}

class OneToOne extends Annotation {
    OneToOne() {
        this.getType().hasQualifiedName("javax.persistence", "OneToOne") or
        this.getType().hasQualifiedName("jakarta.persistence", "OneToOne")
    }
}

class OneToMany extends Annotation {
    OneToMany() {
        this.getType().hasQualifiedName("javax.persistence", "OneToMany") or
        this.getType().hasQualifiedName("jakarta.persistence", "OneToMany")
    }
}

class JoinColumn extends Annotation {
    JoinColumn() {
        this.getType().hasQualifiedName("javax.persistence", "JoinColumn") or
        this.getType().hasQualifiedName("jakarta.persistence", "JoinColumn")
    }
}

class ManyToOne extends Annotation {
    ManyToOne() {
        this.getType().hasQualifiedName("javax.persistence", "ManyToOne") or
        this.getType().hasQualifiedName("jakarta.persistence", "ManyToOne")
    }
}

class ManyToMany extends Annotation {
    ManyToMany() {
        this.getType().hasQualifiedName("javax.persistence", "ManyToMany") or
        this.getType().hasQualifiedName("jakarta.persistence", "ManyToMany")
    }
}

class Table extends Annotation {
    Table() {
        this.getType().hasQualifiedName("javax.persistence", "Table") or
        this.getType().hasQualifiedName("jakarta.persistence", "Table")
    }
}

class Entity extends Annotation {
    Entity() {
        this.getType().hasQualifiedName("javax.persistence", "Entity") or
        this.getType().hasQualifiedName("jakarta.persistence", "Entity")
    }
}