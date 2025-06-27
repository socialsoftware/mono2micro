import frameworks.{FRAMEWORK_NAME}

from CallableFunction cf
where isEndpoint(cf)
select cf.getFullName(), cf.getId()
