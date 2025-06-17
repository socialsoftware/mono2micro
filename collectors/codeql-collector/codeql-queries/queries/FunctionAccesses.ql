import frameworks.{FRAMEWORK_NAME}

from CallableFunction caller, DomainEntity entity, string operation, Location loc
where callableAccessesEntity(caller, entity, operation, loc)
select
  caller,
  caller.getLocation(),
  entity,
  operation,
  loc