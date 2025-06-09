import java
import annotations

predicate elementCollectionJoinTable(Field f, string joinTableName) {
    exists(ElementCollection elmtCollection  |
        f.getAnAnnotation() = elmtCollection and
        (
          exists(CollectionTable collectionTableAnn, string tableName |
              f.getAnAnnotation() = collectionTableAnn and
              tableName = collectionTableAnn.getValue("name").toString() and
              joinTableName = tableName
          ) or
          (
            not exists(CollectionTable collectionTableAnn |
              f.getAnAnnotation() = collectionTableAnn
            ) and
            joinTableName = f.getDeclaringType().getName() + "_" + f.getName()
          )
        )
    )
}

predicate oneToOneJoinTableInfo(Field f, string joinTableName, Type relatedType) {
  exists(OneToOne oneToOne, JoinTable joinTable |
    f.getAnAnnotation() = oneToOne and
    f.getAnAnnotation() = joinTable and
    joinTableName = joinTable.getValue("name").toString() and
    relatedType = f.getType()
  )
}

predicate manyToOneJoinTableInfo(Field f, string joinTableName, Type relatedType) {
  exists(ManyToOne manyToOne, JoinTable joinTable |
    f.getAnAnnotation() = manyToOne and
    f.getAnAnnotation() = joinTable and
    joinTableName = joinTable.getValue("name").toString() and
    relatedType = f.getType()
  )
}

predicate manyToManyJoinTableInfo(Field f, string joinTableName, Type relatedType) {
  exists(ManyToMany manyToMany |
    f.getAnAnnotation() = manyToMany and
    relatedType = f.getType() and
    (
      (
        exists(JoinTable joinTable |
          f.getAnAnnotation() = joinTable and
          joinTableName = joinTable.getValue("name").toString()
        )
      ) or
      (
        not exists(JoinTable joinTable |
          f.getAnAnnotation() = joinTable
        ) and joinTableName = "null"
      )
    )
  )
}

predicate oneToManyJoinTableInfo(Field f, string joinTableName, Type relatedType) {
  exists(OneToMany oneToMany |
    f.getAnAnnotation() = oneToMany and
    relatedType = f.getType() and
    (
      (
        exists(JoinTable joinTable |
          f.getAnAnnotation() = joinTable and
          joinTableName = joinTable.getValue("name").toString()
        )
      ) or
      (
        not exists(JoinTable joinTable |
          f.getAnAnnotation() = joinTable
        ) and joinTableName = "null"
      )
    )
  )
}

from Field f, string tableName, Type relatedType
where (
    elementCollectionJoinTable(f, tableName) and relatedType = f.getDeclaringType()
    or oneToOneJoinTableInfo(f, tableName, relatedType)
    or manyToManyJoinTableInfo(f, tableName, relatedType)
    or manyToOneJoinTableInfo(f, tableName, relatedType)
    or oneToManyJoinTableInfo(f, tableName, relatedType)
)
select f.getDeclaringType(), relatedType, tableName