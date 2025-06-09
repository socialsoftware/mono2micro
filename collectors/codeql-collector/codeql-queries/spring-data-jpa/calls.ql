import java
import callgraph

from Callable caller, Callable callee, Call call, Type retType
where
  callInsideCaller(caller, call) and
  callablesHaveNames(caller, callee) and
  callsCallee(callee, call) and
  retType = callee.getReturnType()
select
  caller.getDeclaringType(),
  caller,
  caller.getLocation(),
  callee.getDeclaringType(),
  callee,
  callee.getLocation(),
  retType,
  callee.getSignature(),
  call.getLocation()