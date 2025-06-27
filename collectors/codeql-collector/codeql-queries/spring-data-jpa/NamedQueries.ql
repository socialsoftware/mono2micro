import frameworks.SpringDataJPA

from Annotation ann, string native
where
  (ann instanceof NamedQueryAnnotation and native = "no") or
  (ann instanceof NamedNativeQueryAnnotation and native = "yes")
select ann.getValue("name").toString(), ann.getValue("query").toString(), native