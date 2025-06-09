import java
import annotations

from NamedQuery nq, string native
where 
    (isNamedQuery(nq) and native = "false") or
    (isNamedNativeQuery(nq) and native = "true")
select nq, nq.getValue("name"), nq.getValue("query"), native