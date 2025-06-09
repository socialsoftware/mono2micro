import java

predicate extendsSuperclass(Class c, Class sup) {
  c.getASupertype() = sup and
  sup.getName().matches("%\\_Base")
}

class DomainEntity extends Class {
  DomainEntity() {
    this.fromSource() and
    exists(Class superclass |
      extendsSuperclass(this, superclass)
    )
  }
}