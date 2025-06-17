import python

/**
 * Domain entity's superclass
 */
class DomainSuperclass extends string {
  DomainSuperclass() { this = "models.Model" }
}

/**
 * Domain entity definition
 */
class DomainEntity extends Class {
  DomainEntity() {
    this.getABase().getEnclosingModule().toString().matches("%.models")
  }

  predicate hasField(DomainField df) {
    df = this.getAStmt()
  }

  predicate hasSuperclass(DomainSuperclass ds) {
    ds.matches("models.Model")
  }

}

/**
 * Domain entity's field definition
 */
class DomainField extends AssignStmt {
  DomainField() {
    // This assignment must be in a domain entity
    exists(DomainEntity de |
      this = de.getAStmt()
    )
  }

  string getName() {
    result = this.getATarget().toString()
  }

  string getType() {
    exists(Attribute a |
      this.getValue().(Expr).getASubExpression() = a |
      result = a.getName()
    )
  }
}

/**
 * Any declared callable - function, constructor, etc..
 */
class CallableFunction extends Function {
  CallableFunction() { 
    this = any(Function f)
  }

  string getFullName() {
    result = this.getScope().getName() + "." + this.getName()
  }

}

/**
 * Any call made
 */
class FunctionInvoc extends Call {
  FunctionInvoc() { this = any(Call c) }
}

/**
 * Checks if a function is a view
 */
predicate isEndpoint(Function f) {
  exists(Call c |
    c.getEnclosingModule().toString().matches("%.urls") and
    c.toString().matches("path%") and
    c.getArg(1) instanceof Attribute and
    f.getName() = ((Attribute) c.getArg(1)).getName()
  )
}

predicate callerCallsCallee(CallableFunction caller, CallableFunction callee, Call c) {
  exists(FunctionInvocation funcInvoc |
    c = funcInvoc.getCall().getNode() and
    c.getScope().inSource() and
    c.getScope() = caller and
    funcInvoc.getFunction().getFunction() = callee
  )
}

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


predicate callableAccessesEntity(CallableFunction cf, DomainEntity de, string operation, Location loc) { 
  exists(Attribute a, Expr e |
    a.getScope().inSource() and
    getBaseAttr(a, e) and
    cf = a.getScope() and
    getOperation(a.getName(), operation) and
    (
      (
        e.toString() = "self" and
        de.toString().suffix(6) = cf.getScope().toString().suffix(6)
      ) or
      (
        e.toString() != "self" and
        de.toString().suffix(6) = e.toString()
      )
    ) and
    loc = e.getLocation()
  )
}