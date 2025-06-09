import java
import entity

from DomainEntity de, Class sup, Method m
where 
    extendsSuperclass(de, sup) and
    m.getDeclaringType() = sup and
    m.getName().matches("get%")
select de, m, m.getReturnType(), de.getLocation()
order by de