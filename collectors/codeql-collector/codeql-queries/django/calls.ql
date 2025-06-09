import python

from FunctionInvocation calleeInvocation, Call call, Function caller, Function callee
where 
    call = calleeInvocation.getCall().getNode() and
    call.getScope().inSource() and
    call.getScope() = caller and
    calleeInvocation.getFunction().getFunction() = callee
select 
    caller.getScope(),
    caller, 
    caller.getLocation(),
    callee.getScope(),
    callee,
    callee.getLocation(),
    call.getLocation()