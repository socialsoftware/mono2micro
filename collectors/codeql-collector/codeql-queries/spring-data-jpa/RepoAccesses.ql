import frameworks.SpringDataJPA

class Query extends Annotation {
    Query() {
        this.getType().hasQualifiedName("org.springframework.data.jpa.repository", "Query")
    }
}

predicate entityRepositoryMethod(MethodCall mc, Method m, DomainEntity entity, Location loc) {
    exists(ParameterizedType jpaType, RefType refType |
        mc.getEnclosingCallable() = m and
        mc.getMethod().getDeclaringType().extendsOrImplements(jpaType) and
        loc = mc.getLocation() and
        jpaType.getATypeArgument() = entity and
        refType = jpaType.getErasure() and
        (
            refType.getPackage().getName().substring(0, 35) = "org.springframework.data.repository" or
            refType.getPackage().getName().substring(0, 39) = "org.springframework.data.jpa.repository"
        )
    )
}

from MethodCall mc, Method m, DomainEntity entity, string declared, string annotation, string native, string queryName, Location loc, string methodFullName
where
    entityRepositoryMethod(mc, m, entity, loc) and
    (
        (not mc.getMethod().fromSource() and declared = "no") or
        (mc.getMethod().fromSource() and declared = "yes") 
    ) and
    (
        (
            not mc.getMethod().hasAnnotation("org.springframework.data.jpa.repository", "Query") and 
            annotation = "null" and
            native = "no-query" and
            queryName = "null"
        ) or
        exists(Query q |
            mc.getMethod().getAnAnnotation() = q and
            annotation = q.getValue("value").toString() and
            native = q.getValue("nativeQuery").toString() and
            queryName = q.getValue("name").toString()
        )
    ) and
    methodFullName = mc.getMethod().getDeclaringType().getPackage().getName() + "." + mc.getMethod().getSignature()
select 
    methodFullName, 
    mc.getMethod().getDeclaringType(), 
    mc.getMethod(), 
    entity.getLocation(), 
    declared, 
    annotation, 
    native, 
    queryName, 
    loc
