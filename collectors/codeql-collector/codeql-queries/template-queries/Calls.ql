import frameworks.{FRAMEWORK_NAME}

from CallableFunction caller, CallableFunction callee, FunctionInvoc call
where callerCallsCallee(caller, callee, call)
select
  caller.getId(),
  callee.getId(),
  call.getLocation()
