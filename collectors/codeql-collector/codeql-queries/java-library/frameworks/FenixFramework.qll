import java

predicate extendsBaseSuperclass(Class c, Class sup) {
  c.getASupertype() = sup and
  sup.getName().matches("%\\_Base")
}

/**
 * Domain entity definition
 */
class DomainEntity extends Class {
  DomainEntity() {
    this.fromSource() and
    exists(Class superclass |
      extendsBaseSuperclass(this, superclass)
    )
  }

  predicate hasField(DomainField df) {
    exists(Class superclass |
        extendsBaseSuperclass(this, superclass) and
        df.getDeclaringType() = superclass
    )
  }

  predicate hasSuperclass(DomainSuperclass ds) {
    extendsBaseSuperclass(this, ds)
  }

}

/**
 * Domain entity's field definition
 */
class DomainField extends Method {
  DomainField() { 
    this = any(Method f |
        f.getName().matches("get%")
    ) 
  }

  string getFieldName() {
    result = this.getName().charAt(3).toLowerCase() + this.getName().suffix(4)
  }

  string getFieldType() {
    result = this.getReturnType().getName()
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

/**
 * Finds all calls inside of each callable, including those inside lambda expressions
 */
predicate callInsideCaller(Callable caller, Call call) {
  caller.getLocation().getFile() = call.getLocation().getFile() and
  call.getLocation().getStartLine() >= caller.getBody().getLocation().getStartLine() and
  call.getLocation().getEndLine() <= caller.getBody().getLocation().getEndLine()
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

predicate callerCallsCallee(CallableFunction caller, CallableFunction callee, Call call) {
  callInsideCaller(caller, call) and
  callsCallee(callee, call)
}

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
        (
          c.hasAnnotation("org.springframework.stereotype", "Controller") and
          cal.hasAnnotation("org.springframework.web.bind.annotation", "RequestBody")
        )
      )
    )
  )
  or (
    exists(Class c, Class sup |
      cal.getDeclaringType() = c and
      c.getASupertype() = sup and
      sup.getName() = "FenixDispatchAction"
    )
  )
}

/**
 * Register accesses to entity in function
 */
predicate callableAccessesEntity(CallableFunction cf, DomainEntity entity, string operation, Location loc) {
  // Has a contradiction since FunctionAccesses is irrelevant for the Fenix framework
  cf.isAbstract() and 
  not cf.isAbstract() and
  entity = entity and
  operation = "" and
  loc = loc
}