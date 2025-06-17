import frameworks.{FRAMEWORK_NAME}

from DomainEntity de, DomainSuperclass ds
where de.hasSuperclass(ds)
select de, de.getLocation(), ds
order by de