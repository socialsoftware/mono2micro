import java
import entity

from DomainEntity de, Field f
where 
    de.hasChildElement(f)
select de, f, f.getType(), de.getLocation()
order by de