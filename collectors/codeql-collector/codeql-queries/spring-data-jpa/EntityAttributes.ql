import frameworks.SpringDataJPA

class TableAnnotation extends Annotation {
  TableAnnotation() {
    this.getType().hasQualifiedName("javax.persistence", "Table")
    or
    this.getType().hasQualifiedName("jakarta.persistence", "Table")
  }
}

from DomainEntity de, Class entityClass, string mappedSuperclass, string tableName, string entityName
where 
    entityClass = de and
    (
        (isEntity(de) and mappedSuperclass = "no") or
        (isMappedSuperclass(de) and mappedSuperclass = "yes")
    ) and
    (
        exists(TableAnnotation t |
            entityClass.getAnAnnotation() = t and
            tableName = t.getValue("name").toString()
        ) or
        (
            not exists(TableAnnotation t |
                entityClass.getAnAnnotation() = t
            ) 
            and tableName = "null"
        )
    ) and
    (
        exists(EntityAnnotation entity |
            entityClass.getAnAnnotation() = entity and
            entityName = entity.getValue("name").toString()
        ) or
        (
            not exists(EntityAnnotation entity |
                entityClass.getAnAnnotation() = entity
            ) 
            and entityName = "null"
        )
    )
select entityClass, mappedSuperclass, tableName, entityName, de.getLocation()
order by entityClass