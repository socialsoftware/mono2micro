import frameworks.{FRAMEWORK_NAME}

from DomainEntity de, DomainField df
where de.hasField(df)
select de, de.getLocation(), df.getName(), df.getType()
order by de