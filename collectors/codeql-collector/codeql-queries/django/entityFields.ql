import python
import entity

from DomainEntity de, AssignStmt astmt, Attribute a
where
    astmt = de.getAStmt() and
    astmt.getValue().getASubExpression() = a
select de, astmt.getATarget(), a.getName(), de.getLocation()