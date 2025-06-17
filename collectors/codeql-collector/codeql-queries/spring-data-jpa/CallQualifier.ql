import frameworks.SpringDataJPA

from CallableFunction caller, CallableFunction callee, FunctionInvoc call
where callerCallsCallee(caller, callee, call)
select 
  caller.getDeclaringType(),
  caller,
  callee.getDeclaringType(),
  callee,
  call.getQualifier().getType(),
  call.getLocation()