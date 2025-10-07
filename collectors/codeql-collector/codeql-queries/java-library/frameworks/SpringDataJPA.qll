import java

/**
 * Domain entity definition
 */
class DomainEntity extends Class {
  DomainEntity() {
    isEntity(this) or isMappedSuperclass(this) or isEmbeddable(this)
  }

  predicate hasField(DomainField df) {
    this.getAField() = df
  }

  predicate hasSuperclass(DomainSuperclass ds) {
    this.getASupertype() = ds
  }

}

predicate isEmbeddable(Class de) {
  exists(EmbeddableAnnotation embeddable |
    de.getAnAnnotation() = embeddable
  )
}

predicate isEntity(Class de) {
  exists(EntityAnnotation entity |
    de.getAnAnnotation() = entity
  )
}

predicate isMappedSuperclass(Class de) {
  exists(MappedSuperclassAnnotation mapperSuperclass |
    de.getAnAnnotation() = mapperSuperclass
  )
}

class EntityAnnotation extends Annotation {
  EntityAnnotation() {
    this.getType().hasQualifiedName("javax.persistence", "Entity")
    or
    this.getType().hasQualifiedName("jakarta.persistence", "Entity")
  }
}

class MappedSuperclassAnnotation extends Annotation {
  MappedSuperclassAnnotation() {
    this.getType().hasQualifiedName("javax.persistence", "MappedSuperclass")
    or
    this.getType().hasQualifiedName("jakarta.persistence", "MappedSuperclass")
  }
}

class EmbeddableAnnotation extends Annotation {
  EmbeddableAnnotation() {
    this.getType().hasQualifiedName("javax.persistence", "Embeddable")
    or
    this.getType().hasQualifiedName("jakarta.persistence", "Embeddable")
  }
}

/**
 * Domain entity's field definition
 */
class DomainField extends Field {
  DomainField() { this = any(Field f) }

  string getFieldName() {
    result = this.getName()
  }

  Type getFieldType() {
    result = this.getType()
  }

}

/**
 * Domain entity's superclass
 */
class DomainSuperclass extends Class {
  DomainSuperclass() { this = any(Class c) }
}

/**
 * Any declared callable - function, constructor, etc..
 */
class CallableFunction extends Callable {
  CallableFunction() { 
    this = any(Callable c |
      filterIrrelevantCallablesByName(c)
    ) 
  }

  string getFullName() {
    result = this.getDeclaringType().getName() + "." + this.getName()
  }

  string getId() {
    result = this.getDeclaringType().getPackage().getName() + "." + this.getSignature()
  }

}

/**
 * Any call made
 */
class FunctionInvoc extends Call {
  FunctionInvoc() { this = any(Call c) }
}

predicate callerCallsCallee(CallableFunction caller, CallableFunction callee, Call call) {
  callInsideCaller(caller, call) and
  callsCallee(callee, call)
}

/**
 * Finds all calls inside of each callable, including those inside lambda expressions
 */
predicate callInsideCaller(Callable caller, Call call) {
  caller.getLocation().getFile() = call.getLocation().getFile() and
  call.getLocation().getStartLine() >= caller.getBody().getLocation().getStartLine() and
  call.getLocation().getEndLine() <= caller.getBody().getLocation().getEndLine()
}

/**
 * Finds all fieldAccesses inside of each callable, including those inside lambda expressions
 */
predicate fieldAccessInsideCaller(Callable caller, FieldAccess fa) {
  caller.getLocation().getFile() = fa.getLocation().getFile() and
  fa.getLocation().getStartLine() >= caller.getBody().getLocation().getStartLine() and
  fa.getLocation().getEndLine() <= caller.getBody().getLocation().getEndLine()
}

/**
 * Filter irrelevant Callables by name and declaringType
 */
predicate filterIrrelevantCallablesByName(Callable c) {
  not c.getDeclaringType().toString().matches("new %") and
  not c.toString().matches("<obinit>") and
  not c.toString().matches("Object")
}

