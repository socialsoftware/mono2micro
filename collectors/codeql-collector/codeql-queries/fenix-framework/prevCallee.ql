import java
import callgraph

from Callable caller, Callable callee, Call call
where
  callInsideCaller(caller, call) and
  callablesHaveNames(caller, callee) and
  callsCallee(callee, call)
select caller.getDeclaringType(),
  caller,
  callee.getDeclaringType(),
  callee,
  call.getQualifier().getType(),
  call.getLocation()