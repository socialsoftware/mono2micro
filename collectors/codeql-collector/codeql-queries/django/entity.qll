import python

class DomainEntity extends Class {
  DomainEntity() {
    this.getABase().getEnclosingModule().toString().matches("%.models")
  }
}