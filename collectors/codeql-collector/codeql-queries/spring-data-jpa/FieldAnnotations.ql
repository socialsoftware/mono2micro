import frameworks.SpringDataJPA

predicate elementCollectionJoinTable(Field f, string joinTableName) {
    exists(ElementCollectionAnnotation elmtCollection  |
        f.getAnAnnotation() = elmtCollection and
        (
          (
            exists(CollectionTableAnnotation collectionTableAnn |
                f.getAnAnnotation() = collectionTableAnn and
                joinTableName = collectionTableAnn.getValue("name").toString()
            )
          ) or
          (
            not exists(CollectionTableAnnotation collectionTableAnn |
              f.getAnAnnotation() = collectionTableAnn
            ) and
            joinTableName = f.getDeclaringType().getName() + "_" + f.getName()
          )
        )
    )
}

predicate oneToOneJoinTableInfo(Field f, string joinTableName, Type relatedType) {
  exists(OneToOneAnnotation oneToOne, JoinTableAnnotation joinTable |
    f.getAnAnnotation() = oneToOne and
    f.getAnAnnotation() = joinTable and
    joinTableName = joinTable.getValue("name").toString() and
    relatedType = f.getType()
  )
}

predicate manyToOneJoinTableInfo(Field f, string joinTableName, Type relatedType) {
  exists(ManyToOneAnnotation manyToOne, JoinTableAnnotation joinTable |
    f.getAnAnnotation() = manyToOne and
    f.getAnAnnotation() = joinTable and
    joinTableName = joinTable.getValue("name").toString() and
    relatedType = f.getType()
  )
}

predicate manyToManyJoinTableInfo(Field f, string joinTableName, Type relatedType) {
  exists(ManyToManyAnnotation manyToMany |
    f.getAnAnnotation() = manyToMany and
    relatedType = f.getType() and
    (
      (
        exists(JoinTableAnnotation joinTable |
          f.getAnAnnotation() = joinTable and
          joinTableName = joinTable.getValue("name").toString()
        )
      ) or
      (
        not exists(JoinTableAnnotation joinTable |
          f.getAnAnnotation() = joinTable
        ) and joinTableName = "null"
      )
    )
  )
}

predicate oneToManyJoinTableInfo(Field f, string joinTableName, Type relatedType) {
  exists(OneToManyAnnotation oneToMany |
    f.getAnAnnotation() = oneToMany and
    relatedType = f.getType() and
    (
      (
        exists(JoinTableAnnotation joinTable |
          f.getAnAnnotation() = joinTable and
          joinTableName = joinTable.getValue("name").toString()
        )
      ) or
      (
        not exists(JoinTableAnnotation joinTable |
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