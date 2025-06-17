import frameworks.{FRAMEWORK_NAME}

from CallableFunction caller, CallableFunction callee, FunctionInvoc call
where callerCallsCallee(caller, callee, call)
select
  caller,
  caller.getLocation(),
  callee,
  callee.getLocation(),
  call.getLocation()