/**
 * Finds callee for every call
 */
predicate callsCallee(Callable callee, Call call) {
  (
    call.getCallee() = callee
  ) or
  ( // callee is and abstract or interface
    exists(Method m, Method override |
      call.getCallee() = m and
      m.isAbstract() and
      override.overrides(m) and
      callee = override
    )
  )
}

/**
 * Checks if method is a controller method
 */
predicate isEndpoint(Method cal) {
  exists(Class c |
    cal.getDeclaringType() = c and
    (
      (
        cal.hasAnnotation("org.springframework.web.bind.annotation", "RequestMapping") or
        cal.hasAnnotation("org.springframework.web.bind.annotation", "GetMapping") or
        cal.hasAnnotation("org.springframework.web.bind.annotation", "PostMapping") or
        cal.hasAnnotation("org.springframework.web.bind.annotation", "PatchMapping") or
        cal.hasAnnotation("org.springframework.web.bind.annotation", "PutMapping") or
        cal.hasAnnotation("org.springframework.web.bind.annotation", "DeleteMapping")
      ) and
      (
        c.hasAnnotation("org.springframework.web.bind.annotation", "RestController") or
        c.hasAnnotation("org.springframework.web.bind.annotation", "RequestMapping") or
        c.hasAnnotation("org.springframework.stereotype", "Controller")
      )
    )
  )
  or (
    exists(Class c, Class sup |
      cal.getDeclaringType() = c and
      c.getASupertype() = sup and
      sup.getName() = "DispatchAction"
    )
  )
}

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
    callInsideCaller(c, cc) and
    loc = cc.getLocation() and
    not exists(MappedSuperclassAnnotation ms |
      entity.getAnAnnotation() = ms
    )
  )
}

predicate fieldAccessInCallableAccessesEntity(Callable c, FieldAccess fa, Field f, DomainEntity entity, Location loc) {
  fieldAccessInsideCaller(c, fa) and
  fa.getField() = f and
  loc = fa.getLocation() and
  f.getDeclaringType() = entity
}

/**
 * Predicate to find all entity accesses via field read accesses
 */
predicate methodAccessesFieldRead(Callable c, DomainEntity entity, Location loc) {
  exists(FieldRead fr, Field f |
    fieldAccessInCallableAccessesEntity(c, fr, f, entity, loc)
  ) or
  exists(FieldRead fr, Field f, ParameterizedType pt, DomainEntity declEntity |
    fieldAccessInCallableAccessesEntity(c, fr, f, declEntity, loc) and
    fieldNotTransient(f) and
    (
      f.getType() = entity or // Field's type is an entity
      (f.getType() = pt and pt.getATypeArgument() = entity) // Type is parameterized
    )
  ) 
}

/**
 * Predicate to find all entity accesses via field write accesses
 */
predicate methodAccessesFieldWrite(Callable c, DomainEntity entity, Location loc) {
  exists(FieldWrite fw, Field f |
    fieldAccessInCallableAccessesEntity(c, fw, f, entity, loc)
  ) or
  exists(FieldWrite fw, Field f, ParameterizedType pt, DomainEntity declEntity |
    fieldAccessInCallableAccessesEntity(c, fw, f, declEntity, loc) and
    fieldNotTransient(f) and
    (
      f.getType() = entity or // Field's type is an entity
      (f.getType() = pt and pt.getATypeArgument() = entity) // Type is parameterized
    )
  )
}

/**
 * Register accesses to entity in function
 */
predicate callableAccessesEntity(CallableFunction cf, DomainEntity entity, string operation, Location loc) {
  (
    (entityConstructorCall(cf, entity, loc) and operation = "W") or
    (methodAccessesFieldRead(cf, entity, loc) and operation = "R") or
    (methodAccessesFieldWrite(cf, entity, loc) and operation = "W")
  )
}