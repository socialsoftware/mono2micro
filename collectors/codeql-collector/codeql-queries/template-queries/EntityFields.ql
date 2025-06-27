import frameworks.{FRAMEWORK_NAME}

from DomainEntity de, DomainField df
where de.hasField(df)
select de, de.getName(), de.getLocation(), df.getFieldName(), df.getFieldType()
order by de