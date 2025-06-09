import java
import entity

from DomainEntity de, Class sup
where extendsSuperclass(de, sup)
select de, sup, "null", "null", "null", de.getLocation()
order by de