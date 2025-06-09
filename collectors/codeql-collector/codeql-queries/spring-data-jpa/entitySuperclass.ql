import java
import entity
import annotations

from DomainEntity e, Class entityClass, Class superclass, string mappedSuperclass, string tableName, string entityName
where 
    entityClass = e and 
    entityClass.getASupertype() = superclass and
    (
        (isEntity(e) and mappedSuperclass = "no") or
        (isMappedSuperclass(e) and mappedSuperclass = "yes")
    ) and
    (
        exists(Table t |
            entityClass.getAnAnnotation() = t and
            tableName = t.getValue("name").toString()
        ) or
        (
            not exists(Table t |
                entityClass.getAnAnnotation() = t
            ) 
            and tableName = "null"
        )
    ) and
    (
        exists(Entity entity |
            entityClass.getAnAnnotation() = entity and
            entityName = entity.getValue("name").toString()
        ) or
        (
            not exists(Entity entity |
                entityClass.getAnAnnotation() = entity
            ) 
            and entityName = "null"
        )
    )
select entityClass, superclass, mappedSuperclass, tableName, entityName, entityClass.getLocation()
order by entityClass