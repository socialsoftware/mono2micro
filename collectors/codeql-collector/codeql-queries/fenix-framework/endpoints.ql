import java
import callgraph

from Callable callable
where 
    isControllerMethod(callable)
select
    callable.getDeclaringType(),
    callable,
    callable.getLocation()