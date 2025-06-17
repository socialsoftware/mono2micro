import frameworks.SpringDataJPA

from NamedQueryAnnotation nq, NamedNativeQueryAnnotation nnq
where 
    (
        ann = nq and
        native = "false"
    ) or
    (
        ann = nnq and
        native = "true"
    )
select ann, ann.getValue("name"), ann.getValue("query"), native