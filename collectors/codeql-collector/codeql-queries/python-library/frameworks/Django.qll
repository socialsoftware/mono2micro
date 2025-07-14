import python

/**
 * Domain entity's superclass
 */
class DomainSuperclass extends Expr {
  DomainSuperclass() {
    exists(DomainEntity de |
      this = de.getABase()
    )
  }

  string getName() {
    (
      this.toString() = "Attribute" and
      result = "models.Model"
    ) or
    (
      this.toString() != "Attribute" and
      result = this.toString()
    )
  }
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
    this.getABase() = ds
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

  string getFieldName() {
    result = this.getATarget().toString()
  }

    string getFieldType() {
    // Handles ForeignKey, ManyToManyField, etc. with related model
    result = getEnhancedFieldType()
  }

  /**
   * Get field names for normal types and relationship fields
   */
  string getEnhancedFieldType() {
    // ForeignKey, ManyToManyField or OneToOneField
    result = this.getValue().(Call).getFunc().(Attribute).getName() + "<" + getArgName() + ">"
    or
    result = getSimpleModelName()
  }

  string getSimpleModelName() {
    exists(Call c, Attribute a |
      c = this.getValue().(Call) and
      a = c.getFunc().(Attribute) and
      (
        a.getName() != "ForeignKey" and
        a.getName() != "ManyToManyField" and
        a.getName() != "OneToOneField"
      ) |
      result = a.getName()
    )
  }
  
  string getArgName() {
    exists(Call c, Attribute a |
      c = this.getValue().(Call) and
      a = c.getFunc().(Attribute) and
      (
        a.getName() = "ForeignKey" or
        a.getName() = "ManyToManyField" or
        a.getName() = "OneToOneField"
      ) |
      result = c.getPositionalArg(0).toString()
    )
  }

}

/**
 * Any declared callable - function, constructor, etc..
 */
class CallableFunction extends Scope {
  CallableFunction() { 
    this = any(AstNode astNode |
      astNode instanceof Class or 
      astNode instanceof Function
    )
  }

  string getFullName() {
    result = this.getScope().getName() + "." + this.(Class).getName() 
    or
    result = this.getScope().getName() + "." + this.(Function).getName()
  }

  Location getId() {
    result = this.getLocation()
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
predicate isEndpoint(CallableFunction cf) {
  exists(Call c |
    c.getEnclosingModule().toString().matches("%.urls") and
    c.toString().matches("path%") and
    (
      (
        c.getArg(1) instanceof Attribute and
        ((Attribute) c.getArg(1)).getName().matches(cf.getName())
      ) or 
      (
        c.getArg(1) instanceof Call and
        ((Call) c.getArg(1)).getFunc().(Attribute).getObject().toString().matches(cf.getName())
      ) or
      (
        c.getArg(1) instanceof Call and
        ((Call) c.getArg(1)).getFunc().(Attribute).getObject().(Attribute).getName().matches(cf.getName())
      )
    )
  )
}

predicate callerCallsCallee(CallableFunction caller, CallableFunction callee, Call c) {
  (
    caller instanceof Function or
    (caller instanceof Class and isEndpoint(caller))
  ) and
  exists(FunctionInvocation funcInvoc |
    c = funcInvoc.getCall().getNode() and
    c.getScope().inSource() and
    expressionInsideCallableFunction(caller, c) and
    funcInvoc.getFunction().getFunction() = callee
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

predicate expressionInsideCallableFunction(CallableFunction cf, Expr e) {
  cf.getLocation().getFile() = e.getLocation().getFile() and
  e.getLocation().getStartLine() >= cf.getBody().getItem(0).getLocation().getStartLine() and
  e.getLocation().getEndLine() <= cf.getLastStatement().getLocation().getEndLine()
}

predicate callableAccessesEntity(CallableFunction cf, DomainEntity de, string op, Location loc) { 
  exists(Attribute a, Expr e |
    a.getScope().inSource() and
    a.getASubExpression() = e and
    expressionInsideCallableFunction(cf, a) and
    expressionInsideCallableFunction(cf, e) and
    e instanceof Attribute and 
    e.(Attribute).getName() = "objects" and
    (
      (
        e.(Attribute).getObject() instanceof Attribute and
        e.(Attribute).getObject().(Attribute).getName() = de.getName()
      ) or
      (
        not e.(Attribute).getObject() instanceof Attribute and
        (
          e.(Attribute).getObject().toString() = de.getName() or
          (
            e.(Attribute).getObject().toString() = "self" and
            a.getScope() = de
          )
        )
      )
    ) and
    getOperation(a.getName(), op) and
    loc = e.getLocation()
  )
}