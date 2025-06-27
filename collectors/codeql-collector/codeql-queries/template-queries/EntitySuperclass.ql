import frameworks.{FRAMEWORK_NAME}

from DomainEntity de, DomainSuperclass ds
where de.hasSuperclass(ds)
select de, de.getName(), de.getLocation(), ds.getName()
order by de
