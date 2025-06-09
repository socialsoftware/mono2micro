import python
import entity

predicate getBaseAttr(Expr e, Expr base) {
    (
        e instanceof Attribute and
        getBaseAttr(e.(Attribute).getObject(), base)
    ) or
    (
        e instanceof Call and
        getBaseAttr(e.(Call).getFunc(), base)
    ) or
    (
        not e instanceof Attribute and
        not e instanceof Call and
        base = e
    )
}

predicate getOperation(string functionName, string op) {
    (
        (
            functionName = "all" or
            functionName = "filter" or
            functionName = "exclude" or
            functionName = "get" or
            functionName = "get_or_create" or
            functionName = "count" or
            functionName = "exists" or
            functionName = "values" or
            functionName = "values_list" or
            functionName = "aggregate" or
            functionName = "annotate"
        ) and
        op = "R"
    ) or
    (
        (
            functionName = "create" or
            functionName = "update" or
            functionName = "delete" or
            functionName = "bulk_create" or
            functionName = "bulk_update" or
            functionName = "get_or_create" or
            functionName = "update_or_create"
        ) and
        op = "W"
    )
}

from Attribute a, Expr e, DomainEntity de, Function f, string op, string entity
where 
    a.getScope().inSource() and
    getBaseAttr(a, e) and
    f = a.getScope() and
    (
        e.toString() = de.getName() or
        e.toString() = "self"
    ) and
    getOperation(a.getName(), op) and
    (
        (
            e.toString() = "self" and
            entity = f.getScope().toString().suffix(6)
        ) or
        (
            e.toString() != "self" and
            entity = e.toString()
        )
    )
select f.getScope(), f, entity, op, f.getLocation()