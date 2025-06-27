import frameworks.SpringDataJPA

from CallableFunction caller, CallableFunction callee, FunctionInvoc call, DomainEntity de
where 
  callerCallsCallee(caller, callee, call) and
  call.getQualifier().getType() = de
select 
  caller.getId(),
  callee.getId(),
  call.getLocation(),
  de.getLocation()