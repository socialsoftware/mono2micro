import codeql.ruby.AST

/**
 * Domain entity's superclass
 */
class DomainSuperclass extends Expr {
  DomainSuperclass() {
    exists(DomainEntity de |
      this = de.getSuperclassExpr()
    )
  }

  string getName() {
    result = this.toString()
  }
}

/**
 * Domain entity definition
 */
class DomainEntity extends ClassDeclaration {
  DomainEntity() {
    this = any(ClassDeclaration c |
      c.getFile().getRelativePath().matches("%app/models%")
    )
  }

  predicate hasField(DomainField df) {
    exists(Block block, Call call, StringLiteral tableName |
        block.getAChild() = df and
        block = call.getAChild() and
        tableName = call.getAChild() and
        this.getName().toLowerCase() = tableName.toString().substring(1, tableName.toString().length() - 2)
    )
  }

  predicate hasSuperclass(DomainSuperclass ds) {
    this.getSuperclassExpr() = ds
  }

}

class DomainField extends MethodCall {
  DomainField() {
    // This assignment must be in a domain entity
    exists(DomainEntity de, Call createTable, Block block, StringLiteral tableName |
      createTable.getFile().getRelativePath().matches("%schema.rb") and
      createTable.toString() = "call to create_table" and
      tableName = createTable.getAChild() and
      tableName.toString().substring(1, tableName.toString().length() - 1) = de.getName().toLowerCase() + "s" and
      block = createTable.getAChild() and
      this = block.getAChild() and
      this.getMethodName() != "index"
    )
  }

  string getFieldName() {
    result = this.getArgument(0).toString().substring(1, this.getArgument(0).toString().length() - 1)
  }

  string getFieldType() {
    result = this.getMethodName()
  }

}

class CallableFunction extends Callable {
  CallableFunction() {
    this = any(Callable c)
  }

  string getFullName() {
    result = this.getEnclosingModule().toString() + "." + this.toString()
  }

  Location getId() {
    result = this.getLocation()
  }

}

class FunctionInvoc extends Call {
  FunctionInvoc() { this = any(Call c) }
}

predicate isEndpoint(CallableFunction cf) {
  cf.getEnclosingToplevel().toString().matches("%controller%")
}


predicate callableAccessesEntity(CallableFunction cf, DomainEntity de, string op, Location loc) {
  exists(MethodCall mc |
    mc.getReceiver().toString() = de.getName() and
    cf = mc.getEnclosingCallable() and
    getOperation(mc.getMethodName(), op) and
    loc = mc.getLocation()
  )
}

predicate getOperation(string functionName, string op) {
  (
    (
      functionName = "find" or
      functionName = "find_by" or
      functionName = "find_by!" or
      functionName = "where" or
      functionName = "all" or
      functionName = "first" or
      functionName = "last" or
      functionName = "pluck" or
      functionName = "exists?" or
      functionName = "count" or
      functionName = "sum" or
      functionName = "average" or
      functionName = "minimum" or
      functionName = "maximum" or
      functionName = "order" or
      functionName = "limit" or
      functionName = "offset"
    ) and
    op = "R"
  ) or
  (
    (
      functionName = "new" or
      functionName = "create" or
      functionName = "create!" or
      functionName = "save" or
      functionName = "save!" or
      functionName = "update" or
      functionName = "update!" or
      functionName = "update_all" or
      functionName = "assign_attributes" or
      functionName = "destroy" or
      functionName = "destroy_all" or
      functionName = "delete" or
      functionName = "delete_all"
    ) and
    op = "W"
  )
}


predicate callerCallsCallee(CallableFunction caller, CallableFunction callee, Call c) {
  caller = c.getEnclosingCallable() and
  callee = c.getATarget()
}