import java

predicate isMappedSuperclass(RefType e) {
  e.hasAnnotation("jakarta.persistence", "MappedSuperclass") or 
  e.hasAnnotation("javax.persistence", "MappedSuperclass")
}

predicate isEntity(RefType e) {
  e.hasAnnotation("jakarta.persistence", "Entity") or
  e.hasAnnotation("javax.persistence", "Entity")
}

/**
 * Class to identify methods that access JPA entities.
 */
class DomainEntity extends RefType {
  DomainEntity() {
    isEntity(this) or isMappedSuperclass(this)
  }
}