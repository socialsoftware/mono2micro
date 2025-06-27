import frameworks.{FRAMEWORK_NAME}

from CallableFunction function, DomainEntity entity, string operation, Location loc
where callableAccessesEntity(function, entity, operation, loc)
select
  function.getId(),
  entity,
  entity.getLocation(),
  operation,
  loc
