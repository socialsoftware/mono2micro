import java
import entity
import callgraph

predicate fieldNotTransient(Field f) {
    not f.hasModifier("transient") and
    not f.hasModifier("static") and
    not f.hasModifier("final") and
    not f.hasAnnotation("jakarta.persistence", "Transient") and
    not f.hasAnnotation("javax.persistence", "Transient") and
    not f.hasAnnotation("java.beans", "Transient") and
    not f.hasAnnotation("org.springframework.data.annotation", "Transient")
}

/**
 * Predicate to find entity accesses via constructor calls
 */
predicate entityConstructorCall(Callable c, DomainEntity entity, Location loc) {
    exists(ConstructorCall cc |
        cc.getConstructor().getDeclaringType() = entity and
        cc.getEnclosingCallable() = c and
        loc = cc.getLocation() and
        not (
            entity.hasAnnotation("javax.persistence", "MappedSuperclass") or
            entity.hasAnnotation("jakarta.persistence", "MappedSuperclass")
        )
    )
}

/**
 * Predicate to find all entity accesses via field read accesses
 */
predicate methodAccessesFieldRead(Callable m, DomainEntity entity, Location loc) {
    exists(FieldRead fr, Field f |
        fr.getField() = f and
        fr.getEnclosingCallable() = m and
        loc = fr.getLocation() and
        m.getName() != "<obinit>" and  // Exclude object initializers
        f.getDeclaringType() = entity
    ) or
    exists(FieldRead fr, Field f, ParameterizedType pt, DomainEntity declEntity |
        fr.getField() = f and
        fr.getEnclosingCallable() = m and
        loc = fr.getLocation() and
        fieldNotTransient(f) and
        m.getName() != "<obinit>" and  // Exclude object initializers
        f.getDeclaringType() = declEntity and
        (
            f.getType() = entity or // Field's type is an entity
            (f.getType() = pt and pt.getATypeArgument() = entity)
        )
    ) 
}

/**
 * Predicate to find all entity accesses via field write accesses
 */
predicate methodAccessesFieldWrite(Callable m, DomainEntity entity, Location loc) {
    exists(FieldWrite fw, Field f |
        fw.getField() = f and
        fw.getEnclosingCallable() = m and
        loc = fw.getLocation() and
        m.getName() != "<obinit>" and // Exclude object initializers
        f.getDeclaringType() = entity
    ) or
    exists(FieldWrite fw, Field f, ParameterizedType pt, DomainEntity declEntity |
        fw.getField() = f and
        fw.getEnclosingCallable() = m and
        loc = fw.getLocation() and
        fieldNotTransient(f) and
        m.getName() != "<obinit>" and  // Exclude object initializers
        f.getDeclaringType() = declEntity and
        (
            f.getType() = entity or // Field's type is an entity
            (f.getType() = pt and pt.getATypeArgument() = entity)
        )
    )  
}

from Callable m, DomainEntity entity, string operation, Location loc
where
    (
        (entityConstructorCall(m, entity, loc) and operation = "W") or
        (methodAccessesFieldRead(m, entity, loc) and operation = "R") or
        (methodAccessesFieldWrite(m, entity, loc) and operation = "W")
    )
select m.getDeclaringType(), m, entity, operation, loc
    
