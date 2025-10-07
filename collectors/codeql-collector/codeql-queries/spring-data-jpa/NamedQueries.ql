import frameworks.SpringDataJPA

class NamedQueryAnnotation extends Annotation {
  NamedQueryAnnotation() {
    this.getType().hasQualifiedName("javax.persistence", "NamedQuery")
    or
    this.getType().hasQualifiedName("jakarta.persistence", "NamedQuery")
  }
}

class NamedNativeQueryAnnotation extends Annotation {
  NamedNativeQueryAnnotation() {
    this.getType().hasQualifiedName("javax.persistence", "NamedNativeQuery")
    or
    this.getType().hasQualifiedName("jakarta.persistence", "NamedNativeQuery")
  }
}


from Annotation ann, string native
where
  (ann instanceof NamedQueryAnnotation and native = "no") or
  (ann instanceof NamedNativeQueryAnnotation and native = "yes")
select ann.getValue("name").toString(), ann.getValue("query").toString(), native